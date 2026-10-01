package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** C55: one row of the checkout's "Your order" panel. {@code quantity} is
 * null unless the subscription actually carries one — seat/quantity rules
 * are undecided (D14) and are not invented here. */
public record CheckoutItemDto(
    Long productId,
    String productName,
    String imageUrl,
    String planName,
    String billingPeriod,
    Instant periodStart,
    Instant periodEnd,
    long amount,
    Integer quantity
) {
}
