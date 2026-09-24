package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.auth.model.PasswordResetToken;
import com.vyoog.eisplatform.modules.auth.repository.PasswordResetTokenRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 8: exercises the full forgot-password / reset-password lifecycle
 * and its anti-enumeration behavior against real DB rows — using fakes only
 * for the two things that would otherwise make a live network call
 * (Keycloak's Admin API, real SMTP), matching how this test class fits into
 * an otherwise entirely network-free automated suite (see
 * FakeKeycloakAdminClient's own javadoc).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(PasswordResetServiceTest.TestConfig.class)
class PasswordResetServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }

        @Bean
        @Primary
        EmailService fakeEmailService() {
            return new FakeEmailService();
        }
    }

    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private KeycloakAdminClient keycloakAdminClient;
    @Autowired
    private EmailService emailService;

    private FakeKeycloakAdminClient fakeKeycloak() {
        return (FakeKeycloakAdminClient) keycloakAdminClient;
    }

    private FakeEmailService fakeEmail() {
        return (FakeEmailService) emailService;
    }

    private String rawTokenFromLastEmail() {
        String link = fakeEmail().lastResetLink;
        return link.substring(link.indexOf("token=") + "token=".length());
    }

    @BeforeEach
    void resetFakes() {
        fakeKeycloak().reset();
        fakeEmail().reset();
    }

    @Test
    void requestingResetForARealAccountCreatesATokenAndEmailsARealLink() {
        fakeKeycloak().registerUser("reset-real@test.example", "kc-sub-real");

        passwordResetService.requestReset("reset-real@test.example");

        assertThat(fakeEmail().lastResetEmail).isEqualTo("reset-real@test.example");
        assertThat(fakeEmail().lastResetLink).contains("token=");
        assertThat(tokenRepository.findAll()).anyMatch(t -> "kc-sub-real".equals(t.getKeycloakSub()));
    }

    @Test
    void requestingResetForAnUnknownEmailCreatesNoTokenAndSendsNoEmail() {
        long before = tokenRepository.count();

        passwordResetService.requestReset("no-such-account@test.example");

        assertThat(tokenRepository.count()).isEqualTo(before);
        assertThat(fakeEmail().lastResetEmail).isNull();
    }

    @Test
    void fullLifecycle_requestThenResetActuallyCallsKeycloakAndLogsOutEverywhere() {
        fakeKeycloak().registerUser("reset-lifecycle@test.example", "kc-sub-lifecycle");
        passwordResetService.requestReset("reset-lifecycle@test.example");
        String rawToken = rawTokenFromLastEmail();

        passwordResetService.resetPassword(rawToken, "NewPassw0rd", "NewPassw0rd");

        assertThat(fakeKeycloak().resetPasswordCalls).containsExactly("kc-sub-lifecycle");
        assertThat(fakeKeycloak().logoutCalls).containsExactly("kc-sub-lifecycle");
        assertThat(fakeEmail().lastChangedNoticeEmail).isEqualTo("reset-lifecycle@test.example");
    }

    @Test
    void aUsedTokenCannotBeReusedEvenWithACorrectPassword() {
        fakeKeycloak().registerUser("reset-reuse@test.example", "kc-sub-reuse");
        passwordResetService.requestReset("reset-reuse@test.example");
        String rawToken = rawTokenFromLastEmail();
        passwordResetService.resetPassword(rawToken, "NewPassw0rd", "NewPassw0rd");

        assertThatThrownBy(() -> passwordResetService.resetPassword(rawToken, "AnotherPassw0rd", "AnotherPassw0rd"))
            .isInstanceOf(InvalidCredentialsException.class);
        // Only the first, successful reset actually reached Keycloak.
        assertThat(fakeKeycloak().resetPasswordCalls).hasSize(1);
    }

    @Test
    void anExpiredTokenIsRejectedWithoutEverCallingKeycloak() {
        fakeKeycloak().registerUser("reset-expired@test.example", "kc-sub-expired");
        passwordResetService.requestReset("reset-expired@test.example");
        String rawToken = rawTokenFromLastEmail();

        // Simulate time passing — no scheduler exists anywhere in this app
        // (consistent with Phase 6's PAM expiry, checked the same way).
        PasswordResetToken stored = tokenRepository.findAll().stream()
            .filter(t -> "kc-sub-expired".equals(t.getKeycloakSub())).findFirst().orElseThrow();
        stored.setExpiresAt(Instant.now().minusSeconds(60));
        tokenRepository.save(stored);

        assertThatThrownBy(() -> passwordResetService.resetPassword(rawToken, "NewPassw0rd", "NewPassw0rd"))
            .isInstanceOf(InvalidCredentialsException.class);
        assertThat(fakeKeycloak().resetPasswordCalls).isEmpty();
    }

    @Test
    void anInvalidTokenIsRejected() {
        assertThatThrownBy(() -> passwordResetService.resetPassword("not-a-real-token", "NewPassw0rd", "NewPassw0rd"))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void mismatchedConfirmationIsRejectedBeforeTouchingTheTokenOrKeycloak() {
        fakeKeycloak().registerUser("reset-mismatch@test.example", "kc-sub-mismatch");
        passwordResetService.requestReset("reset-mismatch@test.example");
        String rawToken = rawTokenFromLastEmail();

        assertThatThrownBy(() -> passwordResetService.resetPassword(rawToken, "NewPassw0rd", "DoesNotMatch1"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(fakeKeycloak().resetPasswordCalls).isEmpty();

        // The token is still usable — rejected on the password check alone,
        // not burned by the attempt.
        passwordResetService.resetPassword(rawToken, "NewPassw0rd", "NewPassw0rd");
        assertThat(fakeKeycloak().resetPasswordCalls).containsExactly("kc-sub-mismatch");
    }

    @Test
    void aFailedKeycloakResetLeavesTheTokenUsableForARetry() {
        fakeKeycloak().registerUser("reset-fail@test.example", "kc-sub-fail");
        fakeKeycloak().resetPasswordShouldFail = true;
        passwordResetService.requestReset("reset-fail@test.example");
        String rawToken = rawTokenFromLastEmail();

        assertThatThrownBy(() -> passwordResetService.resetPassword(rawToken, "NewPassw0rd", "NewPassw0rd"))
            .isInstanceOf(IllegalStateException.class);

        // Recover, then retry the SAME link — it must not have been burned.
        fakeKeycloak().resetPasswordShouldFail = false;
        passwordResetService.resetPassword(rawToken, "NewPassw0rd", "NewPassw0rd");
        assertThat(fakeKeycloak().resetPasswordCalls).containsExactly("kc-sub-fail", "kc-sub-fail");
    }
}
