package com.vyoog.eisplatform.modules.toolsync;

import com.vyoog.eisplatform.modules.integration.service.OutboxDispatcher;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.toolsync.config.ToolSyncProperties;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.model.ToolDelivery;
import com.vyoog.eisplatform.modules.toolsync.service.ToolDeliveryService;
import com.vyoog.eisplatform.modules.toolsync.service.ToolReconcileService;
import com.vyoog.eisplatform.modules.toolsync.service.ToolSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ-INT-003 delivery to tools (TC-INT-052 … TC-INT-055): a change becomes one message per tool and organization, built from the
 * current state; a stopped tool delays nobody; failures are retried and then visible; provisioning starts a tenant and sends everything.
 * The tool is a {@link FakeToolGateway}; the outbox is driven by hand.
 */
class ToolDeliveryTest extends ToolSyncTestBase {

    @Autowired private OutboxDispatcher dispatcher;
    @Autowired private ToolDeliveryService delivery;
    @Autowired private ToolSyncService sync;
    @Autowired private ToolReconcileService reconcile;
    @Autowired private FakeToolGateway tool;
    @Autowired private ToolSyncProperties props;

    @BeforeEach
    void freshTool() {
        tool.reset();
        ReflectionTestUtils.setField(props, "maxAttempts", 8);
        ReflectionTestUtils.setField(props, "initialDelaySeconds", 5L);
        ReflectionTestUtils.setField(props, "maxDelaySeconds", 900L);
    }

    private void outbox() {
        for (int i = 0; i < 5; i++) {
            dispatcher.dispatchDue();
        }
    }

    /** Everything that is due now, including what an earlier run scheduled (a retry waits for its time; tests move it forward). */
    private void deliverEverythingDue() {
        deliveries.findAll().stream().filter(d -> d.getStatus() == DeliveryStatus.PENDING)
            .forEach(d -> { d.setNextAttemptAt(Instant.now().minusSeconds(1)); deliveries.save(d); });
        delivery.deliverDue();
    }

    private TenantAppSchema ready(Organization org, ToolConnector c) {
        TenantAppSchema t = new TenantAppSchema();
        t.setOrganizationId(org.getId());
        t.setProductId(c.getProductId());
        t.setTenantRef(String.valueOf(org.getId()));
        t.setStatus(TenantSchemaStatus.READY);
        return tenants.save(t);
    }

    private ToolConnector secondConnector() {
        ToolConnector c = new ToolConnector();
        c.setProductId(PRODUCT_ID + 1);
        c.setProductCode("valam");
        c.setBaseMcpUrl("http://valam.test/api/mcp");
        c.setClientId("valam-sync");
        c.setStatus(ConnectorStatus.ACTIVE);
        return connectors.save(c);
    }

    private List<ToolDelivery> rows(Organization org) {
        return deliveries.findAll().stream().filter(d -> d.getOrganizationId().equals(org.getId())).toList();
    }

    @Test
    void anOrganizationChangeReachesTheToolWithTheCurrentStateAndItsVersion() {
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        outbox();
        deliverEverythingDue();
        tool.calls.clear();

        org.setName("Renamed Ltd");
        organizations.save(org);
        org.setCity("Chennai");
        organizations.save(org);
        outbox();
        deliverEverythingDue();

        List<FakeToolGateway.Call> calls = tool.callsTo("upsert_organization");
        assertThat(calls).as("two changes before delivery are one message with the latest state").hasSize(1);
        Map<String, Object> envelope = calls.get(0).envelope();
        assertThat(envelope).containsEntry("contractVersion", "1").containsEntry("aggregateType", "Organization")
            .containsEntry("tenantRef", String.valueOf(org.getId())).containsEntry("aggregateId", String.valueOf(org.getId()));
        assertThat(((Number) envelope.get("version")).longValue()).isEqualTo(organizations.findById(org.getId()).orElseThrow().getSyncVersion());
        assertThat(calls.get(0).payload()).containsEntry("name", "Renamed Ltd").containsEntry("status", "ACTIVE");
        @SuppressWarnings("unchecked") Map<String, Object> attributes = (Map<String, Object>) calls.get(0).payload().get("attributes");
        assertThat(attributes).containsEntry("city", "Chennai").containsKey("businessEmail");
        assertThat(envelope.get("occurredAt").toString()).endsWith("+05:30");
    }

    @Test
    void aSubscriptionEndIsSentAsEndOfDayInIndiaWhateverTimeWasStored() {
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        ProductSubscription s = subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        outbox();
        deliverEverythingDue();

        FakeToolGateway.Call call = tool.callsTo("set_subscription").get(0);
        assertThat(call.envelope()).containsEntry("aggregateId", String.valueOf(s.getId()));
        assertThat(call.payload()).containsEntry("productCode", "thittam").containsEntry("status", "ACTIVE").containsEntry("seats", 5)
            .containsEntry("endsAt", "2026-10-31T23:59:00.000+05:30");
        assertThat(call.payload().get("startsAt").toString()).startsWith("2025-11-01T05:30:00.000+05:30");
    }

    @Test
    void aStoppedToolDelaysNobodyElseAndItsMessagesWaitForTheirRetry() {
        Organization org = organization();
        ToolConnector thittam = connector();
        ToolConnector valam = secondConnector();
        ready(org, thittam);
        ready(org, valam);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        subscription(org, valam.getProductId(), SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        tool.stop("thittam");

        org.setName("Both tools hear this");
        organizations.save(org);
        outbox();
        delivery.deliverDue();

        assertThat(tool.callsTo("upsert_organization")).extracting(FakeToolGateway.Call::productCode).containsExactly("valam");
        List<ToolDelivery> waiting = rows(org).stream().filter(d -> d.getToolConnectorId().equals(thittam.getId())).toList();
        assertThat(waiting).isNotEmpty().allSatisfy(d -> assertThat(d.getStatus()).isEqualTo(DeliveryStatus.PENDING));
        assertThat(waiting.stream().filter(d -> d.getAttempts() > 0)).as("only the first was tried; the rest of that tool was skipped").hasSize(1);
        assertThat(waiting.stream().filter(d -> d.getAttempts() > 0).findFirst().orElseThrow().getNextAttemptAt()).isAfter(Instant.now());

        tool.start("thittam");
        deliverEverythingDue();
        assertThat(rows(org)).allSatisfy(d -> assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED));
        assertThat(tool.callsTo("upsert_organization")).extracting(FakeToolGateway.Call::productCode).containsExactlyInAnyOrder("valam", "thittam");
    }

    @Test
    void aMessageThatKeepsFailingIsRetriedAndThenFailedWithItsReasonUntilAnAdminRetriesIt() {
        ReflectionTestUtils.setField(props, "maxAttempts", 3);
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        outbox();
        deliverEverythingDue();
        org.setName("Cannot be applied");
        organizations.save(org);
        outbox();
        tool.answer("upsert_organization", ToolResult.retry("INTERNAL", "db busy"), ToolResult.retry("INTERNAL", "db busy"), ToolResult.retry("INTERNAL", "db busy"));

        for (int i = 0; i < 3; i++) {
            deliverEverythingDue();
        }

        ToolDelivery failed = rows(org).stream().filter(d -> d.getAggregateType().equals("Organization")).findFirst().orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(failed.getAttempts()).isEqualTo(3);
        assertThat(failed.getLastError()).contains("INTERNAL").contains("db busy");
        assertThat(failed.getNextAttemptAt()).isNull();

        assertThat(sync.retry(failed.getId())).isTrue();
        deliverEverythingDue();
        assertThat(deliveries.findById(failed.getId()).orElseThrow().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void aRejectionIsFinalAtOnceAndKeepsTheToolsReason() {
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        outbox();
        deliverEverythingDue();
        org.setName("Nope");
        organizations.save(org);
        outbox();
        tool.answer("upsert_organization", ToolResult.rejected("INVALID_PAYLOAD", "name too long"));

        deliverEverythingDue();

        ToolDelivery failed = rows(org).stream().filter(d -> d.getAggregateType().equals("Organization")).findFirst().orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(failed.getAttempts()).isEqualTo(1);
        assertThat(failed.getLastError()).startsWith("INVALID_PAYLOAD");
    }

    @Test
    void replayDeliversTheAggregateAgainAsANewMessage() {
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        outbox();
        deliverEverythingDue();
        ToolDelivery sent = rows(org).stream().filter(d -> d.getAggregateType().equals("Subscription")).findFirst().orElseThrow();
        assertThat(sent.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        tool.calls.clear();

        ToolDelivery again = sync.replay(sent.getId()).orElseThrow();
        deliverEverythingDue();

        assertThat(again.getId()).isNotEqualTo(sent.getId());
        assertThat(again.getEventId()).isNotEqualTo(sent.getEventId());
        assertThat(tool.callsTo("set_subscription")).hasSize(1);
        assertThat(deliveries.findById(again.getId()).orElseThrow().getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void aSuspendedSubscriptionStillGetsDataButACancelledOrMissingOneDoesNot() {
        Organization suspended = organization();
        Organization cancelled = organization();
        Organization none = organization();
        ToolConnector c = connector();
        ready(suspended, c);
        ready(cancelled, c);
        ready(none, c);
        subscription(suspended, SubscriptionStatus.SUSPENDED, LocalDate.of(2026, 10, 31));
        subscription(cancelled, SubscriptionStatus.CANCELLED, LocalDate.of(2026, 10, 31));
        outbox();
        deliverEverythingDue();
        tool.calls.clear();

        for (Organization o : List.of(suspended, cancelled, none)) {
            o.setCity("Pune");
            organizations.save(o);
        }
        outbox();
        deliverEverythingDue();

        assertThat(tool.callsTo("upsert_organization")).extracting(call -> call.envelope().get("tenantRef"))
            .containsExactly(String.valueOf(suspended.getId()));
    }

    @Test
    void aDeletedNodeIsSentAsADeleteWithItsTombstoneVersion() {
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        OrgNode root = node(org, null, "Head Office", "COMPANY");
        OrgNode child = node(org, root, "Press Shop", "DEPARTMENT");
        outbox();
        deliverEverythingDue();
        tool.calls.clear();
        long before = nodes.findById(child.getId()).orElseThrow().getSyncVersion();

        nodes.deleteById(child.getId());
        outbox();
        deliverEverythingDue();

        FakeToolGateway.Call call = tool.callsTo("delete_org_node").get(0);
        assertThat(call.envelope()).containsEntry("aggregateId", String.valueOf(child.getId()));
        assertThat(call.payload()).containsEntry("id", String.valueOf(child.getId()));
        assertThat(((Number) call.envelope().get("version")).longValue()).isEqualTo(before + 1);
    }

    @Test
    void aSubscriptionWithoutATenantStartsProvisioningAndASuccessSendsEverything() {
        Organization org = organization();
        Customer admin = customer(newSub());
        OrganizationMember adminMember = member(org, admin, OrgRole.ORG_ADMIN);
        Customer person = customer(newSub());
        OrganizationMember personMember = member(org, person, OrgRole.MEMBER);
        node(org, null, "Head Office", "COMPANY");
        ToolConnector c = connector();
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        access(personMember, "PMS_USER", MembershipStatus.ACTIVE);
        outbox();

        assertThat(tenants.findByOrganizationIdAndProductId(org.getId(), c.getProductId()).orElseThrow().getStatus()).isEqualTo(TenantSchemaStatus.PENDING);
        assertThat(tool.calls).as("nothing is sent before the tenant exists").isEmpty();

        deliverEverythingDue();

        FakeToolGateway.Call provision = tool.callsTo("provision_tenant").get(0);
        assertThat(provision.arguments()).containsEntry("tenantRef", String.valueOf(org.getId())).containsEntry("datasourceRef", "default")
            .doesNotContainKeys("schemaName", "databaseName", "url");
        @SuppressWarnings("unchecked") Map<String, Object> firstAdmin = (Map<String, Object>) provision.arguments().get("firstAdmin");
        assertThat(firstAdmin).containsEntry("sub", admin.getKeycloakSub()).containsEntry("orgRole", "ORG_ADMIN");
        TenantAppSchema tenant = tenants.findByOrganizationIdAndProductId(org.getId(), c.getProductId()).orElseThrow();
        assertThat(tenant.getStatus()).isEqualTo(TenantSchemaStatus.READY);
        assertThat(tenant.getSchemaVersion()).isEqualTo("79");

        deliverEverythingDue();         // the full resync that provisioning queued
        assertThat(tool.callsTo("upsert_organization")).hasSize(1);
        assertThat(tool.callsTo("upsert_org_node")).hasSize(1);
        assertThat(tool.callsTo("upsert_user")).extracting(call -> call.envelope().get("aggregateId"))
            .containsExactlyInAnyOrder(admin.getKeycloakSub(), person.getKeycloakSub());
        assertThat(tool.callsTo("set_membership")).hasSize(2);
        assertThat(tool.callsTo("set_subscription")).hasSize(1);
        assertThat(tool.callsTo("set_user_access")).extracting(call -> call.envelope().get("aggregateId"))
            .containsExactly(person.getKeycloakSub() + ":thittam");
        assertThat(rows(org)).allSatisfy(d -> assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED));
        assertThat(adminMember.getId()).isNotNull();
    }

    @Test
    void provisioningWaitsForAnAdministratorAndFailsWhenTheToolRejectsIt() {
        Organization org = organization();
        ToolConnector c = connector();
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        outbox();

        deliverEverythingDue();
        ToolDelivery waiting = rows(org).stream().filter(d -> d.getEventType().equals("TenantProvisioningRequested")).findFirst().orElseThrow();
        assertThat(waiting.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(waiting.getLastError()).contains("NO_FIRST_ADMIN");
        assertThat(tool.callsTo("provision_tenant")).isEmpty();

        member(org, customer(newSub()), OrgRole.ORG_ADMIN);
        tool.answer("provision_tenant", ToolResult.rejected("UNKNOWN_DATASOURCE_REF", "no such datasource"));
        deliverEverythingDue();

        assertThat(deliveries.findById(waiting.getId()).orElseThrow().getStatus()).isEqualTo(DeliveryStatus.FAILED);
        TenantAppSchema tenant = tenants.findByOrganizationIdAndProductId(org.getId(), c.getProductId()).orElseThrow();
        assertThat(tenant.getStatus()).isEqualTo(TenantSchemaStatus.FAILED);
        assertThat(tenant.getLastError()).contains("UNKNOWN_DATASOURCE_REF");
    }

    @Test
    void aPausedToolKeepsItsMessagesPendingAndNothingIsLost() {
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        outbox();
        deliverEverythingDue();
        tool.calls.clear();
        c.setStatus(ConnectorStatus.PAUSED);
        connectors.save(c);

        org.setName("While paused");
        organizations.save(org);
        outbox();
        deliverEverythingDue();
        assertThat(tool.calls).isEmpty();

        c.setStatus(ConnectorStatus.ACTIVE);
        connectors.save(c);
        deliverEverythingDue();
        assertThat(tool.callsTo("upsert_organization")).hasSize(1);
    }

    @Test
    void reconcileFindsWhatTheToolLostAndResendsOnlyThat() {
        Organization org = organization();
        Customer person = customer(newSub());
        member(org, person, OrgRole.MEMBER);
        OrgNode root = node(org, null, "Head Office", "COMPANY");
        OrgNode lost = node(org, root, "Press Shop", "DEPARTMENT");
        ToolConnector c = connector();
        ready(org, c);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 10, 31));
        sync.resync(org.getId(), c.getId());
        deliverEverythingDue();
        assertThat(reconcile.reconcile(org.getId(), c.getId(), true).inSync()).as("everything arrived").isTrue();

        tool.forget("thittam", String.valueOf(org.getId()), "OrgNode", String.valueOf(lost.getId()));
        tool.calls.clear();

        ToolReconcileService.Report report = reconcile.reconcile(org.getId(), c.getId(), true);

        assertThat(report.inSync()).isFalse();
        assertThat(report.resent()).isEqualTo(1);
        assertThat(report.types()).filteredOn(t -> !t.inSync()).extracting(ToolReconcileService.TypeReport::aggregateType).containsExactly("OrgNode");
        deliverEverythingDue();
        assertThat(tool.callsTo("upsert_org_node")).extracting(call -> call.envelope().get("aggregateId")).containsExactly(String.valueOf(lost.getId()));
        assertThat(reconcile.reconcile(org.getId(), c.getId(), true).inSync()).as("repaired").isTrue();
    }

    @Test
    void reconcileSaysSoWhenTheToolCannotBeReachedAndChangesNothing() {
        Organization org = organization();
        ToolConnector c = connector();
        ready(org, c);
        tool.stop("thittam");

        ToolReconcileService.Report report = reconcile.reconcile(org.getId(), c.getId(), true);

        assertThat(report.reachable()).isFalse();
        assertThat(report.problem()).contains("could not be reached");
        assertThat(deliveries.count()).isZero();
    }
}
