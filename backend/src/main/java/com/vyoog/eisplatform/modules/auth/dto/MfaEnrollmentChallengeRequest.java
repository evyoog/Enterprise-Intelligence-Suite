package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** C29: starts authenticator set-up for a sign-in parked on an ENROLL challenge. */
public record MfaEnrollmentChallengeRequest(@NotBlank String challengeId) {
}
