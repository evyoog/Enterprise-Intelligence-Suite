package com.vyoog.eisplatform.modules.billing.model;

import com.vyoog.eisplatform.modules.product.model.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

/** One attempt to pay an invoice through Razorpay. {@link #providerPaymentId}
 * is null until Razorpay Checkout returns a result (BR-5: only a verified
 * result is trusted). No card data is ever stored here (BR-BIL-001) — only
 * display fields (network, last 4). */
@Entity
@Table(name = "payment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(nullable = false, length = 20)
    private String provider = "RAZORPAY";

    @Column(name = "provider_order_id", length = 100)
    private String providerOrderId;

    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PaymentStatus status = PaymentStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(nullable = false)
    private long amount;

    @Column(name = "refunded_amount", nullable = false)
    private long refundedAmount = 0;

    @Column(name = "method_type", length = 20)
    private String methodType;

    @Column(name = "method_network", length = 40)
    private String methodNetwork;

    @Column(name = "method_last4", length = 4)
    private String methodLast4;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "captured_at")
    private Instant capturedAt;

    /** C55 (REQ-BIL-001.20): set only for a payment an admin recorded by
     * hand ({@link #provider} {@code OFFLINE}, {@link #methodType} {@code OFFLINE}). */
    @Enumerated(EnumType.STRING)
    @Column(name = "offline_method", length = 20)
    private OfflinePaymentMethod offlineMethod;

    @Column(name = "offline_reference", length = 100)
    private String offlineReference;

    @Column(name = "received_on")
    private LocalDate receivedOn;

    @Column(name = "recorded_by_customer_id")
    private Long recordedByCustomerId;

    @Column(length = 500)
    private String note;
}
