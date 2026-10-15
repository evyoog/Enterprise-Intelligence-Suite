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
    List<CartPlanOptionDto> plans,
    /** C100 SE-1 B: a plan's price is per seat for an organization, so an organization's item is {@code seats} x {@code unitPrice}; 1 for an individual. */
    int seats,
    /** C106: true when this product charges a seat add-on on top of its base price (amount = baseAmount + seatAddonAmount);
     * false means {@code amount} is exactly {@code unitPrice x seats}, as before this feature. */
    boolean seatPricingEnabled,
    long seatUnitPrice,
    long baseAmount,
    long seatAddonAmount
) {
}
