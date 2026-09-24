package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;

/** {@code subscriptionStatus} is null when this individual has never
 * subscribed to this product at all — distinct from any of the real
 * SubscriptionStatus values, which all describe an existing subscription's
 * own lifecycle. */
public record MyProductDto(
    Long productId,
    String productName,
    String category,
    SubscriptionStatus subscriptionStatus
) {
}
