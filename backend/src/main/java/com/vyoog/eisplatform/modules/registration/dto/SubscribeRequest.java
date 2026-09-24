package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(@NotNull Long productId) {
}
