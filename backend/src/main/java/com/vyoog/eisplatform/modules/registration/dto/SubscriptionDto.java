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
    boolean autoRenew,
    /** C97 SC-1: a downgrade chosen by the customer, taking effect at the next renewal; null when none is scheduled. */
    Long pendingPlanId,
    String pendingPlanName,
    /** C100 SE-1 B: a seat decrease taking effect at the next renewal; null when none is scheduled. */
    Integer pendingQuantity,
    /** C100 RR-1 B: while set and in the future, the subscription is read-only (its term has ended). */
    Instant graceEndsAt,
    /** C102: a free trial; {@code expiresAt} is when it ends. */
    boolean trial
) {
}
