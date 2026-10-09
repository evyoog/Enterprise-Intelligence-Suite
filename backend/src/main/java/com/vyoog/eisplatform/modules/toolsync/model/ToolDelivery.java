package com.vyoog.eisplatform.modules.toolsync.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One message to one tool for one organization (REQ-INT-003, table {@code tool_delivery}): the per-destination status of a platform
 * event. It names WHAT changed (aggregate type and id); the message itself is built from the current state when it is sent, so a retry
 * or a replay never sends something old (BR-SYN-018).
 */
@Entity
@Table(name = "tool_delivery", uniqueConstraints = @UniqueConstraint(name = "uq_tool_delivery", columnNames = {"event_id", "tool_connector_id", "organization_id"}))
@Getter
@Setter
public class ToolDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The platform event this came from, or a fresh id for a resync or replay. */
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "tool_connector_id", nullable = false)
    private Long toolConnectorId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "tenant_ref", nullable = false, length = 100)
    private String tenantRef;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt = Instant.now();

    @Column(name = "last_error", length = 1000)
    private String lastError;

    /** The version of the aggregate when the delivery was made (the tombstone's version for a deleted node). */
    @Column(name = "aggregate_version")
    private Long aggregateVersion;

    /** The version of the aggregate in the message that was accepted. */
    @Column(name = "sent_version")
    private Long sentVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "delivered_at")
    private Instant deliveredAt;
}
