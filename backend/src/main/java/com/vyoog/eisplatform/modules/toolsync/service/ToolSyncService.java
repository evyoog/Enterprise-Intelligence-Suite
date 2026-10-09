package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.toolsync.config.ToolSyncProperties;
import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.model.ToolDelivery;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * What the platform owes each tool for each organization: who is eligible, where an organization's tenant stands, and the rows of
 * {@code tool_delivery} that say "send this aggregate". The rows name WHAT to send; the content is built when it is sent.
 *
 * <p>A tool is eligible for an organization once the tenant of that organization in that tool is READY (see
 * {@link #requestProvisioning}). Until then nothing is sent; when provisioning succeeds, {@link #resync} sends everything.
 */
@Service
@RequiredArgsConstructor
public class ToolSyncService {

    private final ToolConnectorRepository connectors;
    private final TenantAppSchemaRepository tenants;
    private final ToolDeliveryRepository deliveries;
    private final ProductSubscriptionRepository subscriptions;
    private final OrganizationRepository organizations;
    private final OrganizationMemberRepository members;
    private final OrganizationProductAccessRepository accesses;
    private final CustomerRepository customers;
    private final OrgNodeRepository nodes;
    private final com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService hierarchy;
    private final ToolSyncProperties props;

    /** The tenant of an organization in a tool, when it is READY and the organization may use the tool. */
    @Transactional(readOnly = true)
    public boolean eligible(long organizationId, ToolConnector connector, boolean subscriptionEvent) {
        Optional<TenantAppSchema> tenant = tenants.findByOrganizationIdAndProductId(organizationId, connector.getProductId());
        if (tenant.isEmpty() || tenant.get().getStatus() != TenantSchemaStatus.READY) {
            return false;
        }
        return subscriptionEvent || hasLiveSubscription(organizationId, connector);
    }

    /** ACTIVE or SUSPENDED: the tool keeps the organization's data while a subscription is only suspended. */
    private boolean hasLiveSubscription(long organizationId, ToolConnector connector) {
        return subscriptions.findByOwnerOrganizationIdAndProductId(organizationId, connector.getProductId())
            .map(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.SUSPENDED).orElse(false);
    }

    /**
     * An organization with an ACTIVE subscription to a tool's product and no tenant there gets one: a PENDING registry row and a
     * provisioning request that is delivered like any other message (retries, backoff, FAILED).
     *
     * @return true when a request was created
     */
    @Transactional
    public boolean requestProvisioning(long organizationId, ToolConnector connector) {
        if (tenants.findByOrganizationIdAndProductId(organizationId, connector.getProductId()).isPresent()) {
            return false;
        }
        boolean active = subscriptions.findByOwnerOrganizationIdAndProductId(organizationId, connector.getProductId())
            .map(s -> s.getStatus() == SubscriptionStatus.ACTIVE).orElse(false);
        if (!active || organizations.findById(organizationId).isEmpty()) {
            return false;
        }
        TenantAppSchema tenant = new TenantAppSchema();
        tenant.setOrganizationId(organizationId);
        tenant.setProductId(connector.getProductId());
        tenant.setTenantRef(String.valueOf(organizationId));
        tenant.setDatasourceRef(props.getDefaultDatasourceRef());
        tenants.save(tenant);
        enqueue(connector, organizationId, PlatformEventTypes.TENANT_PROVISIONING_REQUESTED, PlatformEventTypes.AGGREGATE_TENANT,
            String.valueOf(organizationId), null);
        return true;
    }

    /**
     * Adds a delivery of one aggregate unless one that nothing has tried yet is already waiting: it carries the current state, so
     * a second one would only repeat it.
     *
     * @return the new delivery, or empty when it was coalesced
     */
    @Transactional
    public Optional<ToolDelivery> enqueue(ToolConnector connector, long organizationId, String eventType, String aggregateType,
                                          String aggregateId, Long aggregateVersion) {
        if (deliveries.existsByToolConnectorIdAndOrganizationIdAndAggregateTypeAndAggregateIdAndStatusAndAttempts(
                connector.getId(), organizationId, aggregateType, aggregateId, DeliveryStatus.PENDING, 0)) {
            return Optional.empty();
        }
        ToolDelivery d = new ToolDelivery();
        d.setEventId(UUID.randomUUID().toString());
        d.setToolConnectorId(connector.getId());
        d.setOrganizationId(organizationId);
        d.setTenantRef(String.valueOf(organizationId));
        d.setEventType(eventType);
        d.setAggregateType(aggregateType);
        d.setAggregateId(aggregateId);
        d.setAggregateVersion(aggregateVersion);
        return Optional.of(deliveries.save(d));
    }

    /** Same as {@link #enqueue} but keeps the id of the platform event it came from (the fan-out of an outbox event). */
    @Transactional
    public Optional<ToolDelivery> enqueueForEvent(String eventId, ToolConnector connector, long organizationId, String eventType,
                                                  String aggregateType, String aggregateId, Long aggregateVersion) {
        Optional<ToolDelivery> d = enqueue(connector, organizationId, eventType, aggregateType, aggregateId, aggregateVersion);
        d.ifPresent(x -> x.setEventId(eventId));
        return d;
    }

    /**
     * Sends everything an organization has to one tool: the organization, its hierarchy (parents first), its people and memberships,
     * the subscription of the tool's product and the people's access to it. Safe to repeat: the tool answers "duplicate" for what it has.
     *
     * @return how many deliveries were created
     */
    @Transactional
    public int resync(long organizationId, long connectorId) {
        ToolConnector connector = connectors.findById(connectorId).orElse(null);
        if (connector == null || organizations.findById(organizationId).isEmpty()) {
            return 0;
        }
        hierarchy.ensureInitialisedFor(organizationId);          // the root, so the tool has it and can create nodes under it
        int created = 0;
        created += count(enqueue(connector, organizationId, PlatformEventTypes.ORGANIZATION_UPSERTED, PlatformEventTypes.AGGREGATE_ORGANIZATION,
            String.valueOf(organizationId), organizations.findById(organizationId).map(o -> o.getSyncVersion()).orElse(null)));
        for (OrgNode n : parentsFirst(nodes.findByOrganizationIdOrderBySortOrderAscNameAsc(organizationId))) {
            created += count(enqueue(connector, organizationId, PlatformEventTypes.ORG_NODE_UPSERTED, PlatformEventTypes.AGGREGATE_ORG_NODE,
                String.valueOf(n.getId()), n.getSyncVersion()));
        }
        List<OrganizationMember> orgMembers = members.findByOrganizationId(organizationId);
        for (OrganizationMember m : orgMembers) {
            Customer c = customers.findById(m.getCustomerId()).orElse(null);
            if (c == null || c.getKeycloakSub() == null || c.getKeycloakSub().isBlank()) {
                continue;
            }
            created += count(enqueue(connector, organizationId, PlatformEventTypes.USER_UPSERTED, PlatformEventTypes.AGGREGATE_USER,
                c.getKeycloakSub(), c.getSyncVersion()));
            created += count(enqueue(connector, organizationId, PlatformEventTypes.MEMBERSHIP_CHANGED, PlatformEventTypes.AGGREGATE_MEMBERSHIP,
                c.getKeycloakSub(), m.getSyncVersion()));
        }
        Optional<ProductSubscription> subscription = subscriptions.findByOwnerOrganizationIdAndProductId(organizationId, connector.getProductId());
        if (subscription.isPresent()) {
            created += count(enqueue(connector, organizationId, PlatformEventTypes.SUBSCRIPTION_SYNCED, PlatformEventTypes.AGGREGATE_SUBSCRIPTION,
                String.valueOf(subscription.get().getId()), subscription.get().getSyncVersion()));
        }
        for (OrganizationMember m : orgMembers) {
            Customer c = customers.findById(m.getCustomerId()).orElse(null);
            if (c == null || c.getKeycloakSub() == null || c.getKeycloakSub().isBlank()) {
                continue;
            }
            Optional<com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess> access =
                accesses.findByOrganizationMemberIdAndProductId(m.getId(), connector.getProductId());
            if (access.isPresent()) {
                created += count(enqueue(connector, organizationId, PlatformEventTypes.USER_PRODUCT_ACCESS_GRANTED, PlatformEventTypes.AGGREGATE_USER_ACCESS,
                    c.getKeycloakSub() + ":" + connector.getProductCode(), access.get().getSyncVersion()));
            }
        }
        return created;
    }

    /**
     * The tool says how provisioning went ({@code report_provisioning_result}). READY starts a full resync; FAILED records why.
     *
     * @return false when the organization has no tenant registry entry for the tool
     */
    @Transactional
    public boolean recordProvisioningResult(long organizationId, ToolConnector connector, boolean ready, String schemaVersion, String error) {
        Optional<TenantAppSchema> found = tenants.findByOrganizationIdAndProductId(organizationId, connector.getProductId());
        if (found.isEmpty()) {
            return false;
        }
        TenantAppSchema t = found.get();
        boolean wasReady = t.getStatus() == TenantSchemaStatus.READY;
        t.setUpdatedAt(Instant.now());
        if (ready) {
            t.setStatus(TenantSchemaStatus.READY);
            if (schemaVersion != null && !schemaVersion.isBlank()) {
                t.setSchemaVersion(schemaVersion.trim());
            }
            t.setLastError(null);
            if (!wasReady) {
                resync(organizationId, connector.getId());
            }
        } else {
            t.setStatus(TenantSchemaStatus.FAILED);
            String text = error == null || error.isBlank() ? "The tool reported that provisioning failed." : error.trim();
            t.setLastError(text.length() > 1000 ? text.substring(0, 1000) : text);
        }
        return true;
    }

    /**
     * The administrator's "Start / resync": an organization with no tenant in the tool gets one (provisioning, when it has an ACTIVE
     * subscription to the tool's product), an organization that has one gets everything sent again.
     *
     * @return a short statement of what was done, for the monitor and the audit log
     */
    @Transactional
    public String start(long organizationId, long connectorId) {
        ToolConnector connector = connectors.findById(connectorId).orElseThrow(() -> new IllegalArgumentException("Unknown tool connector " + connectorId));
        Optional<TenantAppSchema> tenant = tenants.findByOrganizationIdAndProductId(organizationId, connector.getProductId());
        if (tenant.isEmpty()) {
            if (requestProvisioning(organizationId, connector)) {
                return "Provisioning requested";
            }
            throw new IllegalArgumentException("The organization has no ACTIVE subscription to this tool's product, so it cannot be provisioned.");
        }
        if (tenant.get().getStatus() == TenantSchemaStatus.FAILED) {
            TenantAppSchema t = tenant.get();
            t.setStatus(TenantSchemaStatus.PENDING);
            t.setLastError(null);
            t.setUpdatedAt(Instant.now());
            enqueue(connector, organizationId, PlatformEventTypes.TENANT_PROVISIONING_REQUESTED, PlatformEventTypes.AGGREGATE_TENANT, String.valueOf(organizationId), null);
            return "Provisioning requested again";
        }
        if (tenant.get().getStatus() == TenantSchemaStatus.PENDING) {
            return "Provisioning is already in progress";
        }
        return "Resync queued: " + resync(organizationId, connectorId) + " message(s)";
    }

    /** A FAILED delivery goes back to PENDING with a fresh start (admin action "Retry"). */
    @Transactional
    public boolean retry(long deliveryId) {
        return deliveries.findById(deliveryId).filter(d -> d.getStatus() == DeliveryStatus.FAILED).map(d -> {
            d.setStatus(DeliveryStatus.PENDING);
            d.setAttempts(0);
            d.setNextAttemptAt(Instant.now());
            d.setLastError(null);
            return true;
        }).orElse(false);
    }

    /** The same aggregate is sent again, as a new message with the current state (admin action "Replay"). */
    @Transactional
    public Optional<ToolDelivery> replay(long deliveryId) {
        return deliveries.findById(deliveryId).flatMap(d -> connectors.findById(d.getToolConnectorId())
            .flatMap(connector -> enqueue(connector, d.getOrganizationId(), d.getEventType(), d.getAggregateType(), d.getAggregateId(), d.getAggregateVersion())));
    }

    private static int count(Optional<?> o) {
        return o.isPresent() ? 1 : 0;
    }

    /** Hierarchy nodes ordered so that a parent comes before its children. */
    static List<OrgNode> parentsFirst(List<OrgNode> all) {
        Map<Long, OrgNode> byId = new HashMap<>();
        all.forEach(n -> byId.put(n.getId(), n));
        Map<Long, Integer> depth = new HashMap<>();
        for (OrgNode n : all) {
            int d = 0;
            Set<Long> seen = new HashSet<>();
            OrgNode cursor = n;
            while (cursor.getParentId() != null && byId.containsKey(cursor.getParentId()) && seen.add(cursor.getId())) {
                d++;
                cursor = byId.get(cursor.getParentId());
            }
            depth.put(n.getId(), d);
        }
        List<OrgNode> ordered = new ArrayList<>(all);
        ordered.sort(Comparator.comparingInt((OrgNode n) -> depth.get(n.getId())).thenComparing(OrgNode::getId));
        return ordered;
    }
}
