package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.auth.dto.MfaEnrollResponse;
import com.vyoog.eisplatform.modules.auth.dto.MfaRecoveryCodesResponse;
import com.vyoog.eisplatform.modules.auth.dto.MfaStatusDto;
import com.vyoog.eisplatform.modules.auth.repository.CustomerMfaCredentialRepository;
import com.vyoog.eisplatform.modules.auth.repository.MfaLoginChallengeRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
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
 * Phase 2 (2026.3.3): exercises the full Platform TOTP lifecycle (enroll,
 * verify-enrollment, login challenge, recovery codes, disable, regenerate)
 * against real DB rows, real AES-GCM encryption/decryption, and real TOTP
 * math (the same library production code uses) — the only fakes are the two
 * things that would otherwise make a live network call (Keycloak password
 * re-verification, SMTP), same convention as PasswordResetServiceTest.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(PlatformMfaServiceTest.TestConfig.class)
class PlatformMfaServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        CurrentPasswordVerifier fakePasswordVerifier() {
            return new FakeCurrentPasswordVerifier();
        }

        @Bean
        @Primary
        EmailService fakeEmailService() {
            return new FakeEmailService();
        }
    }

    @Autowired
    private PlatformMfaService platformMfaService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerMfaCredentialRepository credentialRepository;
    @Autowired
    private MfaLoginChallengeRepository challengeRepository;
    @Autowired
    private CurrentPasswordVerifier passwordVerifier;
    @Autowired
    private EmailService emailService;

    private final DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final SystemTimeProvider timeProvider = new SystemTimeProvider();

    private FakeCurrentPasswordVerifier fakePasswordVerifier() {
        return (FakeCurrentPasswordVerifier) passwordVerifier;
    }

    @BeforeEach
    void resetFakes() {
        fakePasswordVerifier().reset();
        ((FakeEmailService) emailService).reset();
    }

    private Long newCustomerId(String emailPrefix) {
        Customer customer = new Customer();
        customer.setEmail(emailPrefix + "-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Mfa");
        customer.setLastName("Tester");
        customer.setStatus(RegistrationStatus.COMPLETED);
        return customerRepository.save(customer).getId();
    }

    private String validCodeFor(String secret) {
        try {
            return codeGenerator.generate(secret, timeProvider.getTime() / 30);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------
    // Enrollment
    // ---------------------------------------------------------------

    @Test
    void enrollGeneratesARealSecretAndDoesNotEnableMfaYet() {
        Long customerId = newCustomerId("enroll");
        MfaEnrollResponse response = platformMfaService.enroll(customerId, "enroll@example.com", "correct-password");

        assertThat(response.secret()).isNotBlank();
        assertThat(response.otpAuthUri()).startsWith("otpauth://totp/");
        assertThat(response.qrCodePngBase64()).isNotBlank();
        assertThat(fakePasswordVerifier().verifiedEmails).containsExactly("enroll@example.com");

        assertThat(platformMfaService.isEnabledFor(customerId)).isFalse();
        assertThat(credentialRepository.findById(customerId)).isPresent();
        assertThat(credentialRepository.findById(customerId).get().isEnabled()).isFalse();
    }

    @Test
    void enrollWithWrongPasswordIsRejectedAndChangesNothing() {
        Long customerId = newCustomerId("wrongpw");
        fakePasswordVerifier().shouldFail = true;

        assertThatThrownBy(() -> platformMfaService.enroll(customerId, "wrongpw@example.com", "bad-password"))
            .isInstanceOf(InvalidCredentialsException.class);

        assertThat(credentialRepository.findById(customerId)).isEmpty();
    }

    @Test
    void validCodeEnablesMfaAndReturnsRecoveryCodes() {
        Long customerId = newCustomerId("verify-ok");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "verify-ok@example.com", "pw");

        MfaRecoveryCodesResponse recovery = platformMfaService.verifyEnrollment(customerId, "verify-ok@example.com", validCodeFor(enrolled.secret()));

        assertThat(recovery.codes()).hasSize(10);
        assertThat(platformMfaService.isEnabledFor(customerId)).isTrue();
        MfaStatusDto status = platformMfaService.getStatus(customerId);
        assertThat(status.enabled()).isTrue();
        assertThat(status.enrolledAt()).isNotNull();
        assertThat(status.remainingRecoveryCodes()).isEqualTo(10);
    }

    @Test
    void invalidCodeDoesNotEnableMfa() {
        Long customerId = newCustomerId("verify-bad");
        platformMfaService.enroll(customerId, "verify-bad@example.com", "pw");

        assertThatThrownBy(() -> platformMfaService.verifyEnrollment(customerId, "verify-bad@example.com", "000000"))
            .isInstanceOf(InvalidCredentialsException.class);

        assertThat(platformMfaService.isEnabledFor(customerId)).isFalse();
    }

    @Test
    void verifyEnrollmentWithNoPendingEnrollmentIsRejected() {
        Long customerId = newCustomerId("no-pending");
        assertThatThrownBy(() -> platformMfaService.verifyEnrollment(customerId, "no-pending@example.com", "123456"))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    // ---------------------------------------------------------------
    // Recovery codes
    // ---------------------------------------------------------------

    @Test
    void recoveryCodeIsSingleUse() {
        Long customerId = newCustomerId("recovery");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "recovery@example.com", "pw");
        MfaRecoveryCodesResponse recovery = platformMfaService.verifyEnrollment(customerId, "recovery@example.com", validCodeFor(enrolled.secret()));
        String oneCode = recovery.codes().get(0);

        String challengeId = platformMfaService.createLoginChallenge(customerId, "kc-sub", "access-1", "refresh-1");
        PlatformMfaService.LoginChallengeResult result = platformMfaService.verifyLoginChallenge(challengeId, oneCode);
        assertThat(result.accessToken()).isEqualTo("access-1");

        String challengeId2 = platformMfaService.createLoginChallenge(customerId, "kc-sub", "access-2", "refresh-2");
        assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(challengeId2, oneCode))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void regeneratingRecoveryCodesInvalidatesTheOldOnes() {
        Long customerId = newCustomerId("regen");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "regen@example.com", "pw");
        MfaRecoveryCodesResponse original = platformMfaService.verifyEnrollment(customerId, "regen@example.com", validCodeFor(enrolled.secret()));

        String freshTotp = validCodeFor(enrolled.secret());
        MfaRecoveryCodesResponse regenerated = platformMfaService.regenerateRecoveryCodes(customerId, "regen@example.com", "pw", freshTotp);

        assertThat(regenerated.codes()).doesNotContainAnyElementsOf(original.codes());

        String challengeId = platformMfaService.createLoginChallenge(customerId, "kc-sub", "a", "r");
        assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(challengeId, original.codes().get(0)))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    // ---------------------------------------------------------------
    // Login challenge
    // ---------------------------------------------------------------

    @Test
    void validTotpCompletesTheLoginChallengeAndReturnsTheHeldTokens() {
        Long customerId = newCustomerId("login-ok");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "login-ok@example.com", "pw");
        platformMfaService.verifyEnrollment(customerId, "login-ok@example.com", validCodeFor(enrolled.secret()));

        String challengeId = platformMfaService.createLoginChallenge(customerId, "kc-sub-1", "held-access", "held-refresh");
        PlatformMfaService.LoginChallengeResult result = platformMfaService.verifyLoginChallenge(challengeId, validCodeFor(enrolled.secret()));

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.keycloakSub()).isEqualTo("kc-sub-1");
        assertThat(result.accessToken()).isEqualTo("held-access");
        assertThat(result.refreshToken()).isEqualTo("held-refresh");
    }

    @Test
    void challengeIsSingleUse() {
        Long customerId = newCustomerId("replay");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "replay@example.com", "pw");
        platformMfaService.verifyEnrollment(customerId, "replay@example.com", validCodeFor(enrolled.secret()));

        String challengeId = platformMfaService.createLoginChallenge(customerId, "kc-sub", "a", "r");
        String code = validCodeFor(enrolled.secret());
        platformMfaService.verifyLoginChallenge(challengeId, code);

        assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(challengeId, code))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void expiredChallengeIsRejected() {
        Long customerId = newCustomerId("expired");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "expired@example.com", "pw");
        platformMfaService.verifyEnrollment(customerId, "expired@example.com", validCodeFor(enrolled.secret()));

        String challengeId = platformMfaService.createLoginChallenge(customerId, "kc-sub", "a", "r");
        challengeRepository.findById(challengeId).ifPresent(c -> {
            c.setExpiresAt(Instant.now().minusSeconds(1));
            challengeRepository.save(c);
        });

        assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(challengeId, validCodeFor(enrolled.secret())))
            .isInstanceOf(InvalidCredentialsException.class);
        assertThat(challengeRepository.findById(challengeId)).isEmpty();
    }

    @Test
    void tooManyWrongAttemptsLocksTheChallengeOutEvenWithACorrectCodeAfterward() {
        Long customerId = newCustomerId("lockout");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "lockout@example.com", "pw");
        platformMfaService.verifyEnrollment(customerId, "lockout@example.com", validCodeFor(enrolled.secret()));

        String challengeId = platformMfaService.createLoginChallenge(customerId, "kc-sub", "a", "r");
        for (int i = 0; i < 5; i++) {
            String cid = challengeId;
            assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(cid, "000000"))
                .isInstanceOf(InvalidCredentialsException.class);
        }

        // The challenge is now locked out entirely — even the real code no longer works.
        assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(challengeId, validCodeFor(enrolled.secret())))
            .isInstanceOf(InvalidCredentialsException.class);
        assertThat(challengeRepository.findById(challengeId)).isEmpty();
    }

    // ---------------------------------------------------------------
    // Disable
    // ---------------------------------------------------------------

    @Test
    void disableRequiresAValidFactorNotJustThePassword() {
        Long customerId = newCustomerId("disable-bad-code");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "disable-bad-code@example.com", "pw");
        platformMfaService.verifyEnrollment(customerId, "disable-bad-code@example.com", validCodeFor(enrolled.secret()));

        assertThatThrownBy(() -> platformMfaService.disable(customerId, "disable-bad-code@example.com", "pw", "000000"))
            .isInstanceOf(InvalidCredentialsException.class);
        assertThat(platformMfaService.isEnabledFor(customerId)).isTrue();
    }

    @Test
    void disableWithValidPasswordAndTotpRemovesMfaEntirely() {
        Long customerId = newCustomerId("disable-ok");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "disable-ok@example.com", "pw");
        MfaRecoveryCodesResponse recovery = platformMfaService.verifyEnrollment(customerId, "disable-ok@example.com", validCodeFor(enrolled.secret()));

        platformMfaService.disable(customerId, "disable-ok@example.com", "pw", validCodeFor(enrolled.secret()));

        assertThat(platformMfaService.isEnabledFor(customerId)).isFalse();
        assertThat(credentialRepository.findById(customerId)).isEmpty();

        // The old recovery codes are gone too — a fresh challenge can't use them.
        String challengeId = platformMfaService.createLoginChallenge(customerId, "kc-sub", "a", "r");
        assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(challengeId, recovery.codes().get(0)))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void disableCanAlsoBeCompletedWithARecoveryCodeWhenTheAuthenticatorIsLost() {
        Long customerId = newCustomerId("disable-recovery");
        MfaEnrollResponse enrolled = platformMfaService.enroll(customerId, "disable-recovery@example.com", "pw");
        MfaRecoveryCodesResponse recovery = platformMfaService.verifyEnrollment(customerId, "disable-recovery@example.com", validCodeFor(enrolled.secret()));

        platformMfaService.disable(customerId, "disable-recovery@example.com", "pw", recovery.codes().get(0));

        assertThat(platformMfaService.isEnabledFor(customerId)).isFalse();
    }

    // ---------------------------------------------------------------
    // Isolation
    // ---------------------------------------------------------------

    @Test
    void oneCustomersMfaStateNeverLeaksIntoAnothers() {
        Long customerA = newCustomerId("iso-a");
        Long customerB = newCustomerId("iso-b");
        MfaEnrollResponse enrolledA = platformMfaService.enroll(customerA, "iso-a@example.com", "pw");
        platformMfaService.verifyEnrollment(customerA, "iso-a@example.com", validCodeFor(enrolledA.secret()));

        assertThat(platformMfaService.isEnabledFor(customerA)).isTrue();
        assertThat(platformMfaService.isEnabledFor(customerB)).isFalse();
        assertThat(platformMfaService.getStatus(customerB).enabled()).isFalse();
    }
}
