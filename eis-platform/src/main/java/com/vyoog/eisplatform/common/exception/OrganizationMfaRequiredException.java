package com.vyoog.eisplatform.common.exception;

/** Login succeeded (correct password, and any OTP Keycloak itself demanded),
 * but the account's own organization requires MFA and this login's token
 * shows no {@code amr: otp} — meaning the account has no OTP configured in
 * Keycloak at all. Distinct from {@link MfaChallengeRequiredException} (that
 * one means "you have OTP, Keycloak wants the code right now"); this one
 * means "you need to have OTP set up before this organization will let you
 * in" (see GlobalExceptionHandler's {@code organizationMfaRequired} flag). */
public class OrganizationMfaRequiredException extends RuntimeException {

    public OrganizationMfaRequiredException(String message) {
        super(message);
    }
}
