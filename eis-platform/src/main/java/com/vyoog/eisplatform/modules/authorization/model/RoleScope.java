package com.vyoog.eisplatform.modules.authorization.model;

/**
 * Which "level" a {@link Role} operates at — deliberately mirrors this
 * phase's own instruction to keep organization-level roles (e.g. Organization
 * Admin) and application-level roles (e.g. PMS Admin) distinct, never
 * assuming one implies the other.
 *
 * There is no APPLICATION value here on purpose: an application-scoped role
 * (e.g. "PMS_ADMIN") is a free string on {@code OrganizationProductAccess.productRole}
 * (see that entity's own javadoc) — driven by each product's own catalog, not
 * something eis-platform defines or owns the permission set for. Formalizing
 * PMS's own internal permissions here would mean eis-platform speaking for an
 * authorization model it doesn't control.
 */
public enum RoleScope {
    /** Platform-wide roles (today: just PLATFORM_ADMIN) — who holds one is
     * still decided by Keycloak's client-role claim (see AuthorizationService's
     * own javadoc on why that source wasn't changed), but what a role in this
     * scope is ALLOWED to do is real, DB-editable data via {@link Role#getPermissions()}. */
    PLATFORM,
    /** Per-organization roles (ORG_ADMIN/ORG_MEMBER) — who holds one is fully
     * DB-driven already (OrganizationMember.orgRole, see Phase 2), so this
     * scope's permission checks are entirely database-backed, no Keycloak
     * involvement at all. */
    ORGANIZATION
}
