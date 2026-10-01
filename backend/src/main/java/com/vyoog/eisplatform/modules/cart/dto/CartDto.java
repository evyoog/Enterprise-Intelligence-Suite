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
    String continueAs
) {
}
