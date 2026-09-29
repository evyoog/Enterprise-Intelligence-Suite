package com.vyoog.eisplatform.modules.registration.model;

/** No payment integration exists yet (see ProductSubscriptionService) —
 * these states are modeled now so a real payment step can be inserted later
 * without a schema or API-shape change: PENDING_SUBSCRIPTION is where a
 * future "awaiting payment" state would sit.
 *
 * <p>SUSPENDED (07.01.01 Suspend subscription, sprint 2026.4.3) is a
 * distinct, reversible state from CANCELLED — see
 * SubscriptionService#reactivateSubscription. CANCELLED has no reactivation
 * path; it is deliberately one-way, same reasoning as
 * {@link com.vyoog.eisplatform.modules.registration.model.MembershipStatus#INACTIVE}. */
public enum SubscriptionStatus {
    PENDING_SUBSCRIPTION,
    ACTIVE,
    SUSPENDED,
    CANCELLED,
    EXPIRED
}
