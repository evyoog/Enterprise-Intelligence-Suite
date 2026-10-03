package com.vyoog.eisplatform.modules.integration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * REQ-INT-002.1/.2 (C62): one platform event, written in the same database
 * transaction as the business change it describes. The dispatcher delivers
 * PENDING rows to the in-application handlers registered for the type.
 */
@Entity
@Table(name = "outbox_event", indexes = {
    @Index(name = "idx_outbox_event_status_next", columnList = "status, next_attempt_at"),
    @Index(name = "idx_outbox_event_aggregate", columnList = "aggregate_type, aggregate_id, occurred_at"),
    @Index(name = "idx_outbox_event_type", columnList = "event_type")
})
@Getter
@Setter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
