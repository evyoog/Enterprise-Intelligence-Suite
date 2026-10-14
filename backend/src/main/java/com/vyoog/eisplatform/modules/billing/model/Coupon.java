package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** C100 (PB-1 note): a coupon the platform administrator configures. Usable while it is enabled and before its expiry; the administrator can disable
 * it earlier. A PERCENT coupon takes {@code percentOff} (1 to 99) off the cart; a FIXED coupon takes {@code amountOff} minor units off a cart in
 * its {@code currency}. */
@Entity
@Table(name = "coupon")
@Getter
@Setter
public class Coupon {

    public enum Type { PERCENT, FIXED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private Type discountType;

    @Column(name = "percent_off", precision = 5, scale = 2)
    private BigDecimal percentOff;

    @Column(name = "amount_off")
    private Long amountOff;

    @Column(length = 10)
    private String currency;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "redeemed_count", nullable = false)
    private int redeemedCount = 0;

    @Column(name = "created_by_customer_id")
    private Long createdByCustomerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
