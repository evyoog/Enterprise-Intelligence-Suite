package com.vyoog.eisplatform.modules.cart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One purchase-validation issue (BR-6). {@code code}: NOT_AVAILABLE
 * ({@code reason} PRODUCT or PLAN), PRICE_CHANGED ({@code oldPrice},
 * {@code newPrice}), ALREADY_SUBSCRIBED, MISSING_DEPENDENCY
 * ({@code requiredProductId}, {@code requiredProductName}) or
 * CURRENCY_MISMATCH (the item's currency differs from the cart's). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartIssueDto(
    Long itemId,
    String code,
    String reason,
    Long oldPrice,
    Long newPrice,
    Long requiredProductId,
    String requiredProductName
) {
    public static CartIssueDto of(Long itemId, String code) {
        return new CartIssueDto(itemId, code, null, null, null, null, null);
    }
}
