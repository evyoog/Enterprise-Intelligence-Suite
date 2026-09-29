package com.vyoog.eisplatform.modules.registration.model;

/** Shared by OrganizationMember and OrganizationProductAccess. INACTIVE means
 * "removed but kept for history" — a row here is never deleted (see
 * OrganizationSelfService#removeMember), so a former member's audit trail
 * and seat history survive their removal.
 *
 * <p>SUSPENDED (05.03.01, sprint 2026.4.1) is a distinct, reversible state
 * from INACTIVE: a suspended member frees their seat and cannot sign in
 * (see CurrentCustomerResolver / resolveMembership, which only resolves an
 * ACTIVE row) but can be reactivated by an admin — see
 * OrganizationMemberService#reactivateMember. INACTIVE (remove) has no
 * reactivation path; it is deliberately one-way. */
public enum MembershipStatus {
    ACTIVE,
    SUSPENDED,
    INACTIVE
}
