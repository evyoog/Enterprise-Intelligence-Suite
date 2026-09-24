package com.vyoog.eisplatform.modules.registration.model;

/** An organization-scoped role — never confused with a Keycloak client role
 * (like the existing "ADMIN" catalog-management role) or a product-specific
 * role (PMS_ADMIN etc., stored as a free string on OrganizationProductAccess
 * since the set of products/roles is open-ended and backend-driven). */
public enum OrgRole {
    ORG_ADMIN,
    MEMBER
}
