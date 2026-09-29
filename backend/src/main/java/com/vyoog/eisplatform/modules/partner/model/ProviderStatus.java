package com.vyoog.eisplatform.modules.partner.model;

/** 14.01.01 Provider Lifecycle (sprint 2027.2.1). A provider applies
 * (REGISTERED), then a platform admin verifies and approves it in turn —
 * each its own gate, made at its own time — before finally activating it.
 * REJECTED is reachable from any non-terminal stage and is terminal. */
public enum ProviderStatus {
    REGISTERED,
    VERIFIED,
    APPROVED,
    ACTIVE,
    REJECTED
}
