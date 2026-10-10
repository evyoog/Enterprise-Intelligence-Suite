package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolGateway;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The safety net of the synchronization (contract v1 section 8): compares what the platform holds for an organization with what a
 * tool holds, per aggregate type, and resends whatever the tool is missing or has at an older version. A tool never resends to the platform.
 *
 * <p>Per type the comparison is {@code get_state_digest}: {@code count} and {@code hash}, where hash is the lower-case hex SHA-256 of the
 * {@code id:version} lines, sorted by id as text and joined by a line feed. Only where they differ does it page through
 * {@code list_aggregate_versions}. Things a tool has that the platform does not are reported, not deleted.
 */
@Service
@RequiredArgsConstructor
public class ToolReconcileService {

    static final List<String> TYPES = List.of(PlatformEventTypes.AGGREGATE_ORGANIZATION, PlatformEventTypes.AGGREGATE_ORG_NODE,
        PlatformEventTypes.AGGREGATE_USER, PlatformEventTypes.AGGREGATE_MEMBERSHIP, PlatformEventTypes.AGGREGATE_SUBSCRIPTION,
        PlatformEventTypes.AGGREGATE_USER_ACCESS);
    static final int PAGE = 1000;

    /** What was found for one aggregate type. */
    public record TypeReport(String aggregateType, long platformCount, Long toolCount, boolean inSync, int resent, int extraInTool) {
    }

    public record Report(long organizationId, String productCode, boolean reachable, String problem, List<TypeReport> types) {
        public int resent() {
            return types.stream().mapToInt(TypeReport::resent).sum();
        }

        public boolean inSync() {
            return reachable && types.stream().allMatch(TypeReport::inSync);
        }
    }

    private final ToolConnectorRepository connectors;
    private final TenantAppSchemaRepository tenants;
    private final OrganizationRepository organizations;
    private final OrgNodeRepository nodes;
    private final OrganizationMemberRepository members;
    private final CustomerRepository customers;
    private final ProductSubscriptionRepository subscriptions;
    private final OrganizationProductAccessRepository accesses;
    private final ToolGateway gateway;
    private final ToolSyncService sync;
    private final ToolSyncMetrics metrics;

    /** The platform's {@code id → version} for one organization and tool, per aggregate type: the same ids the tool is told in messages. */
    @Transactional(readOnly = true)
    public Map<String, Map<String, Long>> platformVersions(long organizationId, ToolConnector connector) {
        Map<String, Map<String, Long>> out = new LinkedHashMap<>();
        TYPES.forEach(t -> out.put(t, new TreeMap<>()));
        organizations.findById(organizationId).ifPresent(o -> out.get(PlatformEventTypes.AGGREGATE_ORGANIZATION).put(String.valueOf(o.getId()), o.getSyncVersion()));
        nodes.findByOrganizationIdOrderBySortOrderAscNameAsc(organizationId)
            .forEach(n -> out.get(PlatformEventTypes.AGGREGATE_ORG_NODE).put(String.valueOf(n.getId()), n.getSyncVersion()));
        for (OrganizationMember m : members.findByOrganizationId(organizationId)) {
            Customer c = customers.findById(m.getCustomerId()).orElse(null);
            if (c == null || c.getKeycloakSub() == null || c.getKeycloakSub().isBlank()) {
                continue;
            }
            out.get(PlatformEventTypes.AGGREGATE_USER).put(c.getKeycloakSub(), c.getSyncVersion());
            out.get(PlatformEventTypes.AGGREGATE_MEMBERSHIP).put(c.getKeycloakSub(), m.getSyncVersion());
            accesses.findByOrganizationMemberIdAndProductId(m.getId(), connector.getProductId()).ifPresent(a ->
                out.get(PlatformEventTypes.AGGREGATE_USER_ACCESS).put(c.getKeycloakSub() + ":" + connector.getProductCode(), a.getSyncVersion()));
        }
        subscriptions.findByOwnerOrganizationIdAndProductId(organizationId, connector.getProductId())
            .ifPresent(s -> out.get(PlatformEventTypes.AGGREGATE_SUBSCRIPTION).put(String.valueOf(s.getId()), s.getSyncVersion()));
        return out;
    }

    /** Compares one organization with one tool and, when {@code repair} is true, queues a delivery for everything missing or older there. */
    public Report reconcile(long organizationId, long connectorId, boolean repair) {
        ToolConnector connector = connectors.findById(connectorId).orElseThrow(() -> new IllegalArgumentException("Unknown tool connector " + connectorId));
        TenantAppSchema tenant = tenants.findByOrganizationIdAndProductId(organizationId, connector.getProductId()).orElse(null);
        if (tenant == null || tenant.getStatus() != TenantSchemaStatus.READY) {
            return new Report(organizationId, connector.getProductCode(), false, "The tenant is not READY in this tool.", List.of());
        }
        String tenantRef = String.valueOf(organizationId);
        Map<String, Map<String, Long>> platform = platformVersions(organizationId, connector);
        ToolResult digest;
        try {
            digest = gateway.call(connector, "get_state_digest", Map.of("tenantRef", tenantRef, "aggregateTypes", TYPES));
        } catch (RuntimeException e) {
            return new Report(organizationId, connector.getProductCode(), false, "The tool could not be reached: " + e.getMessage(), List.of());
        }
        if (!digest.succeeded() || digest.data() == null || !(digest.data().get("digests") instanceof Map<?, ?> digests)) {
            return new Report(organizationId, connector.getProductCode(), false,
                "The tool did not answer get_state_digest (" + digest.status() + (digest.reason() == null ? "" : " " + digest.reason()) + ").", List.of());
        }
        List<TypeReport> reports = new ArrayList<>();
        for (String type : TYPES) {
            Map<String, Long> mine = platform.get(type);
            Map<?, ?> theirs = digests.get(type) instanceof Map<?, ?> m ? m : Map.of();
            long theirCount = theirs.get("count") instanceof Number n ? n.longValue() : 0;
            boolean same = theirCount == mine.size() && hash(mine).equals(String.valueOf(theirs.get("hash")));
            if (same) {
                reports.add(new TypeReport(type, mine.size(), theirCount, true, 0, 0));
                continue;
            }
            Map<String, Long> held;
            try {
                held = toolVersions(connector, tenantRef, type);
            } catch (RuntimeException e) {
                return new Report(organizationId, connector.getProductCode(), false, "The tool could not list " + type + ": " + e.getMessage(), reports);
            }
            int resent = 0;
            for (Map.Entry<String, Long> e : mine.entrySet()) {
                Long at = held.get(e.getKey());
                if (at == null || at < e.getValue()) {
                    if (repair && sync.enqueue(connector, organizationId, eventTypeOf(type), type, e.getKey(), e.getValue()).isPresent()) {
                        resent++;
                    }
                }
            }
            int missing = (int) mine.entrySet().stream().filter(e -> held.get(e.getKey()) == null || held.get(e.getKey()) < e.getValue()).count();
            metrics.drift(connector.getProductCode(), type, missing);
            int extra = (int) held.keySet().stream().filter(id -> !mine.containsKey(id)).count();
            reports.add(new TypeReport(type, mine.size(), theirCount, false, resent, extra));
        }
        return new Report(organizationId, connector.getProductCode(), true, null, reports);
    }

    /** Reconciles every READY tenant of every ACTIVE tool and repairs what differs (the nightly job). Returns the reports of tenants that were not in sync. */
    public List<Report> reconcileAllAndRepair() {
        List<Report> drift = new ArrayList<>();
        for (ToolConnector connector : connectors.findByStatus(com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus.ACTIVE)) {
            for (TenantAppSchema t : tenants.findAll()) {
                if (!t.getProductId().equals(connector.getProductId()) || t.getStatus() != TenantSchemaStatus.READY) {
                    continue;
                }
                try {
                    Report r = reconcile(t.getOrganizationId(), connector.getId(), true);
                    if (!r.inSync()) {
                        drift.add(r);
                    }
                } catch (RuntimeException e) {
                    drift.add(new Report(t.getOrganizationId(), connector.getProductCode(), false, e.toString(), List.of()));
                }
            }
        }
        return drift;
    }

    private Map<String, Long> toolVersions(ToolConnector connector, String tenantRef, String type) {
        Map<String, Long> held = new TreeMap<>();
        String after = null;
        while (true) {
            Map<String, Object> args = new LinkedHashMap<>();
            args.put("tenantRef", tenantRef);
            args.put("aggregateType", type);
            args.put("limit", PAGE);
            if (after != null) {
                args.put("afterId", after);
            }
            ToolResult page = gateway.call(connector, "list_aggregate_versions", args);
            if (!page.succeeded() || page.data() == null || !(page.data().get("versions") instanceof List<?> list)) {
                throw new IllegalStateException("list_aggregate_versions answered " + page.status());
            }
            String last = null;
            for (Object o : list) {
                if (o instanceof Map<?, ?> m && m.get("id") != null && m.get("version") instanceof Number v) {
                    last = String.valueOf(m.get("id"));
                    held.put(last, v.longValue());
                }
            }
            if (list.size() < PAGE || last == null) {
                return held;
            }
            after = last;
        }
    }

    /** SHA-256 over the sorted {@code id:version} lines joined by a line feed (contract v1 section 4). Public so a tool's test can use the same rule. */
    public static String hash(Map<String, Long> versions) {
        StringBuilder lines = new StringBuilder();
        new TreeMap<>(versions).forEach((id, version) -> {
            if (lines.length() > 0) {
                lines.append('\n');
            }
            lines.append(id).append(':').append(version);
        });
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(lines.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String eventTypeOf(String aggregateType) {
        return switch (aggregateType) {
            case PlatformEventTypes.AGGREGATE_ORGANIZATION -> PlatformEventTypes.ORGANIZATION_UPSERTED;
            case PlatformEventTypes.AGGREGATE_ORG_NODE -> PlatformEventTypes.ORG_NODE_UPSERTED;
            case PlatformEventTypes.AGGREGATE_USER -> PlatformEventTypes.USER_UPSERTED;
            case PlatformEventTypes.AGGREGATE_MEMBERSHIP -> PlatformEventTypes.MEMBERSHIP_CHANGED;
            case PlatformEventTypes.AGGREGATE_SUBSCRIPTION -> PlatformEventTypes.SUBSCRIPTION_SYNCED;
            default -> PlatformEventTypes.USER_PRODUCT_ACCESS_GRANTED;
        };
    }
}
