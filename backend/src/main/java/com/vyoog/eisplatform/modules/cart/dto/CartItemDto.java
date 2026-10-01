package com.vyoog.eisplatform.modules.cart.dto;

import java.time.Instant;
import java.util.List;

/** {@code unitPrice} is the current catalog price; {@code unitPriceAtAdd} the
 * price when added or last confirmed (BR-5). {@code plans} are the product's
 * paid plans for "Change plan". */
public record CartItemDto(
    Long id,
    Long productId,
    String productName,
    String imageUrl,
    Long planId,
    String planName,
    String billingPeriod,
    long unitPrice,
    long unitPriceAtAdd,
    String currency,
    long amount,
    Instant addedAt,
    List<CartPlanOptionDto> plans
) {
}
