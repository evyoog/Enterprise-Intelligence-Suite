package com.vyoog.eisplatform.modules.authorization.model;

/** One lifecycle transition on a {@link PrivilegedAccessRequest} — the
 * "audit trail" requirement, kept as its own append-only table rather than
 * inferred from the request row's current fields, since a row that's been
 * updated (e.g. PENDING -> APPROVED) no longer shows that it was ever PENDING. */
public enum PrivilegedAccessEventType {
    REQUESTED,
    APPROVED,
    REJECTED,
    REVOKED
}
