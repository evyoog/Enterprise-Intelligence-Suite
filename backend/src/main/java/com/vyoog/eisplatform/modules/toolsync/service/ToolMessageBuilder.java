package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgLevel;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgLevelRepository;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionClock;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the contract v1 snapshots (sections 3.1–3.6) from the CURRENT state of the platform, when a message is sent (BR-SYN-018):
 * a retry or a replay never sends something old. Only what the contract allows leaves the platform: no passwords, tokens, MFA
 * secrets, payment or bank data (BR-SYN-011). An aggregate that does not exist (or does not belong to the organization or the tool)
 * gives no message.
 */
@Service
@RequiredArgsConstructor
public class ToolMessageBuilder {

    /** Perpetual subscriptions (no expiry in the platform) are sent with this end so the contract's "endsAt" is always present. */
    static final LocalDate PERPETUAL_END = LocalDate.of(2099, 12, 31);

    /** What to send: the tool to call, the envelope (or tool arguments), and the version it carries. */
    public record Message(String tool, Map<String, Object> envelope, long version) {
    }

    private final OrganizationRepository organizations;
    private final OrgNodeRepository nodes;
    private final OrgLevelRepository levels;
    private final CustomerRepository customers;
    private final OrganizationMemberRepository members;
    private final OrganizationProductAccessRepository accesses;
    private final ProductSubscriptionRepository subscriptions;
    private final ProductPlanRepository plans;

    /**
     * @param hintVersion the version recorded with the delivery; only used for a deleted hierarchy node (its tombstone version)
     */
    public Optional<Message> build(String aggregateType, String aggregateId, long organizationId, ToolConnector connector,
                                   String eventId, Instant occurredAt, Long hintVersion) {
        String tenantRef = String.valueOf(organizationId);
        return switch (aggregateType) {
            case PlatformEventTypes.AGGREGATE_ORGANIZATION -> organizations.findById(parse(aggregateId)).filter(o -> o.getId() == organizationId)
                .map(o -> message("upsert_organization", PlatformEventTypes.ORGANIZATION_UPSERTED, aggregateType, aggregateId, o.getSyncVersion(),
                    tenantRef, eventId, occurredAt, organization(o)));
            case PlatformEventTypes.AGGREGATE_ORG_NODE -> node(aggregateId, organizationId, tenantRef, eventId, occurredAt, hintVersion);
            case PlatformEventTypes.AGGREGATE_USER -> customers.findByKeycloakSub(aggregateId).filter(c -> memberOf(c, organizationId).isPresent())
                .map(c -> message("upsert_user", PlatformEventTypes.USER_UPSERTED, aggregateType, aggregateId, c.getSyncVersion(),
                    tenantRef, eventId, occurredAt, user(c)));
            case PlatformEventTypes.AGGREGATE_MEMBERSHIP -> customers.findByKeycloakSub(aggregateId).flatMap(c -> memberOf(c, organizationId)
                .map(m -> message("set_membership", PlatformEventTypes.MEMBERSHIP_CHANGED, aggregateType, aggregateId, m.getSyncVersion(),
                    tenantRef, eventId, occurredAt, membership(aggregateId, m))));
            case PlatformEventTypes.AGGREGATE_SUBSCRIPTION -> subscriptions.findById(parse(aggregateId))
                .filter(s -> s.getOwnerType() == RegistrationOwnerType.ORGANIZATION && Long.valueOf(organizationId).equals(s.getOwnerOrganizationId())
                    && s.getProductId().equals(connector.getProductId()))
                .map(s -> message("set_subscription", subscriptionEventType(s), aggregateType, aggregateId, s.getSyncVersion(),
                    tenantRef, eventId, occurredAt, subscription(s, connector)));
            case PlatformEventTypes.AGGREGATE_USER_ACCESS -> userAccess(aggregateId, organizationId, connector, tenantRef, eventId, occurredAt);
            default -> Optional.empty();
        };
    }

    /**
     * The arguments of {@code provision_tenant} (contract section 4): the organization snapshot and the first administrator, the
     * earliest-joined active ORG_ADMIN who has a Keycloak id. Empty when the organization has no such person yet.
     */
    public Optional<Map<String, Object>> provisionTenantArguments(long organizationId, String datasourceRef, String schemaVersion) {
        Optional<Organization> organization = organizations.findById(organizationId);
        if (organization.isEmpty()) {
            return Optional.empty();
        }
        return members.findByOrganizationIdAndOrgRoleAndStatus(organizationId, OrgRole.ORG_ADMIN, MembershipStatus.ACTIVE).stream()
            .sorted(java.util.Comparator.comparing(OrganizationMember::getJoinedAt).thenComparing(OrganizationMember::getId))
            .map(m -> customers.findById(m.getCustomerId()).filter(c -> c.getKeycloakSub() != null && !c.getKeycloakSub().isBlank())
                .map(c -> {
                    Map<String, Object> admin = new LinkedHashMap<>(user(c));
                    admin.putAll(membership(c.getKeycloakSub(), m));
                    Map<String, Object> args = new LinkedHashMap<>();
                    args.put("tenantRef", String.valueOf(organizationId));
                    args.put("datasourceRef", datasourceRef);
                    args.put("organization", organization(organization.get()));
                    args.put("firstAdmin", admin);
                    if (schemaVersion != null) {
                        args.put("schemaVersion", schemaVersion);
                    }
                    return args;
                }))
            .flatMap(Optional::stream).findFirst();
    }

    // ---- snapshots (contract section 3) ----

    /** 3.1 — every organization field the platform holds, except nothing that is payment data (none is held here). */
    public Map<String, Object> organization(Organization o) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", String.valueOf(o.getId()));
        put(p, "code", o.getCode());
        p.put("name", o.getName());
        put(p, "type", o.getType());
        p.put("status", o.getLifecycleStatus().name());
        if (o.getParentOrganizationId() != null) {
            p.put("parentOrganizationId", String.valueOf(o.getParentOrganizationId()));
        }
        p.put("licensedSeats", o.getLicensedSeats());
        Map<String, Object> a = new LinkedHashMap<>();
        put(a, "industry", o.getIndustry());
        put(a, "website", o.getWebsite());
        put(a, "businessEmail", o.getBusinessEmail());
        put(a, "phone", o.getPhone());
        put(a, "country", o.getCountry());
        put(a, "state", o.getState());
        put(a, "city", o.getCity());
        put(a, "address", o.getAddress());
        put(a, "gstin", o.getGstin());
        put(a, "pan", o.getPan());
        put(a, "companyRegistrationNumber", o.getCompanyRegistrationNumber());
        put(a, "taxVatNumber", o.getTaxVatNumber());
        a.put("billingSameAsAddress", o.isBillingSameAsAddress());
        put(a, "billingAddress", o.getBillingAddress());
        put(a, "billingCountry", o.getBillingCountry());
        put(a, "billingState", o.getBillingState());
        put(a, "billingCity", o.getBillingCity());
        if (o.getRegionId() != null) {
            a.put("regionId", String.valueOf(o.getRegionId()));
        }
        a.put("mfaRequired", o.isMfaRequired());
        a.put("allowSeatOverage", o.isAllowSeatOverage());
        p.put("attributes", a);
        return p;
    }

    /** 3.2 */
    public Map<String, Object> orgNode(OrgNode n) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", String.valueOf(n.getId()));
        if (n.getParentId() != null) {
            p.put("parentId", String.valueOf(n.getParentId()));
        }
        p.put("name", n.getName());
        p.put("type", n.getNodeType());
        levels.findByOrganizationIdOrderByLevelRank(n.getOrganizationId()).stream()
            .filter(l -> l.getNodeType().equals(n.getNodeType())).findFirst().map(OrgLevel::getLevelRank)
            .ifPresent(rank -> p.put("levelRank", rank));
        put(p, "code", n.getCode());
        put(p, "description", n.getDescription());
        p.put("sortOrder", n.getSortOrder());
        p.put("active", n.isActive());
        return p;
    }

    /** 3.3 — profile only; credentials never. */
    public Map<String, Object> user(Customer c) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("sub", c.getKeycloakSub());
        p.put("platformUserId", String.valueOf(c.getId()));
        p.put("email", c.getEmail() == null ? "" : c.getEmail().trim().toLowerCase());
        p.put("emailVerified", c.getStatus() != RegistrationStatus.PENDING_EMAIL_VERIFICATION);
        p.put("firstName", c.getFirstName());
        p.put("lastName", c.getLastName());
        put(p, "phone", c.getMobile());
        p.put("status", c.getStatus() == RegistrationStatus.CANCELLED || c.getStatus() == RegistrationStatus.EXPIRED ? "INACTIVE" : "ACTIVE");
        Map<String, Object> a = new LinkedHashMap<>();
        put(a, "country", c.getCountry());
        put(a, "companyName", c.getCompanyName());
        put(a, "jobTitle", c.getJobTitle());
        put(a, "industry", c.getIndustry());
        if (!a.isEmpty()) {
            p.put("attributes", a);
        }
        return p;
    }

    /** 3.4 */
    public Map<String, Object> membership(String sub, OrganizationMember m) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("sub", sub);
        p.put("status", m.getStatus().name());
        p.put("orgRole", m.getOrgRole().name());
        if (m.getOrgNodeId() != null) {
            p.put("nodeId", String.valueOf(m.getOrgNodeId()));
        }
        return p;
    }

    /** 3.5 — the end is always 23:59:00.000+05:30 (BR-SUB-010, A3). */
    public Map<String, Object> subscription(ProductSubscription s, ToolConnector connector) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("productCode", connector.getProductCode());
        if (s.getPlanId() != null) {
            plans.findById(s.getPlanId()).map(ProductPlan::getName).ifPresent(name -> p.put("plan", name));
        }
        p.put("status", s.getStatus().name());
        Instant starts = s.getStartedAt() != null ? s.getStartedAt() : s.getCreatedAt() != null ? s.getCreatedAt() : Instant.EPOCH;
        p.put("startsAt", SubscriptionClock.format(starts));
        p.put("endsAt", SubscriptionClock.format(s.getExpiresAt() != null ? s.getExpiresAt() : SubscriptionClock.endOfDay(PERPETUAL_END)));
        p.put("seats", Math.max(1, s.getQuantity()));
        p.put("autoRenew", s.isAutoRenew());
        return p;
    }

    /** 3.6 */
    public Map<String, Object> userAccess(String sub, OrganizationProductAccess a, ToolConnector connector) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("sub", sub);
        p.put("productCode", connector.getProductCode());
        p.put("productRole", a.getProductRole());
        p.put("status", a.getStatus() == MembershipStatus.ACTIVE ? "ACTIVE" : "INACTIVE");
        return p;
    }

    public static String subscriptionEventType(ProductSubscription s) {
        return switch (s.getStatus()) {
            case ACTIVE -> "SubscriptionChanged";
            case SUSPENDED -> "SubscriptionSuspended";
            case CANCELLED -> "SubscriptionCancelled";
            case EXPIRED -> "SubscriptionExpired";
            default -> "SubscriptionChanged";
        };
    }

    // ---- aggregates that need a lookup ----

    private Optional<Message> node(String aggregateId, long organizationId, String tenantRef, String eventId, Instant occurredAt, Long hintVersion) {
        Optional<OrgNode> found = nodes.findById(parse(aggregateId)).filter(n -> n.getOrganizationId() == organizationId);
        if (found.isPresent()) {
            OrgNode n = found.get();
            return Optional.of(message("upsert_org_node", PlatformEventTypes.ORG_NODE_UPSERTED, PlatformEventTypes.AGGREGATE_ORG_NODE, aggregateId,
                n.getSyncVersion(), tenantRef, eventId, occurredAt, orgNode(n)));
        }
        if (nodes.existsById(parse(aggregateId)) || hintVersion == null) {
            return Optional.empty();       // belongs to another organization, or we do not know the tombstone version
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", aggregateId);
        return Optional.of(message("delete_org_node", PlatformEventTypes.ORG_NODE_DELETED, PlatformEventTypes.AGGREGATE_ORG_NODE, aggregateId,
            hintVersion, tenantRef, eventId, occurredAt, payload));
    }

    private Optional<Message> userAccess(String aggregateId, long organizationId, ToolConnector connector, String tenantRef, String eventId, Instant occurredAt) {
        int colon = aggregateId.indexOf(':');
        if (colon < 0 || !aggregateId.substring(colon + 1).equals(connector.getProductCode())) {
            return Optional.empty();
        }
        String sub = aggregateId.substring(0, colon);
        return customers.findByKeycloakSub(sub).flatMap(c -> memberOf(c, organizationId))
            .flatMap(m -> accesses.findByOrganizationMemberIdAndProductId(m.getId(), connector.getProductId()))
            .map(a -> message("set_user_access", a.getStatus() == MembershipStatus.ACTIVE
                    ? PlatformEventTypes.USER_PRODUCT_ACCESS_GRANTED : PlatformEventTypes.USER_PRODUCT_ACCESS_REVOKED,
                PlatformEventTypes.AGGREGATE_USER_ACCESS, aggregateId, a.getSyncVersion(), tenantRef, eventId, occurredAt, userAccess(sub, a, connector)));
    }

    private Optional<OrganizationMember> memberOf(Customer customer, long organizationId) {
        return members.findByOrganizationIdAndCustomerId(organizationId, customer.getId());
    }

    private static Message message(String tool, String eventType, String aggregateType, String aggregateId, long version, String tenantRef,
                                   String eventId, Instant occurredAt, Map<String, Object> payload) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("contractVersion", "1");
        e.put("eventId", eventId);
        e.put("eventType", eventType);
        e.put("occurredAt", SubscriptionClock.format(occurredAt));
        e.put("aggregateType", aggregateType);
        e.put("aggregateId", aggregateId);
        e.put("version", version);
        e.put("tenantRef", tenantRef);
        e.put("payload", payload);
        return new Message(tool, e, version);
    }

    private static long parse(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void put(Map<String, Object> m, String key, String value) {
        if (value != null && !value.isBlank()) {
            m.put(key, value.trim());
        }
    }
}
