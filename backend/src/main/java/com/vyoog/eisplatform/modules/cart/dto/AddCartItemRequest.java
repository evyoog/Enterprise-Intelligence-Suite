package com.vyoog.eisplatform.modules.cart.dto;

import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(@NotNull Long productId, @NotNull Long planId) {
}
