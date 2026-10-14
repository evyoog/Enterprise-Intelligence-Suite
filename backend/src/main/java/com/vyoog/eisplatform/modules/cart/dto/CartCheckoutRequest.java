package com.vyoog.eisplatform.modules.cart.dto;

/** C100 CA-2: the body of a checkout. {@code reason} is required when an organization member's cart becomes a purchase request. */
public record CartCheckoutRequest(String reason) {
}
