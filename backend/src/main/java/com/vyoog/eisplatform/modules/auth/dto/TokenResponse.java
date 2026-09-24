package com.vyoog.eisplatform.modules.auth.dto;

/** Deliberately no refreshToken field — that token only ever travels as an
 * HttpOnly cookie. {@code mfaVerified} (Phase 7) reports whether Keycloak's
 * own {@code amr} claim on this specific token shows an OTP challenge was
 * completed — informational on every call, but only ever enforced (see
 * OrganizationMfaRequiredException) at fresh login. */
public record TokenResponse(String accessToken, long expiresInSeconds, boolean mfaVerified) {
}
