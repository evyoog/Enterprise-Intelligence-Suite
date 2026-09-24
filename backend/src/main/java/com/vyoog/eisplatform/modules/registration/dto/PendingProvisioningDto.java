package com.vyoog.eisplatform.modules.registration.dto;

/** One row in the platform admin's "needs a Keycloak user created manually"
 * queue — see AdminRegistrationService. {@code kind} is "INDIVIDUAL" or
 * "ORG_ADMIN" so the admin screen can tell the two apart. */
public record PendingProvisioningDto(
    Long customerId,
    String kind,
    String firstName,
    String lastName,
    String email,
    String organizationName
) {
}
