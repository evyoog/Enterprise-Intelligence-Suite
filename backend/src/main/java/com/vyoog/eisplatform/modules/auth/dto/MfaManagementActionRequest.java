package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Shared shape for the two highest-value MFA actions (disable, regenerate
 * recovery codes) — both require the current password AND a currently-valid
 * factor (a fresh TOTP code, or a recovery code for someone who's lost their
 * authenticator but still needs to disable rather than stay locked out). */
public record MfaManagementActionRequest(@NotBlank String currentPassword, @NotBlank String code) {
}
