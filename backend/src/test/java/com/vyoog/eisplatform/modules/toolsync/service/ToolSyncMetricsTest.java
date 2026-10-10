package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.integration.service.OutboxDispatcher;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.toolsync.config.ToolSyncProperties;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Phase 8 metrics (TC-INT-061): delivery results, FAILED, lag, gauges for what waits, drift and entitlement refusals. */
class ToolSyncMetricsTest extends ToolSyncTestBase {

    @Autowired private MeterRegistry registry;
    @Autowired private OutboxDispatcher dispatcher;
    @Autowired private ToolDeliveryService delivery;
    @Autowired private ToolSyncMetrics metrics;
    @Autowired private ToolReconcileService reconcile;
    @Autowired private ToolInboundService inbound;
    @Autowired private FakeToolGateway tool;
    @Autowired private ToolSyncProperties props;

    @BeforeEach
    void fresh() {
        tool.reset();
        ReflectionTestUtils.setField(props, "maxAttempts", 2);
    }

    private double count(String name, String... tags) {
        var c = registry.find(name).tags(tags).counter();
        return c == null ? 0 : c.count();
    }

    private Organization connected(ToolConnector c) {
        Organization org = organization();
        TenantAppSchema t = new TenantAppSchema();
        t.setOrganizationId(org.getId());
        t.setProductId(c.getProductId());
        t.setTenantRef(String.valueOf(org.getId()));
        t.setStatus(TenantSchemaStatus.READY);
        tenants.save(t);
        subscription(org, SubscriptionStatus.ACTIVE, LocalDate.now().plusMonths(3));
        return org;
    }

    private void run() {
        for (int i = 0; i < 3; i++) {
            dispatcher.dispatchDue();
        }
        deliveries.findAll().stream().filter(d -> d.getStatus().name().equals("PENDING")).forEach(d -> { d.setNextAttemptAt(Instant.now().minusSeconds(1)); deliveries.save(d); });
        delivery.deliverDue();
    }

    @Test
    void aDeliveredMessageCountsAndRecordsItsLagAndAFailedOneCountsAsFailed() {
        ToolConnector c = connector();
        Organization org = connected(c);
        double deliveredBefore = count("platformsync.delivery", "tool", "thittam", "status", "delivered");
        double failedBefore = count("platformsync.delivery.failed", "tool", "thittam");
        run();
        assertThat(count("platformsync.delivery", "tool", "thittam", "status", "delivered")).isGreaterThan(deliveredBefore);
        assertThat(registry.find("platformsync.lag.seconds").tag("tool", "thittam").summary()).isNotNull();

        org.setCity("Pune");
        organizations.save(org);
        tool.answer("upsert_organization", ToolResult.retry("INTERNAL", "busy"), ToolResult.retry("INTERNAL", "busy"));
        run();
        run();

        assertThat(count("platformsync.delivery", "tool", "thittam", "status", "retry")).isGreaterThanOrEqualTo(1);
        assertThat(count("platformsync.delivery.failed", "tool", "thittam")).isEqualTo(failedBefore + 1);
    }

    @Test
    void theGaugesShowWhatWaitsWhatIsFailedAndHowOldTheOldestWaitingMessageIs() {
        ToolConnector c = connector();
        Organization org = connected(c);
        tool.stop("thittam");
        dispatcher.dispatchDue();
        delivery.deliverDue();           // tried once, now waiting for its retry

        metrics.refreshGauges();

        assertThat(registry.get("platformsync.delivery.waiting").tag("tool", "thittam").gauge().value()).isGreaterThanOrEqualTo(1.0);
        assertThat(registry.get("platformsync.delivery.failed.open").tag("tool", "thittam").gauge().value()).isEqualTo(0.0);
        assertThat(registry.get("platformsync.lag.oldest.seconds").tag("tool", "thittam").gauge().value()).isGreaterThanOrEqualTo(0.0);
        assertThat(org.getId()).isNotNull();
    }

    @Test
    void driftFoundByReconcileAndEntitlementRefusalsAreCountedByReason() {
        ToolConnector c = connector();
        Organization org = connected(c);
        run();
        tool.forget("thittam", String.valueOf(org.getId()), "Organization", String.valueOf(org.getId()));
        double driftBefore = count("platformsync.drift.detected", "tool", "thittam", "type", "Organization");

        reconcile.reconcile(org.getId(), c.getId(), true);

        assertThat(count("platformsync.drift.detected", "tool", "thittam", "type", "Organization")).isEqualTo(driftBefore + 1);

        double deniedBefore = count("platformsync.entitlement.denied", "reason", "NOT_A_MEMBER");
        Map<String, Object> args = new HashMap<>();
        args.put("tenantRef", String.valueOf(org.getId()));
        args.put("sub", UUID.randomUUID().toString());
        args.put("productCode", "thittam");
        inbound.getEntitlement("thittam-sync", args);
        assertThat(count("platformsync.entitlement.denied", "reason", "NOT_A_MEMBER")).isEqualTo(deniedBefore + 1);
    }
}
