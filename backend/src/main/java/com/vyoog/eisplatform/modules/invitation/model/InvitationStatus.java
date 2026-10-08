package com.vyoog.eisplatform.modules.invitation.model;

/** REQ-TEN-008.4 lifecycle; records are never deleted. */
public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED,
    REVOKED
}
