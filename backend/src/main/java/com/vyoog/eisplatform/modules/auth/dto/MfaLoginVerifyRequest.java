package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code code} is either a 6-digit Platform TOTP code or a recovery code —
 * see PlatformMfaService#verifyLoginChallenge for how the two are told apart. */
public record MfaLoginVerifyRequest(@NotBlank String challengeId, @NotBlank String code) {
}
