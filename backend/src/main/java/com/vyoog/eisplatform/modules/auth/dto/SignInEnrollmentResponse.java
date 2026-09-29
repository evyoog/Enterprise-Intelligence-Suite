package com.vyoog.eisplatform.modules.auth.dto;

import java.util.List;

/** C29: a sign-in completed by setting up an authenticator. Same token fields
 * as {@link TokenResponse}, plus the new recovery codes, shown once. */
public record SignInEnrollmentResponse(String accessToken, long expiresInSeconds, boolean mfaVerified,
                                       List<String> recoveryCodes) {
}
