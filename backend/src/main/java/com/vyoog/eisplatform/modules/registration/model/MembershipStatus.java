package com.vyoog.eisplatform.modules.registration.model;

/** Shared by OrganizationMember and OrganizationProductAccess. INACTIVE means
 * "removed but kept for history" — a row here is never deleted (see
 * OrganizationSelfService#removeMember), so a former member's audit trail
 * and seat history survive their removal. */
public enum MembershipStatus {
    ACTIVE,
    INACTIVE
}
