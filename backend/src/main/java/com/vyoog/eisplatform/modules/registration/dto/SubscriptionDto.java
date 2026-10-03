package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionDto(
    Long id,
    Long productId,
    String productName,
    SubscriptionStatus status,
    Instant startedAt,
    Instant expiresAt,
    Long planId,
    String planName,
    /** REQ-SUB-003: seats (1 for individual subscriptions). */
    int quantity,
    /** REQ-SUB-004: renewed automatically on {@code expiresAt}. */
    boolean autoRenew
) {
}
