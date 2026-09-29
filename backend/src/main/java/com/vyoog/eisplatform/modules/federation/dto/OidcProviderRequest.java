package com.vyoog.eisplatform.modules.federation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create or update an OIDC provider. On update a blank {@code clientSecret}
 * keeps the stored one; on create it is required. Blank {@code scopes} means
 * {@code openid email profile}. */
public record OidcProviderRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 500) String issuerUrl,
    @NotBlank @Size(max = 255) String clientId,
    @Size(max = 2000) String clientSecret,
    @Size(max = 500) String scopes
) {
}
