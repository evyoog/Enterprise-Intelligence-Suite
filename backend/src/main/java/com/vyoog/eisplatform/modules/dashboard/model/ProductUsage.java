package com.vyoog.eisplatform.modules.dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 16: real, if minimal, usage data — one row per (customer, product),
 * updated every time the frontend records a Launch click (see
 * DashboardController#recordLaunch). This is the honest basis for both
 * "recently used" (order by {@link #lastLaunchedAt}) and "frequently used"
 * (order by {@link #launchCount}) — no richer usage metric (session length,
 * feature-level activity, etc.) exists anywhere in this app, and none is
 * fabricated here.
 */
@Entity
@Table(name = "product_usage")
@Getter
@Setter
public class ProductUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "last_launched_at", nullable = false)
    private Instant lastLaunchedAt = Instant.now();

    @Column(name = "launch_count", nullable = false)
    private long launchCount = 0;
}
