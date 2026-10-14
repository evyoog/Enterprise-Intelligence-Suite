package com.vyoog.eisplatform.modules.cart.dto;

import com.vyoog.eisplatform.modules.billing.dto.TaxLineDto;

import java.util.List;

/** C59 (REQ-MKT-003.3): the cart page. Amounts in the currency's smallest
 * unit. {@code continueAs} is {@code INDIVIDUAL} or {@code ORGANIZATION_MEMBER},
 * decided on the server from the caller's membership. */
public record CartDto(
    List<CartItemDto> items,
    int itemCount,
    String currency,
    long subtotal,
    List<TaxLineDto> taxLines,
    boolean taxCalculatedAtPayment,
    long total,
    /** INDIVIDUAL, ORGANIZATION_BUYER (an organization admin or a person with Manage billing buys directly, C100 CA-2) or ORGANIZATION_MEMBER
     * (the cart becomes a purchase request with a reason). */
    String continueAs,
    /** C100 (PB-1 note): the applied coupon code and its discount (0 with {@code couponMessage} when it can no longer be used); the total is net of it. */
    String couponCode,
    long discount,
    String couponMessage
) {
}
