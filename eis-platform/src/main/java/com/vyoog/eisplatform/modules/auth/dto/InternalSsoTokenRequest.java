package com.vyoog.eisplatform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** targetClientId is the CALLING app's own Keycloak client id — the audience
 * the returned token should be minted for (see ImpersonationExchangeService). */
public record InternalSsoTokenRequest(@NotBlank String ssoSessionId, @NotBlank String targetClientId) {
}
