package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolDeliveryRepository;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The platform side of the synchronization metrics (REQ-INT-003, all defined here; they appear under {@code /actuator/metrics}).
 * Names match the tool side so one dashboard serves both.
 * <ul>
 *   <li>{@code platformsync.delivery{tool,status}} — send attempts by result: {@code delivered}, {@code retry}, {@code unreachable}, {@code failed};</li>
 *   <li>{@code platformsync.delivery.failed{tool}} — messages that became FAILED (alert: any increase);</li>
 *   <li>{@code platformsync.lag.seconds{tool}} — from the change to its delivery;</li>
 *   <li>gauges {@code platformsync.delivery.waiting{tool}}, {@code platformsync.delivery.failed.open{tool}} and
 *       {@code platformsync.lag.oldest.seconds{tool}} — what waits now, what is FAILED now, and the age of the oldest waiting message (alert: above 300 s);</li>
 *   <li>{@code platformsync.drift.detected{tool,type}} — objects a tool lacked or held at an older version when reconcile compared;</li>
 *   <li>{@code platformsync.entitlement.denied{reason}} — {@code get_entitlement} answers that said no, by reason.</li>
 * </ul>
 * Does nothing when there is no meter registry.
 */
@Component
public class ToolSyncMetrics {

    private final MeterRegistry registry;
    private final ToolConnectorRepository connectors;
    private final ToolDeliveryRepository deliveries;
    private final Map<String, AtomicLong> gauges = new ConcurrentHashMap<>();

    public ToolSyncMetrics(ObjectProvider<MeterRegistry> registry, ToolConnectorRepository connectors, ToolDeliveryRepository deliveries) {
        this.registry = registry.getIfAvailable();
        this.connectors = connectors;
        this.deliveries = deliveries;
    }

    public void deliveryAttempt(String tool, String status) {
        if (registry != null) {
            registry.counter("platformsync.delivery", "tool", tool, "status", status).increment();
        }
    }

    public void deliveryFailed(String tool) {
        if (registry != null) {
            registry.counter("platformsync.delivery.failed", "tool", tool).increment();
        }
    }

    public void delivered(String tool, Instant createdAt) {
        deliveryAttempt(tool, "delivered");
        if (registry != null && createdAt != null) {
            DistributionSummary.builder("platformsync.lag.seconds").tag("tool", tool).baseUnit("seconds").publishPercentiles(0.5, 0.95)
                .register(registry).record(Math.max(0, Duration.between(createdAt, Instant.now()).toMillis() / 1000.0));
        }
    }

    public void drift(String tool, String type, int objects) {
        if (registry != null && objects > 0) {
            registry.counter("platformsync.drift.detected", "tool", tool, "type", type).increment(objects);
        }
    }

    public void entitlementDenied(String reason) {
        if (registry != null) {
            registry.counter("platformsync.entitlement.denied", "reason", reason).increment();
        }
    }

    /** Reads what waits and what is FAILED now (called every 30 s by {@code ToolSyncMetricsJob}). */
    public void refreshGauges() {
        if (registry == null) {
            return;
        }
        List<ToolConnector> all = connectors.findAll();
        Map<Long, long[]> open = new ConcurrentHashMap<>();
        for (Object[] row : deliveries.countOpenByTenant()) {
            long[] counts = open.computeIfAbsent((Long) row[0], k -> new long[2]);
            counts[row[2] == DeliveryStatus.PENDING ? 0 : 1] += (Long) row[3];
        }
        for (ToolConnector c : all) {
            long[] counts = open.getOrDefault(c.getId(), new long[2]);
            gauge("platformsync.delivery.waiting", c.getProductCode()).set(counts[0]);
            gauge("platformsync.delivery.failed.open", c.getProductCode()).set(counts[1]);
            Instant oldest = deliveries.oldestPending(c.getId());
            gauge("platformsync.lag.oldest.seconds", c.getProductCode()).set(oldest == null ? 0 : Math.max(0, Duration.between(oldest, Instant.now()).getSeconds()));
        }
    }

    private AtomicLong gauge(String name, String tool) {
        return gauges.computeIfAbsent(name + "|" + tool, k -> {
            AtomicLong holder = new AtomicLong();
            Gauge.builder(name, holder, AtomicLong::get).tag("tool", tool).register(registry);
            return holder;
        });
    }
}
