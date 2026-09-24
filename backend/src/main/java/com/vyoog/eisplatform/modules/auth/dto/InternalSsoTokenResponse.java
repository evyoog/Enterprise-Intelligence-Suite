package com.vyoog.eisplatform.modules.auth.dto;

/** Already fully scoped to the CALLING app's own client — the receiving
 * backend just stores refreshToken as its own refresh cookie and returns
 * accessToken to its frontend, exactly like a normal login. */
public record InternalSsoTokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {
}
