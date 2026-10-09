package com.vyoog.eisplatform.modules.toolsync;

import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.McpIdempotencyRepository;
import com.vyoog.eisplatform.modules.toolsync.service.ToolInboundService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ-INT-003, the tools a tool calls on the platform (TC-INT-056 … TC-INT-058): who may call, what a call changes, that a repeat does
 * the same thing once, and what the platform answers to "may this person use the product".
 */
class ToolInboundTest extends ToolSyncTestBase {

    private static final String AZP = "thittam-sync";

    @Autowired private ToolInboundService inbound;
    @Autowired private com.fasterxml.jackson.databind.ObjectMapper json;
    @Autowired private McpIdempotencyRepository idempotency;
    @Autowired private com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService hierarchy;

    @AfterEach
    void clean() {
        idempotency.deleteAll();
    }

    private static Map<String, Object> args(Object... pairs) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put((String) pairs[i], pairs[i + 1]);
        }
        return m;
    }

    private TenantAppSchema tenant(Organization org, ToolConnector c, TenantSchemaStatus status) {
        TenantAppSchema t = new TenantAppSchema();
        t.setOrganizationId(org.getId());
        t.setProductId(c.getProductId());
        t.setTenantRef(String.valueOf(org.getId()));
        t.setStatus(status);
        return tenants.save(t);
    }

    /** An organization the tool is connected to: READY tenant, ACTIVE subscription. */
    private Organization connected(ToolConnector c) {
        Organization org = organization();
        tenant(org, c, TenantSchemaStatus.READY);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.now().plusMonths(6));
        return org;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> snapshots(ToolResult r) {
        return (List<Map<String, Object>>) r.data().get("snapshots");
    }

    // ---- who may call ----

    @Test
    void onlyAnActiveToolWithAnActiveSubscriptionAndAReadyTenantMayCall() {
        ToolConnector c = connector();
        Organization org = connected(c);
        Organization noSubscription = organization();
        tenant(noSubscription, c, TenantSchemaStatus.READY);
        Organization notReady = organization();
        tenant(notReady, c, TenantSchemaStatus.PENDING);
        subscription(notReady, SubscriptionStatus.ACTIVE, LocalDate.now().plusMonths(1));
        Map<String, Object> call = args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", String.valueOf(org.getId()),
            "sub", newSub(), "email", "x@y.example", "emailVerified", true, "firstName", "A", "lastName", "B");

        assertThat(inbound.reportUserCreated(null, call).reason()).isEqualTo("NOT_ALLOWED_CLIENT");
        assertThat(inbound.reportUserCreated("some-other-client", call).reason()).isEqualTo("NOT_ALLOWED_CLIENT");
        assertThat(inbound.reportUserCreated(AZP, args("idempotencyKey", "k", "tenantRef", "999999999")).reason()).isEqualTo("UNKNOWN_TENANT");
        assertThat(inbound.reportUserCreated(AZP, args("idempotencyKey", "k", "tenantRef", "abc")).reason()).isEqualTo("UNKNOWN_TENANT");
        assertThat(inbound.reportUserCreated(AZP, args("idempotencyKey", "k", "tenantRef", String.valueOf(noSubscription.getId()))).reason())
            .isEqualTo("NO_ACTIVE_SUBSCRIPTION_FOR_TENANT");
        assertThat(inbound.reportUserCreated(AZP, args("idempotencyKey", "k", "tenantRef", String.valueOf(notReady.getId()))).reason())
            .isEqualTo("TENANT_NOT_READY");

        c.setStatus(ConnectorStatus.PAUSED);
        connectors.save(c);
        assertThat(inbound.reportUserCreated(AZP, call).reason()).as("a paused tool is not allowed to call").isEqualTo("NOT_ALLOWED_CLIENT");
    }

    // ---- report_user_created ----

    @Test
    void aPersonRegisteredInTheToolBecomesACustomerAndAMemberWithNoProductAccessAndARepeatDoesNothingMore() throws Exception {
        ToolConnector c = connector();
        Organization org = connected(c);
        String sub = newSub();
        String key = UUID.randomUUID().toString();
        Map<String, Object> call = args("idempotencyKey", key, "tenantRef", String.valueOf(org.getId()), "sub", sub,
            "email", "New.Person@Org.Example", "emailVerified", true, "firstName", "New", "lastName", "Person", "phone", "99999");
        long customersBefore = customers.count();

        ToolResult first = inbound.reportUserCreated(AZP, call);

        assertThat(first.status()).isEqualTo("applied");
        List<Map<String, Object>> snaps = snapshots(first);
        assertThat(snaps).extracting(s -> s.get("aggregateType")).containsExactly("User", "Membership");
        assertThat(snaps.get(0)).containsEntry("aggregateId", sub).containsEntry("version", 1L);
        @SuppressWarnings("unchecked") Map<String, Object> user = (Map<String, Object>) snaps.get(0).get("payload");
        assertThat(user).containsEntry("email", "new.person@org.example").containsEntry("emailVerified", true).containsEntry("status", "ACTIVE");
        @SuppressWarnings("unchecked") Map<String, Object> membership = (Map<String, Object>) snaps.get(1).get("payload");
        assertThat(membership).containsEntry("orgRole", "MEMBER").containsEntry("status", "ACTIVE");
        Customer saved = customers.findByKeycloakSub(sub).orElseThrow();
        OrganizationMember member = members.findByOrganizationIdAndCustomerId(org.getId(), saved.getId()).orElseThrow();
        assertThat(accesses.findByOrganizationMemberId(member.getId())).as("no product access").isEmpty();
        assertThat(events("User", sub)).as("the change fans out like any other").isNotEmpty();

        ToolResult again = inbound.reportUserCreated(AZP, call);

        assertThat(json.writeValueAsString(again)).as("the first result, again").isEqualTo(json.writeValueAsString(first));
        assertThat(customers.count()).isEqualTo(customersBefore + 1);
    }

    @Test
    void aPersonIsNeverMergedByAnUnverifiedEmailButIsLinkedWhenTheEmailIsVerified() {
        ToolConnector c = connector();
        Organization org = connected(c);
        Customer existing = customer(null);
        Customer other = customer(newSub());
        String sub = newSub();

        ToolResult unverified = inbound.reportUserCreated(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", String.valueOf(org.getId()),
            "sub", sub, "email", existing.getEmail(), "emailVerified", false, "firstName", "A", "lastName", "B"));
        ToolResult taken = inbound.reportUserCreated(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", String.valueOf(org.getId()),
            "sub", newSub(), "email", other.getEmail(), "emailVerified", true, "firstName", "A", "lastName", "B"));
        ToolResult verified = inbound.reportUserCreated(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", String.valueOf(org.getId()),
            "sub", sub, "email", existing.getEmail(), "emailVerified", true, "firstName", "A", "lastName", "B"));

        assertThat(unverified.reason()).isEqualTo("EMAIL_NOT_VERIFIED");
        assertThat(taken.reason()).isEqualTo("INVALID_PAYLOAD");
        assertThat(verified.status()).isEqualTo("applied");
        assertThat(customers.findById(existing.getId()).orElseThrow().getKeycloakSub()).isEqualTo(sub);
    }

    @Test
    void aFullOrganizationRefusesANewMemberAndAnInvalidCallChangesNothing() {
        ToolConnector c = connector();
        Organization org = connected(c);
        org.setLicensedSeats(1);
        organizations.save(org);
        member(org, customer(newSub()), OrgRole.ORG_ADMIN);
        long customersBefore = customers.count();

        ToolResult full = inbound.reportUserCreated(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", String.valueOf(org.getId()),
            "sub", newSub(), "email", "late@org.example", "emailVerified", true, "firstName", "L", "lastName", "M"));
        ToolResult invalid = inbound.reportUserCreated(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", String.valueOf(org.getId()),
            "sub", "not-a-uuid", "email", "x@y.example", "firstName", "L", "lastName", "M"));
        ToolResult noKey = inbound.reportUserCreated(AZP, args("tenantRef", String.valueOf(org.getId()), "sub", newSub(), "email", "z@y.example",
            "firstName", "L", "lastName", "M"));

        assertThat(full.reason()).isEqualTo("SEAT_LIMIT_EXCEEDED");
        assertThat(invalid.reason()).isEqualTo("INVALID_PAYLOAD");
        assertThat(noKey.reason()).isEqualTo("INVALID_PAYLOAD");
        assertThat(customers.count()).as("the rejected call rolled back").isEqualTo(customersBefore);
    }

    // ---- update_user_profile ----

    @Test
    void aProfileChangeBumpsTheVersionAndAStaleVersionIsRejectedWithTheCurrentSnapshot() {
        ToolConnector c = connector();
        Organization org = connected(c);
        Customer person = customer(newSub());
        member(org, person, OrgRole.MEMBER);
        String tenantRef = String.valueOf(org.getId());

        ToolResult changed = inbound.updateUserProfile(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "sub", person.getKeycloakSub(), "changes", args("firstName", "Renamed", "phone", "12345"), "expectedVersion", 1));

        assertThat(changed.status()).isEqualTo("applied");
        assertThat(changed.version()).isEqualTo(2L);
        assertThat(customers.findById(person.getId()).orElseThrow().getFirstName()).isEqualTo("Renamed");

        ToolResult stale = inbound.updateUserProfile(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "sub", person.getKeycloakSub(), "changes", args("firstName", "Older"), "expectedVersion", 1));

        assertThat(stale.reason()).isEqualTo("STALE_VERSION");
        assertThat(snapshots(stale).get(0)).containsEntry("version", 2L);
        assertThat(customers.findById(person.getId()).orElseThrow().getFirstName()).isEqualTo("Renamed");

        ToolResult stranger = inbound.updateUserProfile(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "sub", newSub(), "changes", args("firstName", "X")));
        assertThat(stranger.reason()).isEqualTo("NOT_A_MEMBER");
    }

    // ---- nodes ----

    @Test
    void aToolCreatesMovesAndDeletesNodesUnderThePlatformsOwnRules() {
        ToolConnector c = connector();
        Organization org = connected(c);
        hierarchy.ensureInitialisedFor(org.getId());          // the platform sets up the root and the default level types
        OrgNode root = nodes.findByOrganizationIdOrderBySortOrderAscNameAsc(org.getId()).stream().filter(n -> n.getParentId() == null).findFirst().orElseThrow();
        String tenantRef = String.valueOf(org.getId());

        ToolResult division = inbound.createOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "parentId", String.valueOf(root.getId()), "name", "North", "type", "DIVISION"));
        assertThat(division.status()).as(String.valueOf(division.message())).isEqualTo("applied");
        @SuppressWarnings("unchecked") Map<String, Object> divisionPayload = (Map<String, Object>) snapshots(division).get(0).get("payload");
        long divisionId = Long.parseLong((String) divisionPayload.get("id"));
        ToolResult department = inbound.createOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "parentId", String.valueOf(divisionId), "name", "Press Shop", "type", "DEPARTMENT"));
        @SuppressWarnings("unchecked") Map<String, Object> departmentPayload = (Map<String, Object>) snapshots(department).get(0).get("payload");
        long departmentId = Long.parseLong((String) departmentPayload.get("id"));

        ToolResult sameName = inbound.createOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "parentId", String.valueOf(root.getId()), "name", "north", "type", "DIVISION"));
        ToolResult cycle = inbound.moveOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "id", String.valueOf(divisionId), "newParentId", String.valueOf(departmentId)));
        ToolResult inUse = inbound.deleteOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef, "id", String.valueOf(divisionId)));
        ToolResult renamed = inbound.updateOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "id", String.valueOf(departmentId), "name", "Press Shop 2", "expectedVersion", 1));
        ToolResult stale = inbound.updateOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef,
            "id", String.valueOf(departmentId), "name", "Older", "expectedVersion", 1));
        long before = nodes.findById(departmentId).orElseThrow().getSyncVersion();
        ToolResult deleted = inbound.deleteOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", tenantRef, "id", String.valueOf(departmentId)));

        assertThat(sameName.status()).isEqualTo("rejected");
        assertThat(cycle.reason()).isEqualTo("CYCLE");
        assertThat(inUse.reason()).isEqualTo("NODE_IN_USE");
        assertThat(renamed.status()).isEqualTo("applied");
        assertThat(renamed.version()).isEqualTo(2L);
        assertThat(stale.reason()).isEqualTo("STALE_VERSION");
        assertThat(deleted.status()).isEqualTo("applied");
        assertThat(deleted.version()).isEqualTo(before + 1);
        assertThat(snapshots(deleted).get(0)).containsEntry("eventType", "OrgNodeDeleted").containsEntry("aggregateId", String.valueOf(departmentId));
        assertThat(nodes.findById(departmentId)).isEmpty();
        assertThat(events("OrgNode", departmentId)).as("the platform fans the change out").isNotEmpty();
    }

    @Test
    void aToolCannotTouchANodeOfAnotherOrganization() {
        ToolConnector c = connector();
        Organization mine = connected(c);
        Organization other = organization();
        OrgNode foreign = node(other, null, "Theirs", "ORGANIZATION");

        ToolResult r = inbound.deleteOrgNode(AZP, args("idempotencyKey", UUID.randomUUID().toString(), "tenantRef", String.valueOf(mine.getId()),
            "id", String.valueOf(foreign.getId())));

        assertThat(r.status()).isEqualTo("rejected");
        assertThat(nodes.findById(foreign.getId())).isPresent();
    }

    // ---- get_entitlement ----

    private ToolResult ask(Organization org, String sub) {
        return inbound.getEntitlement(AZP, args("tenantRef", String.valueOf(org.getId()), "sub", sub, "productCode", PRODUCT_CODE));
    }

    private static String reason(ToolResult r) {
        return (String) r.data().get("reason");
    }

    @Test
    void theEntitlementAnswerGivesTheReasonInTheOrderOfTheContract() {
        ToolConnector c = connector();
        Organization org = organization();
        Customer person = customer(newSub());
        OrganizationMember m = member(org, person, OrgRole.MEMBER);
        String sub = person.getKeycloakSub();

        TenantAppSchema t = tenant(org, c, TenantSchemaStatus.PENDING);
        assertThat(reason(ask(org, sub))).isEqualTo("NOT_PROVISIONED");
        t.setStatus(TenantSchemaStatus.READY);
        tenants.save(t);
        assertThat(reason(ask(org, newSub()))).isEqualTo("NOT_A_MEMBER");
        assertThat(reason(ask(org, sub))).as("no subscription row").isEqualTo("NO_SUBSCRIPTION");

        ProductSubscription s = subscription(org, SubscriptionStatus.ACTIVE, LocalDate.now().plusMonths(2));
        assertThat(reason(ask(org, sub))).isEqualTo("NO_PRODUCT_ACCESS");

        access(m, "PMS_MANAGER", MembershipStatus.ACTIVE);
        ToolResult ok = ask(org, sub);
        assertThat(ok.data()).containsEntry("allowed", true).containsEntry("reason", "OK").containsEntry("productRole", "PMS_MANAGER");
        assertThat(ok.data().get("endsAt").toString()).endsWith("T23:59:00.000+05:30");

        s.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        subscriptions.save(s);
        ToolResult ended = ask(org, sub);
        assertThat(ended.data()).containsEntry("allowed", false).containsEntry("reason", "SUBSCRIPTION_ENDED");

        s.setExpiresAt(Instant.now().plus(10, ChronoUnit.DAYS));
        s.setStatus(SubscriptionStatus.SUSPENDED);
        subscriptions.save(s);
        assertThat(reason(ask(org, sub))).isEqualTo("SUBSCRIPTION_ENDED");

        s.setStatus(SubscriptionStatus.ACTIVE);
        subscriptions.save(s);
        m.setStatus(MembershipStatus.SUSPENDED);
        members.save(m);
        assertThat(reason(ask(org, sub))).isEqualTo("USER_INACTIVE");

        m.setStatus(MembershipStatus.ACTIVE);
        members.save(m);
        org.setLifecycleStatus(OrganizationLifecycleStatus.SUSPENDED);
        organizations.save(org);
        assertThat(reason(ask(org, sub))).isEqualTo("ORG_INACTIVE");
    }

    @Test
    void anEntitlementQuestionIsAnswerableOnlyForTheCallersOwnProduct() {
        ToolConnector c = connector();
        Organization org = connected(c);

        ToolResult r = inbound.getEntitlement(AZP, args("tenantRef", String.valueOf(org.getId()), "sub", newSub(), "productCode", "valam"));

        assertThat(r.reason()).isEqualTo("NOT_ALLOWED_CLIENT");
    }

    // ---- report_provisioning_result ----

    @Test
    void theToolsProvisioningReportMakesTheTenantReadyAndStartsTheFullSend() {
        ToolConnector c = connector();
        Organization org = organization();
        member(org, customer(newSub()), OrgRole.ORG_ADMIN);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.now().plusMonths(6));
        tenant(org, c, TenantSchemaStatus.PENDING);
        String tenantRef = String.valueOf(org.getId());

        ToolResult failed = inbound.reportProvisioningResult(AZP, args("tenantRef", tenantRef, "productCode", PRODUCT_CODE, "status", "FAILED", "error", "no space"));
        TenantAppSchema afterFailure = tenants.findByOrganizationIdAndProductId(org.getId(), c.getProductId()).orElseThrow();
        ToolResult ready = inbound.reportProvisioningResult(AZP, args("tenantRef", tenantRef, "productCode", PRODUCT_CODE, "status", "READY", "schemaVersion", "79"));
        TenantAppSchema afterReady = tenants.findByOrganizationIdAndProductId(org.getId(), c.getProductId()).orElseThrow();

        assertThat(failed.status()).isEqualTo("applied");
        assertThat(afterFailure.getStatus()).isEqualTo(TenantSchemaStatus.FAILED);
        assertThat(afterFailure.getLastError()).isEqualTo("no space");
        assertThat(ready.status()).isEqualTo("applied");
        assertThat(afterReady.getStatus()).isEqualTo(TenantSchemaStatus.READY);
        assertThat(afterReady.getSchemaVersion()).isEqualTo("79");
        assertThat(afterReady.getLastError()).isNull();
        assertThat(deliveries.findAll()).filteredOn(d -> d.getOrganizationId().equals(org.getId())).extracting(d -> d.getAggregateType())
            .contains("Organization", "User", "Membership", "Subscription");
    }

    @Test
    void aProvisioningReportForAnOrganizationThePlatformNeverAskedAboutIsRefused() {
        ToolConnector c = connector();
        Organization org = organization();
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.now().plusMonths(6));

        ToolResult r = inbound.reportProvisioningResult(AZP, args("tenantRef", String.valueOf(org.getId()), "productCode", PRODUCT_CODE, "status", "READY"));

        assertThat(r.reason()).isEqualTo("UNKNOWN_TENANT");
        assertThat(c.getId()).isNotNull();
        assertThat(RegistrationStatus.values()).isNotEmpty();
    }
}
