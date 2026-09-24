package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code totp} is optional — present only on the second submission of a
 * login that Keycloak's first attempt flagged as needing an OTP code (see
 * MfaChallengeRequiredException). */
public record LoginRequest(@NotBlank String email, @NotBlank String password, String totp) {
}
