package com.vyoog.eisplatform.modules.authorization.model;

/**
 * Lifecycle of one {@link PrivilegedAccessRequest}. Deliberately no EXPIRED
 * value here — a request that ran out its clock is still, factually,
 * APPROVED; "expired" is a computed fact (status == APPROVED &amp;&amp;
 * expiresAt is in the past), checked at evaluation/listing time, not a
 * stored state a background job would need to flip. This app has no
 * scheduler anywhere else either — lazy expiration matches the existing
 * pattern for token/session expiry checks in this codebase.
 */
public enum PrivilegedAccessStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REVOKED
}
