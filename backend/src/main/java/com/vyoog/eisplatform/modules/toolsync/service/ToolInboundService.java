package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos.CreateNodeRequest;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos.NodeDto;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos.UpdateNodeRequest;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionClock;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * What a tool may ask of the platform (contract v1 section 5). The platform stays the single writer (BR-SYN-001): a tool reports a fact
 * or asks for a change, the platform validates and commits it with the platform's own rules, and the answer carries the new state in
 * {@code data.snapshots} (section 5.1) so the tool stores exactly what the platform stored. The fan-out of the same change to every tool
 * follows from the data change itself ({@code ToolSyncChangeRecorder}); nothing here publishes.
 *
 * <p>Every call: the caller is checked ({@link ToolCallGuard}); a changing call is done once per idempotency key
 * ({@link ToolIdempotencyService}); a rule that is broken is a {@code rejected} result, an unexpected failure is {@code retry INTERNAL},
 * and nothing is ever thrown to the tool.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ToolInboundService {

    static final Pattern SUB = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final ToolCallGuard guard;
    private final ToolIdempotencyService idempotency;
    private final ToolMessageBuilder builder;
    private final ToolSyncService sync;
    private final OrgHierarchyService hierarchy;
    private final OrganizationMemberService memberService;
    private final CustomerRepository customers;
    private final OrganizationMemberRepository members;
    private final OrganizationRepository organizations;
    private final OrganizationProductAccessRepository accesses;
    private final ProductSubscriptionRepository subscriptions;
    private final TenantAppSchemaRepository tenants;
    private final OrgNodeRepository nodes;
    private final PlatformTransactionManager transactionManager;
    private final ToolSyncMetrics metrics;
    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate readTx() {
        TransactionTemplate t = new TransactionTemplate(transactionManager);
        t.setReadOnly(true);
        t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return t;
    }

    // ------------------------------------------------------------------ the tools

    public ToolResult reportUserCreated(String azp, Map<String, Object> a) {
        return changing("report_user_created", azp, a, true, true, c -> doReportUserCreated(c, a));
    }

    public ToolResult updateUserProfile(String azp, Map<String, Object> a) {
        return changing("update_user_profile", azp, a, true, true, c -> doUpdateUserProfile(c, a));
    }

    public ToolResult createOrgNode(String azp, Map<String, Object> a) {
        return changing("create_org_node", azp, a, true, true, c -> {
            NodeDto dto = hierarchy.createAsTool(c.organizationId(), c.connector().getProductCode(), new CreateNodeRequest(
                longOf(a, "parentId"), text(a, "name"), text(a, "type"), text(a, "code"), text(a, "description"), intOf(a, "sortOrder")));
            return nodeResult(c, dto.id());
        });
    }

    public ToolResult updateOrgNode(String azp, Map<String, Object> a) {
        return changing("update_org_node", azp, a, true, true, c -> {
            Long id = longOf(a, "id");
            ToolResult stale = staleNode(c, id, a);
            if (stale != null) {
                return stale;
            }
            hierarchy.updateAsTool(c.organizationId(), c.connector().getProductCode(), id, new UpdateNodeRequest(
                text(a, "name"), text(a, "type"), text(a, "code"), text(a, "description"), intOf(a, "sortOrder"),
                a.get("active") instanceof Boolean b ? b : null, a.get("force") instanceof Boolean f ? f : null));
            return nodeResult(c, id);
        });
    }

    public ToolResult moveOrgNode(String azp, Map<String, Object> a) {
        return changing("move_org_node", azp, a, true, true, c -> {
            Long id = longOf(a, "id");
            ToolResult stale = staleNode(c, id, a);
            if (stale != null) {
                return stale;
            }
            hierarchy.moveAsTool(c.organizationId(), c.connector().getProductCode(), id, longOf(a, "newParentId"));
            return nodeResult(c, id);
        });
    }

    public ToolResult deleteOrgNode(String azp, Map<String, Object> a) {
        return changing("delete_org_node", azp, a, true, true, c -> {
            Long id = longOf(a, "id");
            ToolResult stale = staleNode(c, id, a);
            if (stale != null) {
                return stale;
            }
            long tombstone = nodes.findById(id).filter(n -> n.getOrganizationId() == c.organizationId()).map(n -> n.getSyncVersion() + 1)
                .orElseThrow(() -> new ResourceNotFoundException("Node not found"));
            hierarchy.deleteAsTool(c.organizationId(), c.connector().getProductCode(), id);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", String.valueOf(id));
            return ToolResult.applied(tombstone).withData(Map.of("snapshots",
                List.of(snapshot(PlatformEventTypes.ORG_NODE_DELETED, PlatformEventTypes.AGGREGATE_ORG_NODE, String.valueOf(id), tombstone, payload))));
        });
    }

    /** An authoritative yes or no, with the reason, from the platform's own data (contract section 5, {@code get_entitlement}). */
    public ToolResult getEntitlement(String azp, Map<String, Object> a) {
        ToolCallGuard.Outcome o = guard.check(azp, text(a, "tenantRef"), false, false);
        if (!o.allowed()) {
            return o.rejection();
        }
        try {
            return readTx().execute(s -> entitlement(o.caller(), text(a, "sub"), text(a, "productCode")));
        } catch (RuntimeException e) {
            log.warn("get_entitlement failed: {}", describe(e));
            return ToolResult.retry("INTERNAL", "The platform could not answer.");
        }
    }

    public ToolResult reportProvisioningResult(String azp, Map<String, Object> a) {
        // naturally repeatable, so the idempotency key is optional here
        boolean hasKey = text(a, "idempotencyKey") != null;
        return changing("report_provisioning_result", azp, a, false, hasKey, c -> {
            if (!c.connector().getProductCode().equals(text(a, "productCode"))) {
                return ToolResult.rejected("NOT_ALLOWED_CLIENT", "productCode is not the tool of this client.");
            }
            String status = text(a, "status");
            if (!"READY".equals(status) && !"FAILED".equals(status)) {
                return ToolResult.rejected("INVALID_PAYLOAD", "status must be READY or FAILED.");
            }
            if (!sync.recordProvisioningResult(c.organizationId(), c.connector(), "READY".equals(status), text(a, "schemaVersion"), text(a, "error"))) {
                return ToolResult.rejected("UNKNOWN_TENANT", "The platform has not asked this tool to provision this organization.");
            }
            return ToolResult.applied(null);
        });
    }

    // ------------------------------------------------------------------ plumbing

    /** Guard, then (for a changing call) once per idempotency key, in one transaction. */
    private ToolResult changing(String tool, String azp, Map<String, Object> args, boolean requireReady, boolean requireKey,
                                Function<ToolCallGuard.Caller, ToolResult> work) {
        ToolCallGuard.Outcome o = guard.check(azp, text(args, "tenantRef"), requireReady);
        if (!o.allowed()) {
            return o.rejection();
        }
        String key = text(args, "idempotencyKey");
        if (requireKey && (key == null || key.length() > 100)) {
            return ToolResult.rejected("INVALID_PAYLOAD", "idempotencyKey is required (at most 100 characters).");
        }
        if (key == null) {
            try {
                return work.apply(o.caller());
            } catch (RuntimeException e) {
                return translate(tool, e);
            }
        }
        return idempotency.once(azp, key, tool, () -> work.apply(o.caller()), e -> translate(tool, e));
    }

    /** A failure for the log without its message: a database error can quote the data being written (an e-mail address). */
    private static String describe(Throwable t) {
        Throwable root = t;
        for (int i = 0; i < 10 && root.getCause() != null && root.getCause() != root; i++) {
            root = root.getCause();
        }
        return t.getClass().getSimpleName() + (root != t ? " caused by " + root.getClass().getSimpleName() : "");
    }

    /** The rules of the platform's own services, as contract results. */
    private ToolResult translate(String tool, RuntimeException e) {
        if (e instanceof IllegalArgumentException) {
            return OrgHierarchyService.CYCLE_MESSAGE.equals(e.getMessage())
                ? ToolResult.rejected("CYCLE", e.getMessage()) : ToolResult.rejected("INVALID_PAYLOAD", e.getMessage());
        }
        if (e instanceof InvalidStateException) {
            return ToolResult.rejected("NODE_IN_USE", e.getMessage());
        }
        if (e instanceof ResourceNotFoundException || e instanceof DuplicateResourceException || e instanceof ForbiddenException) {
            return ToolResult.rejected("INVALID_PAYLOAD", e.getMessage());
        }
        if (e instanceof SeatLimitExceededException) {
            return ToolResult.rejected("SEAT_LIMIT_EXCEEDED", e.getMessage());
        }
        log.warn("Tool call {} failed: {}", tool, describe(e));
        return ToolResult.retry("INTERNAL", "The platform could not complete the call.");
    }

    // ------------------------------------------------------------------ report_user_created

    private ToolResult doReportUserCreated(ToolCallGuard.Caller c, Map<String, Object> a) {
        String sub = text(a, "sub");
        String email = text(a, "email");
        String first = text(a, "firstName");
        String last = text(a, "lastName");
        String phone = text(a, "phone");
        if (sub == null || !SUB.matcher(sub).matches()) {
            return ToolResult.rejected("INVALID_PAYLOAD", "sub must be a Keycloak user id.");
        }
        if (email == null || !email.contains("@") || email.length() > 255) {
            return ToolResult.rejected("INVALID_PAYLOAD", "email is required.");
        }
        if (first == null || last == null || first.length() > 100 || last.length() > 100 || (phone != null && phone.length() > 30)) {
            return ToolResult.rejected("INVALID_PAYLOAD", "firstName and lastName are required (at most 100 characters); phone at most 30.");
        }
        Long nodeId = longOf(a, "nodeId");
        if (nodeId != null && nodes.findById(nodeId).filter(n -> n.getOrganizationId() == c.organizationId()).isEmpty()) {
            return ToolResult.rejected("INVALID_PAYLOAD", "nodeId is not a node of this organization.");
        }
        boolean verified = Boolean.TRUE.equals(a.get("emailVerified"));
        email = email.toLowerCase();

        Customer customer = customers.findByKeycloakSub(sub).orElse(null);
        if (customer == null) {
            Customer sameEmail = customers.findByEmailIgnoreCase(email).orElse(null);
            if (sameEmail != null) {
                // never merge by an e-mail nobody has verified (contract: "never by an unverified email")
                if (sameEmail.getKeycloakSub() != null && !sameEmail.getKeycloakSub().isBlank()) {
                    return ToolResult.rejected("INVALID_PAYLOAD", "This e-mail belongs to another person.");
                }
                if (!verified) {
                    return ToolResult.rejected("EMAIL_NOT_VERIFIED", "An existing person with this e-mail can only be linked once the e-mail is verified.");
                }
                sameEmail.setKeycloakSub(sub);
                customer = customers.saveAndFlush(sameEmail);
            } else {
                Customer fresh = new Customer();
                fresh.setEmail(email);
                fresh.setFirstName(first);
                fresh.setLastName(last);
                fresh.setMobile(phone);
                fresh.setKeycloakSub(sub);
                fresh.setStatus(verified ? RegistrationStatus.COMPLETED : RegistrationStatus.PENDING_EMAIL_VERIFICATION);
                customer = customers.saveAndFlush(fresh);
            }
        }
        OrganizationMember member = members.findByOrganizationIdAndCustomerId(c.organizationId(), customer.getId()).orElse(null);
        if (member == null) {
            memberService.assertSeatAvailable(c.organizationId());
            OrganizationMember m = new OrganizationMember();
            m.setOrganizationId(c.organizationId());
            m.setCustomerId(customer.getId());
            m.setOrgRole(OrgRole.MEMBER);
            m.setStatus(MembershipStatus.ACTIVE);
            m.setOrgNodeId(nodeId);
            member = members.saveAndFlush(m);
        }
        entityManager.flush();
        return applied(userAndMembership(customer, member));
    }

    // ------------------------------------------------------------------ update_user_profile

    private ToolResult doUpdateUserProfile(ToolCallGuard.Caller c, Map<String, Object> a) {
        String sub = text(a, "sub");
        Customer customer = sub == null ? null : customers.findByKeycloakSub(sub).orElse(null);
        OrganizationMember member = customer == null ? null : members.findByOrganizationIdAndCustomerId(c.organizationId(), customer.getId()).orElse(null);
        if (member == null) {
            return ToolResult.rejected("NOT_A_MEMBER", "The person is not a member of this organization.");
        }
        Long expected = longOf(a, "expectedVersion");
        if (expected != null && expected < customer.getSyncVersion()) {
            return new ToolResult(ToolResult.REJECTED, customer.getSyncVersion(), "STALE_VERSION", "The profile changed since the version you hold.",
                Map.of("snapshots", List.of(userSnapshot(customer))));
        }
        if (!(a.get("changes") instanceof Map<?, ?> raw) || raw.isEmpty()) {
            return ToolResult.rejected("INVALID_PAYLOAD", "changes is required.");
        }
        @SuppressWarnings("unchecked") Map<String, Object> changes = (Map<String, Object>) raw;
        if (changes.containsKey("firstName")) {
            String v = text(changes, "firstName");
            if (v == null || v.length() > 100) {
                return ToolResult.rejected("INVALID_PAYLOAD", "firstName is required (at most 100 characters).");
            }
            customer.setFirstName(v);
        }
        if (changes.containsKey("lastName")) {
            String v = text(changes, "lastName");
            if (v == null || v.length() > 100) {
                return ToolResult.rejected("INVALID_PAYLOAD", "lastName is required (at most 100 characters).");
            }
            customer.setLastName(v);
        }
        if (changes.containsKey("phone")) {
            String v = text(changes, "phone");
            if (v != null && v.length() > 30) {
                return ToolResult.rejected("INVALID_PAYLOAD", "phone is at most 30 characters.");
            }
            customer.setMobile(v);
        }
        if (changes.get("attributes") instanceof Map<?, ?> attrs) {
            @SuppressWarnings("unchecked") Map<String, Object> at = (Map<String, Object>) attrs;
            if (at.containsKey("country")) customer.setCountry(limited(text(at, "country"), 100));
            if (at.containsKey("companyName")) customer.setCompanyName(limited(text(at, "companyName"), 255));
            if (at.containsKey("jobTitle")) customer.setJobTitle(limited(text(at, "jobTitle"), 150));
            if (at.containsKey("industry")) customer.setIndustry(limited(text(at, "industry"), 150));
        }
        customers.saveAndFlush(customer);
        entityManager.flush();
        return applied(List.of(userSnapshot(customer)));
    }

    // ------------------------------------------------------------------ nodes

    /** {@code STALE_VERSION} when the caller's {@code expectedVersion} is lower than the node's, with the current snapshot. */
    private ToolResult staleNode(ToolCallGuard.Caller c, Long id, Map<String, Object> a) {
        Long expected = longOf(a, "expectedVersion");
        if (expected == null || id == null) {
            return null;
        }
        Optional<OrgNode> node = nodes.findById(id).filter(n -> n.getOrganizationId() == c.organizationId());
        if (node.isPresent() && expected < node.get().getSyncVersion()) {
            return new ToolResult(ToolResult.REJECTED, node.get().getSyncVersion(), "STALE_VERSION", "The node changed since the version you hold.",
                Map.of("snapshots", List.of(nodeSnapshot(node.get()))));
        }
        return null;
    }

    private ToolResult nodeResult(ToolCallGuard.Caller c, Long id) {
        entityManager.flush();
        OrgNode node = nodes.findById(id).filter(n -> n.getOrganizationId() == c.organizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Node not found"));
        return applied(List.of(nodeSnapshot(node)));
    }

    // ------------------------------------------------------------------ get_entitlement

    ToolResult entitlement(ToolCallGuard.Caller c, String sub, String productCode) {
        if (productCode == null || !productCode.equals(c.connector().getProductCode())) {
            return ToolResult.rejected("NOT_ALLOWED_CLIENT", "productCode is not the tool of this client.");
        }
        if (sub == null || !SUB.matcher(sub).matches()) {
            return ToolResult.rejected("INVALID_PAYLOAD", "sub must be a Keycloak user id.");
        }
        Optional<TenantAppSchema> tenant = tenants.findByOrganizationIdAndProductId(c.organizationId(), c.connector().getProductId());
        if (tenant.isEmpty() || tenant.get().getStatus() != TenantSchemaStatus.READY) {
            return answer(false, "NOT_PROVISIONED", null, null);
        }
        Organization org = organizations.findById(c.organizationId()).orElse(null);
        if (org == null || org.getLifecycleStatus() != OrganizationLifecycleStatus.ACTIVE) {
            return answer(false, "ORG_INACTIVE", null, null);
        }
        Customer customer = customers.findByKeycloakSub(sub).orElse(null);
        OrganizationMember member = customer == null ? null : members.findByOrganizationIdAndCustomerId(c.organizationId(), customer.getId()).orElse(null);
        if (member == null) {
            return answer(false, "NOT_A_MEMBER", null, null);
        }
        if (member.getStatus() != MembershipStatus.ACTIVE || customer.getStatus() == RegistrationStatus.CANCELLED || customer.getStatus() == RegistrationStatus.EXPIRED) {
            return answer(false, "USER_INACTIVE", null, null);
        }
        ProductSubscription subscription = subscriptions.findByOwnerOrganizationIdAndProductId(c.organizationId(), c.connector().getProductId()).orElse(null);
        if (subscription == null || subscription.getStatus() == SubscriptionStatus.PENDING_SUBSCRIPTION) {
            return answer(false, "NO_SUBSCRIPTION", null, null);
        }
        String endsAt = subscription.getExpiresAt() == null ? null : SubscriptionClock.format(subscription.getExpiresAt());
        boolean inForce = subscription.getStatus() == SubscriptionStatus.ACTIVE
            && SubscriptionClock.effective(subscription.getStartedAt() == null ? Instant.EPOCH : subscription.getStartedAt(),
                subscription.getExpiresAt() == null ? Instant.MAX : subscription.getExpiresAt(), Instant.now());
        if (!inForce) {
            return answer(false, "SUBSCRIPTION_ENDED", endsAt, null);
        }
        Optional<OrganizationProductAccess> access = accesses.findByOrganizationMemberIdAndProductId(member.getId(), c.connector().getProductId())
            .filter(x -> x.getStatus() == MembershipStatus.ACTIVE);
        if (access.isEmpty()) {
            return answer(false, "NO_PRODUCT_ACCESS", endsAt, null);
        }
        return answer(true, "OK", endsAt, access.get().getProductRole());
    }

    private ToolResult answer(boolean allowed, String reason, String endsAt, String productRole) {
        if (!allowed) {
            metrics.entitlementDenied(reason);
        }
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("allowed", allowed);
        d.put("reason", reason);
        if (endsAt != null) {
            d.put("endsAt", endsAt);
        }
        if (productRole != null) {
            d.put("productRole", productRole);
        }
        return ToolResult.applied(null).withData(d);
    }

    // ------------------------------------------------------------------ snapshots (section 5.1)

    private List<Map<String, Object>> userAndMembership(Customer customer, OrganizationMember member) {
        List<Map<String, Object>> out = new ArrayList<>();
        out.add(userSnapshot(customer));
        out.add(snapshot(PlatformEventTypes.MEMBERSHIP_CHANGED, PlatformEventTypes.AGGREGATE_MEMBERSHIP, customer.getKeycloakSub(), member.getSyncVersion(),
            builder.membership(customer.getKeycloakSub(), member)));
        return out;
    }

    private Map<String, Object> userSnapshot(Customer customer) {
        return snapshot(PlatformEventTypes.USER_UPSERTED, PlatformEventTypes.AGGREGATE_USER, customer.getKeycloakSub(), customer.getSyncVersion(), builder.user(customer));
    }

    private Map<String, Object> nodeSnapshot(OrgNode node) {
        return snapshot(PlatformEventTypes.ORG_NODE_UPSERTED, PlatformEventTypes.AGGREGATE_ORG_NODE, String.valueOf(node.getId()), node.getSyncVersion(), builder.orgNode(node));
    }

    private static Map<String, Object> snapshot(String eventType, String aggregateType, String aggregateId, long version, Map<String, Object> payload) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("eventType", eventType);
        s.put("aggregateType", aggregateType);
        s.put("aggregateId", aggregateId);
        s.put("version", version);
        s.put("payload", payload);
        return s;
    }

    private static ToolResult applied(List<Map<String, Object>> snapshots) {
        Long version = snapshots.isEmpty() ? null : (Long) snapshots.get(0).get("version");
        return ToolResult.applied(version).withData(Map.of("snapshots", snapshots));
    }

    // ------------------------------------------------------------------ argument helpers

    private static String text(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static Long longOf(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) {
            return n.longValue();
        }
        String s = text(m, key);
        if (s == null) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a number.");
        }
    }

    private static Integer intOf(Map<String, Object> m, String key) {
        Long v = longOf(m, key);
        return v == null ? null : Math.toIntExact(v);
    }

    private static String limited(String v, int max) {
        if (v != null && v.length() > max) {
            throw new IllegalArgumentException("A value is longer than " + max + " characters.");
        }
        return v;
    }
}
