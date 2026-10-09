package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.audit.model.AuditLog;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.model.ToolDelivery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The "Tool sync" monitor (TC-INT-055): gated by MANAGE_INTEGRATIONS, shows tools, tenants and deliveries, and its actions are audited. */
@AutoConfigureMockMvc
class AdminToolSyncTest extends ToolSyncTestBase {

    private static final String ADMIN_SUB = "admin-sub-for-tool-sync";

    @Autowired private MockMvc mvc;
    @Autowired private AuditLogRepository auditLog;
    @Autowired private FakeToolGateway tool;

    private RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject(ADMIN_SUB)).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private ToolDelivery delivery(ToolConnector c, Organization org, DeliveryStatus status, int attempts, String error) {
        ToolDelivery d = new ToolDelivery();
        d.setEventId(java.util.UUID.randomUUID().toString());
        d.setToolConnectorId(c.getId());
        d.setOrganizationId(org.getId());
        d.setTenantRef(String.valueOf(org.getId()));
        d.setEventType("OrganizationUpserted");
        d.setAggregateType("Organization");
        d.setAggregateId(String.valueOf(org.getId()));
        d.setStatus(status);
        d.setAttempts(attempts);
        d.setLastError(error);
        d.setNextAttemptAt(status == DeliveryStatus.PENDING ? java.time.Instant.now().plusSeconds(60) : null);
        d.setDeliveredAt(status == DeliveryStatus.DELIVERED ? java.time.Instant.now() : null);
        return deliveries.save(d);
    }

    private TenantAppSchema tenant(Organization org, ToolConnector c, TenantSchemaStatus status) {
        TenantAppSchema t = new TenantAppSchema();
        t.setOrganizationId(org.getId());
        t.setProductId(c.getProductId());
        t.setTenantRef(String.valueOf(org.getId()));
        t.setStatus(status);
        return tenants.save(t);
    }

    private List<AuditLog> audited(String action) {
        return auditLog.findAll().stream().filter(a -> action.equals(a.getAction()) && ADMIN_SUB.equals(a.getActorKeycloakSub())).toList();
    }

    @Test
    void onlyAPlatformAdminWithManageIntegrationsSeesTheMonitor() throws Exception {
        mvc.perform(get("/admin/events/tool-sync/overview")).andExpect(status().isUnauthorized());
        mvc.perform(get("/admin/events/tool-sync/overview").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/events/tool-sync/deliveries/1/retry").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/events/tool-sync/overview").with(admin())).andExpect(status().isOk());
        mvc.perform(get("/admin/events/tool-sync/deliveries").with(admin())).andExpect(status().isOk()).andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void theOverviewShowsEachToolAndTheStateOfEachTenantWithItsWaitingAndFailedCounts() throws Exception {
        ToolConnector c = connector();
        Organization org = organization();
        TenantAppSchema t = tenant(org, c, TenantSchemaStatus.READY);
        t.setSchemaVersion("79");
        tenants.save(t);
        delivery(c, org, DeliveryStatus.PENDING, 0, null);
        delivery(c, org, DeliveryStatus.PENDING, 2, "UNREACHABLE: connection refused");
        delivery(c, org, DeliveryStatus.FAILED, 8, "INVALID_PAYLOAD: name too long");
        delivery(c, org, DeliveryStatus.DELIVERED, 1, null);

        mvc.perform(get("/admin/events/tool-sync/overview").with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].productCode").value("thittam"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"))
            .andExpect(jsonPath("$[0].tenants[0].organizationName").value(org.getName()))
            .andExpect(jsonPath("$[0].tenants[0].status").value("READY"))
            .andExpect(jsonPath("$[0].tenants[0].schemaVersion").value("79"))
            .andExpect(jsonPath("$[0].tenants[0].pending").value(2))
            .andExpect(jsonPath("$[0].tenants[0].failed").value(1))
            .andExpect(jsonPath("$[0].tenants[0].lastDeliveredAt").exists());
        mvc.perform(get("/admin/events/tool-sync/deliveries?status=FAILED&organizationId=" + org.getId()).with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.items[0].productCode").value("thittam"))
            .andExpect(jsonPath("$.items[0].lastError").value("INVALID_PAYLOAD: name too long"))
            .andExpect(jsonPath("$.items[0].attempts").value(8));
        mvc.perform(get("/admin/events/tool-sync/deliveries?status=LOST").with(admin())).andExpect(status().isBadRequest());
    }

    @Test
    void retryAndReplayAreAuditedAndRetryOnlyAppliesToAFailedDelivery() throws Exception {
        ToolConnector c = connector();
        Organization org = organization();
        tenant(org, c, TenantSchemaStatus.READY);
        ToolDelivery failed = delivery(c, org, DeliveryStatus.FAILED, 8, "INTERNAL: db busy");
        ToolDelivery delivered = delivery(c, org, DeliveryStatus.DELIVERED, 1, null);

        mvc.perform(post("/admin/events/tool-sync/deliveries/" + delivered.getId() + "/retry").with(admin())).andExpect(status().isConflict());
        mvc.perform(post("/admin/events/tool-sync/deliveries/" + delivered.getId() + "/replay").with(admin()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Replay queued"));
        mvc.perform(post("/admin/events/tool-sync/deliveries/" + delivered.getId() + "/replay").with(admin()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.message").value(containsString("already waiting")));
        mvc.perform(post("/admin/events/tool-sync/deliveries/" + failed.getId() + "/retry").with(admin())).andExpect(status().isOk());
        ToolDelivery again = deliveries.findById(failed.getId()).orElseThrow();
        assertThat(again.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(again.getAttempts()).isZero();
        assertThat(again.getLastError()).isNull();
        assertThat(deliveries.findAll()).filteredOn(d -> d.getOrganizationId().equals(org.getId()) && d.getStatus() == DeliveryStatus.PENDING).hasSize(2);
        mvc.perform(post("/admin/events/tool-sync/deliveries/999999/replay").with(admin())).andExpect(status().isNotFound());

        assertThat(audited("TOOL_DELIVERY_RETRIED")).hasSize(1);
        assertThat(audited("TOOL_DELIVERY_REPLAYED")).as("both replay requests, queued or not").hasSize(2);
    }

    @Test
    void aToolCanBePausedAndResumedAndAnOrganizationStartedOrResynced() throws Exception {
        ToolConnector c = connector();
        Organization noSubscription = organization();
        Organization subscribed = organization();
        subscription(subscribed, SubscriptionStatus.ACTIVE, LocalDate.now().plusMonths(3));

        mvc.perform(post("/admin/events/tool-sync/connectors/" + c.getId() + "/pause").with(admin())).andExpect(status().isOk());
        assertThat(connectors.findById(c.getId()).orElseThrow().getStatus()).isEqualTo(ConnectorStatus.PAUSED);
        mvc.perform(post("/admin/events/tool-sync/connectors/" + c.getId() + "/resume").with(admin())).andExpect(status().isOk());
        assertThat(connectors.findById(c.getId()).orElseThrow().getStatus()).isEqualTo(ConnectorStatus.ACTIVE);

        mvc.perform(post("/admin/events/tool-sync/tenants/" + noSubscription.getId() + "/" + c.getId() + "/start").with(admin())).andExpect(status().isBadRequest());
        mvc.perform(post("/admin/events/tool-sync/tenants/" + subscribed.getId() + "/" + c.getId() + "/start").with(admin()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Provisioning requested"));
        assertThat(tenants.findByOrganizationIdAndProductId(subscribed.getId(), c.getProductId()).orElseThrow().getStatus()).isEqualTo(TenantSchemaStatus.PENDING);
        assertThat(deliveries.findAll()).filteredOn(d -> d.getOrganizationId().equals(subscribed.getId())).extracting(ToolDelivery::getEventType)
            .containsExactly("TenantProvisioningRequested");
        mvc.perform(post("/admin/events/tool-sync/tenants/999999/" + c.getId() + "/start").with(admin())).andExpect(status().isNotFound());

        assertThat(audited("TOOL_CONNECTOR_PAUSED")).hasSize(1);
        assertThat(audited("TOOL_CONNECTOR_RESUMED")).hasSize(1);
        assertThat(audited("TOOL_TENANT_STARTED")).hasSize(1);
    }

    @Test
    void reconcileReportsAnUnreachableToolWithoutChangingAnything() throws Exception {
        ToolConnector c = connector();
        Organization org = organization();
        tenant(org, c, TenantSchemaStatus.READY);
        tool.reset();
        tool.stop("thittam");

        mvc.perform(post("/admin/events/tool-sync/tenants/" + org.getId() + "/" + c.getId() + "/reconcile").with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reachable").value(false))
            .andExpect(jsonPath("$.inSync").value(false));
        tool.reset();

        assertThat(deliveries.count()).isZero();
        assertThat(audited("TOOL_TENANT_RECONCILED")).hasSize(1);
    }
}
