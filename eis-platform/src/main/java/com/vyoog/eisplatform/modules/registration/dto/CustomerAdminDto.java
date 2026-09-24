package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;

import java.time.Instant;

/** Platform-admin view of an individual customer (never joined any
 * organization — see AdminRegistrationService#listAllIndividuals for how
 * that's distinguished from an org's own admin/members). */
public record CustomerAdminDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    String mobile,
    String country,
    String companyName,
    String jobTitle,
    String industry,
    RegistrationStatus status,
    boolean keycloakLinked,
    Instant createdAt
) {
}
