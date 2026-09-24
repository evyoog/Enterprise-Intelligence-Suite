package com.vyoog.eisplatform.modules.registration.model;

/** Lifecycle of a registration itself — deliberately separate from
 * SubscriptionStatus (a registration completing is not the same thing as a
 * product being subscribed). Shared by Customer (individual flow) and
 * Organization (org flow) rather than two parallel enums. */
public enum RegistrationStatus {
    PENDING_EMAIL_VERIFICATION,
    EMAIL_VERIFIED,
    PENDING_SUBSCRIPTION,
    COMPLETED,
    CANCELLED,
    EXPIRED
}
