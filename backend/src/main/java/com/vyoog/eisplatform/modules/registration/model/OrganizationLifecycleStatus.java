package com.vyoog.eisplatform.modules.registration.model;

/**
 * REQ-TEN-001 (05.01.01 Organization Lifecycle): whether an organization may
 * be used, kept separate from {@link RegistrationStatus}, which only tracks
 * how far self-registration got. SUSPENDED and CLOSED both disable every
 * active member's Keycloak login; both can be reversed with Activate.
 * Nothing here ever deletes a row.
 */
public enum OrganizationLifecycleStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED
}
