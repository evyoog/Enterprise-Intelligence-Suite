package com.vyoog.eisplatform.modules.product.model;

/**
 * 02.05.02.01 Configure currency (sprint 2026.4.1). A fixed ISO 4217 subset,
 * not a configurable list — nothing in the source documents specifies which
 * currencies the platform must support, and no exchange-rate or multi-currency
 * billing engine exists yet (that is 08 Billing & Payments, a later sprint),
 * so this only labels a plan's price; it does not convert anything.
 */
public enum Currency {
    USD,
    EUR,
    GBP,
    INR
}
