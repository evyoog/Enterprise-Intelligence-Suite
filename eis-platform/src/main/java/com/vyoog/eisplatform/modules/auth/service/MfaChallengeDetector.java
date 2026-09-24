package com.vyoog.eisplatform.modules.auth.service;

/**
 * Phase 7: Keycloak's Direct Grant (ROPC) flow, when its "OTP" execution is
 * configured for a user who has TOTP enrolled, rejects a token request
 * missing (or carrying a wrong) {@code totp} form parameter with a JSON
 * error body whose {@code error_description} contains the literal word
 * "totp" (e.g. {@code "Missing totp"}, {@code "Invalid totp"}) — this is
 * how the real Keycloak server distinguishes "wrong password" from "right
 * password, but this account also needs its OTP code," using only the
 * existing token-endpoint call (no Keycloak Admin API, no hosted UI).
 *
 * <p>Kept as a small, pure, directly-testable class (mirroring
 * {@code ClientAudienceValidator}'s own extraction) specifically because
 * this exact error string is real Keycloak behavior this environment has no
 * live Keycloak admin/OTP-enrolled test account to verify end-to-end against
 * — isolating the string-matching logic here means at least THIS part is
 * unit-tested against literal, documented Keycloak error bodies, rather than
 * being untested logic buried inside an HTTP call.
 */
final class MfaChallengeDetector {

    private MfaChallengeDetector() {
    }

    static boolean isOtpChallenge(String keycloakErrorResponseBody) {
        return keycloakErrorResponseBody != null && keycloakErrorResponseBody.toLowerCase().contains("totp");
    }
}
