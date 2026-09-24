package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionDto(
    Long id,
    Long productId,
    String productName,
    SubscriptionStatus status,
    Instant startedAt,
    Instant expiresAt
) {
}
