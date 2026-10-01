package com.vyoog.eisplatform.modules.cart.dto;

/** Either {@code planId} (change plan, same product only — BR-4) or
 * {@code confirmPrice: true} (accept a changed price — BR-7). */
public record UpdateCartItemRequest(Long planId, Boolean confirmPrice) {
}
