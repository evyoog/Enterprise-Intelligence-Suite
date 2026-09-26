package com.vyoog.eisplatform.common.exception;

/** C29: sign-in succeeded, but the member's organization requires MFA and
 * they have no authenticator yet. The frontend uses {@link #getChallengeId()}
 * with {@code POST /auth/mfa/enroll/start} and {@code /complete} to set one up
 * before a session is issued. */
public class PlatformMfaEnrollmentRequiredException extends RuntimeException {

    private final String challengeId;

    public PlatformMfaEnrollmentRequiredException(String message, String challengeId) {
        super(message);
        this.challengeId = challengeId;
    }

    public String getChallengeId() {
        return challengeId;
    }
}
