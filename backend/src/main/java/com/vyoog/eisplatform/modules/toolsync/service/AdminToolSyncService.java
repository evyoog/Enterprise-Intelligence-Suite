package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.ActionResultDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.ConnectorDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.DeliveryDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.DeliveryPageDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.ReconcileDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.ReconcileTypeDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.TenantDto;
import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.model.ToolDelivery;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The administrator's view of the synchronization (the "Tool sync" tab, gated by {@code MANAGE_INTEGRATIONS} in SecurityConfig): which
 * tools are connected, where each organization's tenant stands, every delivery with its attempts and error, and the actions
 * Retry, Replay, Pause/Resume, Start/Resync and Reconcile. Every action is written to the audit log with the administrator who did it.
 */
@Service
@RequiredArgsConstructor
public class AdminToolSyncService {

    public static final int PAGE_SIZE = 20;

    private final ToolConnectorRepository connectors;
    private final TenantAppSchemaRepository tenants;
    private final ToolDeliveryRepository deliveries;
    private final OrganizationRepository organizations;
    private final ToolSyncService sync;
    private final ToolReconcileService reconcile;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<ConnectorDto> overview() {
        Map<String, Long> pending = new HashMap<>();
        Map<String, Long> failed = new HashMap<>();
        for (Object[] row : deliveries.countOpenByTenant()) {
            (row[2] == DeliveryStatus.PENDING ? pending : failed).put(row[0] + "/" + row[1], (Long) row[3]);
        }
        Map<String, java.time.Instant> last = new HashMap<>();
        for (Object[] row : deliveries.lastDeliveredByTenant()) {
            last.put(row[0] + "/" + row[1], (Instant) row[2]);
        }
        Map<Long, String> names = organizations.findAllById(tenants.findAll().stream().map(TenantAppSchema::getOrganizationId).distinct().toList()).stream()
            .collect(Collectors.toMap(Organization::getId, Organization::getName));
        List<TenantAppSchema> all = tenants.findAll();
        return connectors.findAll().stream().map(c -> new ConnectorDto(c.getId(), c.getProductCode(), c.getBaseMcpUrl(), c.getClientId(), c.getStatus().name(),
            all.stream().filter(t -> t.getProductId().equals(c.getProductId())).map(t -> {
                String key = c.getId() + "/" + t.getOrganizationId();
                return new TenantDto(t.getOrganizationId(), names.get(t.getOrganizationId()), t.getStatus().name(), t.getSchemaVersion(), t.getLastSyncedAt(),
                    t.getLastError(), pending.getOrDefault(key, 0L), failed.getOrDefault(key, 0L), last.get(key));
            }).toList())).toList();
    }

    @Transactional(readOnly = true)
    public DeliveryPageDto deliveries(String status, Long organizationId, Long connectorId, int page) {
        int safePage = Math.max(page, 0);
        PageRequest request = PageRequest.of(safePage, PAGE_SIZE);
        Page<ToolDelivery> result;
        DeliveryStatus parsed = status == null || status.isBlank() ? null : parseStatus(status);
        if (connectorId != null) {
            result = deliveries.findByToolConnectorIdOrderByIdDesc(connectorId, request);
        } else if (parsed != null && organizationId != null) {
            result = deliveries.findByStatusAndOrganizationIdOrderByIdDesc(parsed, organizationId, request);
        } else if (parsed != null) {
            result = deliveries.findByStatusOrderByIdDesc(parsed, request);
        } else if (organizationId != null) {
            result = deliveries.findByOrganizationIdOrderByIdDesc(organizationId, request);
        } else {
            result = deliveries.findAllByOrderByIdDesc(request);
        }
        Map<Long, String> codes = connectors.findAll().stream().collect(Collectors.toMap(ToolConnector::getId, ToolConnector::getProductCode));
        return new DeliveryPageDto(result.getContent().stream().map(d -> dto(d, codes)).toList(), result.getTotalElements(), safePage, PAGE_SIZE);
    }

    @Transactional
    public ActionResultDto retry(Long deliveryId, String actor) {
        ToolDelivery d = find(deliveryId);
        if (d.getStatus() != DeliveryStatus.FAILED) {
            throw new InvalidStateException("Only a FAILED delivery can be retried.");
        }
        sync.retry(deliveryId);
        audit.recordSuccess("TOOL_DELIVERY_RETRIED", actor, null, null, "ToolDelivery", String.valueOf(deliveryId), d.getOrganizationId(),
            d.getEventType() + " " + d.getAggregateType() + " " + d.getAggregateId());
        return new ActionResultDto("Queued again");
    }

    @Transactional
    public ActionResultDto replay(Long deliveryId, String actor) {
        ToolDelivery d = find(deliveryId);
        Optional<ToolDelivery> again = sync.replay(deliveryId);
        audit.recordSuccess("TOOL_DELIVERY_REPLAYED", actor, null, null, "ToolDelivery", String.valueOf(deliveryId), d.getOrganizationId(),
            d.getEventType() + " " + d.getAggregateType() + " " + d.getAggregateId());
        return new ActionResultDto(again.isPresent() ? "Replay queued" : "A message for this object is already waiting; it carries the current state");
    }

    @Transactional
    public ActionResultDto setPaused(Long connectorId, boolean paused, String actor) {
        ToolConnector c = connectors.findById(connectorId).orElseThrow(() -> new ResourceNotFoundException("Tool not found"));
        c.setStatus(paused ? ConnectorStatus.PAUSED : ConnectorStatus.ACTIVE);
        c.setUpdatedAt(Instant.now());
        audit.recordSuccess(paused ? "TOOL_CONNECTOR_PAUSED" : "TOOL_CONNECTOR_RESUMED", actor, null, null, "ToolConnector", String.valueOf(c.getId()), null, c.getProductCode());
        return new ActionResultDto(paused ? "Paused: changes are kept and sent when it is resumed" : "Resumed");
    }

    @Transactional
    public ActionResultDto start(Long organizationId, Long connectorId, String actor) {
        ToolConnector c = connectors.findById(connectorId).orElseThrow(() -> new ResourceNotFoundException("Tool not found"));
        if (!organizations.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        String message = sync.start(organizationId, connectorId);
        audit.recordSuccess("TOOL_TENANT_STARTED", actor, null, null, "TenantAppSchema", organizationId + "/" + c.getProductCode(), organizationId, message);
        return new ActionResultDto(message);
    }

    public ReconcileDto reconcile(Long organizationId, Long connectorId, boolean repair, String actor) {
        ToolConnector c = connectors.findById(connectorId).orElseThrow(() -> new ResourceNotFoundException("Tool not found"));
        ToolReconcileService.Report r = reconcile.reconcile(organizationId, connectorId, repair);
        audit.recordSuccess("TOOL_TENANT_RECONCILED", actor, null, null, "TenantAppSchema", organizationId + "/" + c.getProductCode(), organizationId,
            r.reachable() ? (r.inSync() ? "In sync" : "Drift found, resent " + r.resent()) : r.problem());
        return new ReconcileDto(r.organizationId(), r.productCode(), r.reachable(), r.problem(), r.inSync(), r.resent(),
            r.types().stream().map(t -> new ReconcileTypeDto(t.aggregateType(), t.platformCount(), t.toolCount(), t.inSync(), t.resent(), t.extraInTool())).toList());
    }

    private ToolDelivery find(Long id) {
        return deliveries.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
    }

    private static DeliveryStatus parseStatus(String status) {
        try {
            return DeliveryStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    private static DeliveryDto dto(ToolDelivery d, Map<Long, String> codes) {
        return new DeliveryDto(d.getId(), d.getEventId(), d.getToolConnectorId(), codes.get(d.getToolConnectorId()), d.getOrganizationId(), d.getEventType(),
            d.getAggregateType(), d.getAggregateId(), d.getStatus().name(), d.getAttempts(), d.getNextAttemptAt(), d.getLastError(), d.getSentVersion(),
            d.getCreatedAt(), d.getDeliveredAt());
    }
}
