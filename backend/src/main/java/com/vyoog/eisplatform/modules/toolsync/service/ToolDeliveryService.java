package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.toolsync.config.ToolSyncProperties;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolGateway;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolUnavailableException;
import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.model.ToolDelivery;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolDeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Sends the due {@code tool_delivery} rows to the tools (REQ-INT-003, BR-SYN-018/019/020).
 *
 * <ul>
 *   <li><b>One row, one destination:</b> each row has its own attempts, backoff and FAILED state, so a tool that is stopped delays
 *       nobody else. When a tool is unreachable, the rest of its rows are skipped for this run (they are not charged an attempt).</li>
 *   <li><b>Current state:</b> the message is built when it is sent ({@link ToolMessageBuilder}); a retry never sends something old.</li>
 *   <li><b>Outcomes:</b> applied/duplicate → DELIVERED; retry or unreachable → next attempt with backoff (default 8 attempts, 5 s to
 *       15 min), then FAILED; rejected → FAILED at once with the tool's reason. The monitor can retry a FAILED row or replay any.</li>
 *   <li><b>Provisioning:</b> a {@code TenantProvisioningRequested} row calls {@code provision_tenant}; success makes the tenant READY
 *       and queues a full resync.</li>
 * </ul>
 * A row is claimed (one attempt counted, next try scheduled) in its own transaction before the call, so a crash in between only costs a
 * later retry, and no network call holds a database transaction. Single application instance assumed, as for the outbox dispatcher.
 */
@Service
@Slf4j
public class ToolDeliveryService {

    private final ToolDeliveryRepository deliveries;
    private final ToolConnectorRepository connectors;
    private final TenantAppSchemaRepository tenants;
    private final ToolMessageBuilder builder;
    private final ToolSyncService sync;
    private final ToolGateway gateway;
    private final ToolSyncProperties props;
    private final TransactionTemplate tx;
    private final TransactionTemplate readTx;

    public ToolDeliveryService(ToolDeliveryRepository deliveries, ToolConnectorRepository connectors, TenantAppSchemaRepository tenants,
                               ToolMessageBuilder builder, ToolSyncService sync, ToolGateway gateway, ToolSyncProperties props,
                               PlatformTransactionManager transactionManager) {
        this.deliveries = deliveries;
        this.connectors = connectors;
        this.tenants = tenants;
        this.builder = builder;
        this.sync = sync;
        this.gateway = gateway;
        this.props = props;
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.readTx = new TransactionTemplate(transactionManager);
        this.readTx.setReadOnly(true);
        this.readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** One run. Returns how many rows became DELIVERED. */
    public int deliverDue() {
        List<ToolDelivery> due = deliveries.findDue(Instant.now(), PageRequest.of(0, props.getBatchSize()));
        Map<Long, Optional<ToolConnector>> known = new HashMap<>();
        Set<Long> unreachable = new HashSet<>();
        int delivered = 0;
        for (ToolDelivery d : due) {
            Optional<ToolConnector> connector = known.computeIfAbsent(d.getToolConnectorId(), connectors::findById);
            if (connector.isEmpty() || connector.get().getStatus() != ConnectorStatus.ACTIVE || unreachable.contains(d.getToolConnectorId())) {
                continue;                       // paused or stopped tools keep their rows PENDING; others are not held up
            }
            Outcome outcome = deliver(d.getId(), connector.get());
            if (outcome == Outcome.DELIVERED) {
                delivered++;
            } else if (outcome == Outcome.UNREACHABLE) {
                unreachable.add(d.getToolConnectorId());
            }
        }
        return delivered;
    }

    enum Outcome { DELIVERED, WAITING, FAILED, UNREACHABLE, SKIPPED }

    Outcome deliver(Long deliveryId, ToolConnector connector) {
        // 1. claim: count the attempt and schedule the next one before anything is sent
        ToolDelivery claimed = tx.execute(s -> deliveries.findById(deliveryId)
            .filter(d -> d.getStatus() == DeliveryStatus.PENDING)
            .map(d -> {
                d.setAttempts(d.getAttempts() + 1);
                d.setNextAttemptAt(Instant.now().plus(backoff(d.getAttempts())));
                return detached(d);
            }).orElse(null));
        if (claimed == null) {
            return Outcome.SKIPPED;
        }
        // 2. build and send, outside any transaction that writes
        ToolResult result;
        Long sentVersion = null;
        try {
            if (PlatformEventTypes.TENANT_PROVISIONING_REQUESTED.equals(claimed.getEventType())) {
                result = provision(claimed, connector);
            } else {
                Optional<ToolMessageBuilder.Message> message = readTx.execute(s -> builder.build(claimed.getAggregateType(), claimed.getAggregateId(),
                    claimed.getOrganizationId(), connector, claimed.getEventId(), claimed.getCreatedAt(), claimed.getAggregateVersion()));
                if (message == null || message.isEmpty()) {
                    return finish(claimed, ToolResult.applied(null).withData(Map.of("note", "nothing to send: the object no longer exists for this tool")), null);
                }
                sentVersion = message.get().version();
                result = gateway.call(connector, message.get().tool(), Map.of("envelope", message.get().envelope()));
            }
        } catch (ToolUnavailableException e) {
            log.warn("Tool {} unreachable for delivery {}: {}", connector.getProductCode(), claimed.getId(), e.getMessage());
            fail(claimed, "UNREACHABLE", e.getMessage(), false);
            return Outcome.UNREACHABLE;
        } catch (RuntimeException e) {
            log.warn("Delivery {} to {} failed: {}", claimed.getId(), connector.getProductCode(), e.toString());
            fail(claimed, "INTERNAL", e.getClass().getSimpleName(), false);
            return Outcome.WAITING;
        }
        return finish(claimed, result, sentVersion);
    }

    private Outcome finish(ToolDelivery d, ToolResult result, Long sentVersion) {
        if (result.succeeded()) {
            tx.executeWithoutResult(s -> deliveries.findById(d.getId()).ifPresent(cur -> {
                cur.setStatus(DeliveryStatus.DELIVERED);
                cur.setDeliveredAt(Instant.now());
                cur.setNextAttemptAt(null);
                cur.setLastError(null);
                cur.setSentVersion(result.version() != null ? result.version() : sentVersion);
                tenants.findByOrganizationIdAndProductId(cur.getOrganizationId(), productOf(cur)).ifPresent(t -> {
                    t.setLastSyncedAt(Instant.now());
                    t.setLastSyncedVersion(Math.max(t.getLastSyncedVersion(), cur.getId()));      // the watermark: the newest delivered row
                    t.setUpdatedAt(Instant.now());
                });
            }));
            return Outcome.DELIVERED;
        }
        if (result.rejectedForGood()) {
            fail(d, result.reason(), result.message(), true);
            return Outcome.FAILED;
        }
        fail(d, result.reason() == null ? "RETRY" : result.reason(), result.message(), false);
        return Outcome.WAITING;
    }

    /** Records a failed attempt: FAILED when the tool rejected it for good or the attempts are used up; otherwise PENDING with the next try already scheduled. */
    private void fail(ToolDelivery d, String reason, String message, boolean permanent) {
        tx.executeWithoutResult(s -> deliveries.findById(d.getId()).ifPresent(cur -> {
            String text = reason + (message == null || message.isBlank() ? "" : ": " + message);
            cur.setLastError(text.length() > 1000 ? text.substring(0, 1000) : text);
            if (permanent || cur.getAttempts() >= props.getMaxAttempts()) {
                cur.setStatus(DeliveryStatus.FAILED);
                cur.setNextAttemptAt(null);
                if (PlatformEventTypes.TENANT_PROVISIONING_REQUESTED.equals(cur.getEventType())) {
                    tenants.findByOrganizationIdAndProductId(cur.getOrganizationId(), productOf(cur)).ifPresent(t -> {
                        t.setStatus(TenantSchemaStatus.FAILED);
                        t.setLastError(cur.getLastError());
                        t.setUpdatedAt(Instant.now());
                    });
                }
            } else {
                cur.setNextAttemptAt(Instant.now().plus(backoff(cur.getAttempts())));
            }
        }));
    }

    // ---- provisioning ----

    private ToolResult provision(ToolDelivery d, ToolConnector connector) {
        TenantAppSchema tenant = readTx.execute(s -> tenants.findByOrganizationIdAndProductId(d.getOrganizationId(), connector.getProductId()).orElse(null));
        if (tenant == null) {
            return ToolResult.rejected("UNKNOWN_TENANT", "the organization has no tenant registry entry for this tool");
        }
        if (tenant.getStatus() == TenantSchemaStatus.READY) {
            return ToolResult.duplicate(null);             // already provisioned (the tool reported it first)
        }
        Optional<Map<String, Object>> args = readTx.execute(s -> builder.provisionTenantArguments(d.getOrganizationId(), tenant.getDatasourceRef(), tenant.getSchemaVersion()));
        if (args == null || args.isEmpty()) {
            return ToolResult.retry("NO_FIRST_ADMIN", "the organization has no active administrator with a Keycloak id yet");
        }
        ToolResult result = gateway.call(connector, "provision_tenant", args.get());
        if (result.succeeded()) {
            String schemaVersion = result.data() != null && result.data().get("schemaVersion") != null ? String.valueOf(result.data().get("schemaVersion")) : null;
            tx.executeWithoutResult(s -> {
                tenants.findByOrganizationIdAndProductId(d.getOrganizationId(), connector.getProductId()).ifPresent(t -> {
                    t.setStatus(TenantSchemaStatus.READY);
                    if (schemaVersion != null) {
                        t.setSchemaVersion(schemaVersion);
                    }
                    t.setLastError(null);
                    t.setUpdatedAt(Instant.now());
                });
                sync.resync(d.getOrganizationId(), connector.getId());
            });
        }
        return result;
    }

    private Long productOf(ToolDelivery d) {
        return connectors.findById(d.getToolConnectorId()).map(ToolConnector::getProductId).orElse(-1L);
    }

    /** 5 s, 10 s, 20 s … doubling, capped (contract v1 section 6 defaults: 8 attempts, 5 s to 15 min). */
    Duration backoff(int attempts) {
        long seconds = props.getInitialDelaySeconds();
        for (int i = 1; i < attempts && seconds < props.getMaxDelaySeconds(); i++) {
            seconds *= 2;
        }
        return Duration.ofSeconds(Math.min(seconds, props.getMaxDelaySeconds()));
    }

    /** A copy that is safe to use after the claiming transaction has ended. */
    private static ToolDelivery detached(ToolDelivery d) {
        ToolDelivery c = new ToolDelivery();
        c.setId(d.getId());
        c.setEventId(d.getEventId());
        c.setToolConnectorId(d.getToolConnectorId());
        c.setOrganizationId(d.getOrganizationId());
        c.setTenantRef(d.getTenantRef());
        c.setEventType(d.getEventType());
        c.setAggregateType(d.getAggregateType());
        c.setAggregateId(d.getAggregateId());
        c.setAggregateVersion(d.getAggregateVersion());
        c.setAttempts(d.getAttempts());
        c.setCreatedAt(d.getCreatedAt());
        return c;
    }
}
