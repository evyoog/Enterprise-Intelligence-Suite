package com.vyoog.eisplatform.modules.partner.model;

/** 14.01.02 Contracts (sprint 2027.2.1). Flipped from ACTIVE to EXPIRED by
 * the scheduled {@code ContractExpiryJob}, not computed ad hoc on read —
 * same pattern as {@code SubscriptionStatus}'s own auto-expiry (07.04.01). */
public enum ContractStatus {
    ACTIVE,
    EXPIRED
}
