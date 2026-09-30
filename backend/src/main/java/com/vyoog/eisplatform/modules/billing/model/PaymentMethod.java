package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** A saved Razorpay-tokenized card or UPI handle. {@link #providerTokenRef}
 * is the ONLY thing that identifies the real payment instrument at Razorpay
 * — every other field here is display-only (BR-BIL-001: no card number, no
 * CVV, ever). */
@Entity
@Table(name = "payment_method")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    @Column(name = "owner_organization_id")
    private Long ownerOrganizationId;

    @Column(name = "provider_token_ref", nullable = false, length = 100)
    private String providerTokenRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentMethodType type;

    @Column(length = 40)
    private String network;

    @Column(length = 4)
    private String last4;

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(length = 100)
    private String issuer;

    @Column(name = "upi_masked", length = 100)
    private String upiMasked;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "consent_at")
    private Instant consentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentMethodStatus status = PaymentMethodStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public boolean isExpiredCard() {
        if (type != PaymentMethodType.CARD || expiryMonth == null || expiryYear == null) {
            return false;
        }
        Instant endOfExpiryMonth = java.time.YearMonth.of(expiryYear, expiryMonth)
            .atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneOffset.UTC).toInstant();
        return Instant.now().isAfter(endOfExpiryMonth);
    }
}
