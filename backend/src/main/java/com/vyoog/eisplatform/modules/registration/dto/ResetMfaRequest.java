package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** C30: the account whose two-factor authentication a platform admin resets. */
public record ResetMfaRequest(@NotBlank @Email String email) {
}
