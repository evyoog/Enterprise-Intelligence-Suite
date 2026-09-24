package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Re-authentication before a state-changing MFA action begins — see
 * PlatformMfaService's own javadoc for why every enroll/disable/regenerate
 * call requires the current password, verified fresh against Keycloak. */
public record MfaEnrollRequest(@NotBlank String currentPassword) {
}
