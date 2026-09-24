package com.vyoog.eisplatform.modules.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the exact, documented Keycloak error bodies this detector exists
 * to recognize (see its own javadoc) — the one piece of Phase 7's MFA work
 * this environment can verify without a live Keycloak OTP-enrolled account.
 */
class MfaChallengeDetectorTest {

    @Test
    void recognizesKeycloaksMissingTotpError() {
        assertThat(MfaChallengeDetector.isOtpChallenge("{\"error\":\"invalid_grant\",\"error_description\":\"Missing totp\"}")).isTrue();
    }

    @Test
    void recognizesKeycloaksInvalidTotpError() {
        assertThat(MfaChallengeDetector.isOtpChallenge("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid totp\"}")).isTrue();
    }

    @Test
    void isCaseInsensitive() {
        assertThat(MfaChallengeDetector.isOtpChallenge("{\"error_description\":\"MISSING TOTP\"}")).isTrue();
    }

    @Test
    void aGenuinelyWrongPasswordIsNotMistakenForAnOtpChallenge() {
        assertThat(MfaChallengeDetector.isOtpChallenge("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}")).isFalse();
    }

    @Test
    void handlesNullBody() {
        assertThat(MfaChallengeDetector.isOtpChallenge(null)).isFalse();
    }
}
