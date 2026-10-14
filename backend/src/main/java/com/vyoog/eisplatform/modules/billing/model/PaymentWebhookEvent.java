package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** BR-6: each Razorpay webhook event is processed at most once, identified
 * by {@link #providerEventId}. */
@Entity
@Table(name = "payment_webhook_event", uniqueConstraints = @UniqueConstraint(name = "uq_payment_webhook_event_provider", columnNames = {"provider", "provider_event_id"}))
@Getter
@Setter
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** C98: the provider whose webhook this was; an event id is unique per provider. */
    @Column(nullable = false, length = 30)
    private String provider = "RAZORPAY";

    @Column(name = "provider_event_id", nullable = false, length = 100)
    private String providerEventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "payload_summary", length = 500)
    private String payloadSummary;
}
