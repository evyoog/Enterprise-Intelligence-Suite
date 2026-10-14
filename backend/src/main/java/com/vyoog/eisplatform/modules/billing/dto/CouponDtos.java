package com.vyoog.eisplatform.modules.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** C100 (PB-1 note): the coupon API. {@code status}: ACTIVE, DISABLED or EXPIRED. Amounts are in the currency's smallest unit. */
public final class CouponDtos {

    private CouponDtos() {
    }

    public record Coupon(Long id, String code, String description, String discountType, BigDecimal percentOff, Long amountOff, String currency,
                         Instant expiresAt, boolean enabled, String status, int redeemedCount, Instant createdAt) {
    }

    /** {@code discountType} PERCENT (1 to 99, {@code percentOff}) or FIXED ({@code amountOff} and {@code currency}). */
    public record Save(String code, String description, String discountType, BigDecimal percentOff, Long amountOff, String currency, Instant expiresAt) {
    }

    public record SetEnabled(boolean enabled) {
    }

    /** What a cart shows when a code is applied or removed. */
    public record Applied(String code, String description, String discountType, BigDecimal percentOff, long discount) {
    }
}
