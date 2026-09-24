package com.vyoog.eisplatform.common.exception;

/** Email/password were correct, but Keycloak also demands this account's
 * OTP code — distinct from {@link InvalidCredentialsException} so the
 * frontend can show an inline "enter your code" field instead of "wrong
 * password" (see GlobalExceptionHandler's {@code mfaRequired} response flag). */
public class MfaChallengeRequiredException extends RuntimeException {

    public MfaChallengeRequiredException(String message) {
        super(message);
    }
}
