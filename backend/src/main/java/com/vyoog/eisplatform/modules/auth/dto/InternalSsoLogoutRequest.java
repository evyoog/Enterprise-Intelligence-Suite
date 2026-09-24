package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record InternalSsoLogoutRequest(@NotBlank String ssoSessionId) {
}
