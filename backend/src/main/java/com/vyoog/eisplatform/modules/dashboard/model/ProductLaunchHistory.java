package com.vyoog.eisplatform.modules.dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * C104 / AN-1 (16 Analytics & data platform): one row per launch, kept for
 * trend history — unlike {@link ProductUsage}, which holds only the latest
 * timestamp and a cumulative count. Written alongside it, never replacing
 * it; {@code ProductLaunchHistoryCleanupJob} deletes rows older than 13
 * months.
 */
@Entity
@Table(name = "product_launch_history")
@Getter
@Setter
public class ProductLaunchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "launched_at", nullable = false)
    private Instant launchedAt = Instant.now();
}
