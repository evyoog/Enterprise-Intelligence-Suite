package com.vyoog.eisplatform.common.exception;

/** Keycloak's own password grant already succeeded (and any Keycloak-native
 * OTP it demanded was already satisfied), but this customer also has
 * Platform TOTP enabled — the login is not complete until
 * {@code POST /auth/mfa/verify} is called with the carried
 * {@link #challengeId}. Distinct from {@link MfaChallengeRequiredException}
 * (that one is Keycloak's own OTP demand, mid-grant); this one is the
 * platform's own second factor, evaluated only after a real Keycloak grant
 * already succeeded (see GlobalExceptionHandler's {@code platformMfaRequired}
 * flag). */
public class PlatformMfaChallengeRequiredException extends RuntimeException {

    private final String challengeId;

    public PlatformMfaChallengeRequiredException(String message, String challengeId) {
        super(message);
        this.challengeId = challengeId;
    }

    public String getChallengeId() {
        return challengeId;
    }
}
