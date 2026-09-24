package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record MfaVerifyEnrollmentRequest(@NotBlank String code) {
}
