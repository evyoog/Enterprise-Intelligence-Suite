package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** BR-7: amount > 0 and <= captured minus already refunded; reason required.
 * Admin-only (FRD Open question 6 assumes admins only). */
@Entity
@Table(name = "payment_refund")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "provider_refund_id", length = 100)
    private String providerRefundId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status = "PROCESSED";

    @Column(name = "requested_by_customer_id", nullable = false)
    private Long requestedByCustomerId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
