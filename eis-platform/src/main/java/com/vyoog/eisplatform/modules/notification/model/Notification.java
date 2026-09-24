package com.vyoog.eisplatform.modules.notification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 18: one real, already-occurred event a customer is told about — this
 * table IS the notification center's own history, not a derived/ephemeral
 * view (unlike DashboardService's alerts, which are recomputed on every
 * request from live state). Deliberately keyed by the plain {@code
 * customer_id}, same loose-coupling convention as FavoriteProduct/
 * ProductUsage/SearchHistoryEntry — no FK to whatever entity the event was
 * about, since that varies per category and isn't needed to render the
 * notification itself.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationSeverity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    private Instant readAt;
}
