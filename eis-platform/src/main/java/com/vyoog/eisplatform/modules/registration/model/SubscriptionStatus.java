package com.vyoog.eisplatform.modules.registration.model;

/** No payment integration exists yet (see ProductSubscriptionService) —
 * these states are modeled now so a real payment step can be inserted later
 * without a schema or API-shape change: PENDING_SUBSCRIPTION is where a
 * future "awaiting payment" state would sit. */
public enum SubscriptionStatus {
    PENDING_SUBSCRIPTION,
    ACTIVE,
    CANCELLED,
    EXPIRED
}
