package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationRegistrationRequest;
import com.vyoog.eisplatform.modules.registration.dto.RegistrationAcceptedResponse;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Organization registration is now the ONLY registration path (individual
 * self-registration was removed) — see RegistrationService's own javadoc.
 * Uses a fake KeycloakAdminClient (never a live network call), same
 * convention as PasswordResetServiceTest. */
@SpringBootTest
@ActiveProfiles("test")
@Import(RegistrationServiceTest.TestConfig.class)
class RegistrationServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }
    }

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    private FakeKeycloakAdminClient fakeKeycloak() {
        return (FakeKeycloakAdminClient) keycloakAdminClient;
    }

    @BeforeEach
    void resetFake() {
        fakeKeycloak().reset();
    }

    private OrganizationRegistrationRequest.FirstAdmin admin(String email) {
        return new OrganizationRegistrationRequest.FirstAdmin(null, null, email, null, "Passw0rd1", "Passw0rd1");
    }

    private OrganizationRegistrationRequest fullRequest(String orgName, String code, String adminEmail) {
        return new OrganizationRegistrationRequest(
            orgName, code, null, null, null,
            "biz-" + adminEmail, null, "India", null, null, null, null, null, null, null,
            true, null, null, null, null,
            List.of(), 3,
            admin(adminEmail)
        );
    }

    private OrganizationRegistrationRequest minimalRequest(String orgName, String adminEmail) {
        return new OrganizationRegistrationRequest(
            orgName, null, null, null, null,
            "biz-" + adminEmail, "9876543210", null, null, null, null, "22AAAAA0000A1Z5", null, null, null,
            true, null, null, null, null,
            null, null,
            admin(adminEmail)
        );
    }

    @Test
    void validOrganizationRegistrationCreatesAPendingOrgAndADisabledKeycloakUser() {
        RegistrationAcceptedResponse response = registrationService.registerOrganization(
            fullRequest("Acme", "ACME-" + System.nanoTime(), "admin-" + System.nanoTime() + "@test.example"));

        var organization = organizationRepository.findById(Long.valueOf(response.registrationId())).orElseThrow();
        assertThat(organization.getStatus()).isEqualTo(RegistrationStatus.PENDING_EMAIL_VERIFICATION);

        assertThat(fakeKeycloak().createUserCalls).hasSize(1);
        assertThat(fakeKeycloak().createUserCalls.get(0).enabled()).isFalse();
    }

    @Test
    void minimalRegistrationDefaultsEverythingNotCollectedByTheRealForm() {
        String orgName = "Minimal Org " + System.nanoTime();
        String adminEmail = "minimal-" + System.nanoTime() + "@test.example";

        RegistrationAcceptedResponse response = registrationService.registerOrganization(minimalRequest(orgName, adminEmail));

        var organization = organizationRepository.findById(Long.valueOf(response.registrationId())).orElseThrow();
        assertThat(organization.getCode()).isNotBlank();
        assertThat(organization.getCountry()).isEqualTo("India");
        assertThat(organization.getLicensedSeats()).isEqualTo(5);

        var admin = customerRepository.findByEmailIgnoreCase(adminEmail).orElseThrow();
        assertThat(admin.getFirstName()).isNotBlank();
        assertThat(admin.getLastName()).isNotBlank();
        assertThat(admin.getKeycloakSub()).isNotBlank();
    }

    @Test
    void emailVerificationEnablesTheKeycloakUserAndCompletesTheOrganization() {
        String adminEmail = "verify-" + System.nanoTime() + "@test.example";
        RegistrationAcceptedResponse response = registrationService.registerOrganization(
            fullRequest("Verify Co", "VERIFY-" + System.nanoTime(), adminEmail));
        Long organizationId = Long.valueOf(response.registrationId());

        var admin = customerRepository.findByEmailIgnoreCase(adminEmail).orElseThrow();
        assertThat(fakeKeycloak().isEnabled(admin.getKeycloakSub())).isFalse();

        // The raw token only ever exists in the emailed link (only its hash
        // is persisted — see EmailVerificationService) — re-issuing a fresh
        // one for the SAME organization exercises the identical verifyEmail()
        // path a real click would, without needing a fake EmailService here.
        String rawToken = emailVerificationService.issueForOrganization(organizationId);
        registrationService.verifyEmail(rawToken);

        var organization = organizationRepository.findById(organizationId).orElseThrow();
        assertThat(organization.getStatus()).isEqualTo(RegistrationStatus.COMPLETED);
        assertThat(fakeKeycloak().isEnabled(admin.getKeycloakSub())).isTrue();
    }

    @Test
    void keycloakUserCreationFailureRollsBackTheWholeRegistration() {
        fakeKeycloak().createUserShouldFail = true;
        String adminEmail = "failcreate-" + System.nanoTime() + "@test.example";

        assertThatThrownBy(() -> registrationService.registerOrganization(
            fullRequest("Fail Co", "FAILCO-" + System.nanoTime(), adminEmail)))
            .isInstanceOf(IllegalStateException.class);

        assertThat(customerRepository.existsByEmailIgnoreCase(adminEmail)).isFalse();
    }

    @Test
    void mismatchedPasswordsAreRejected() {
        var request = new OrganizationRegistrationRequest(
            "Mismatch Co", null, null, null, null,
            "biz-mismatch@test.example", null, null, null, null, null, null, null, null, null,
            true, null, null, null, null,
            null, null,
            new OrganizationRegistrationRequest.FirstAdmin(null, null, "mismatch@test.example", null, "Passw0rd1", "SomethingElse1")
        );
        assertThatThrownBy(() -> registrationService.registerOrganization(request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tooShortPasswordIsRejected() {
        var request = new OrganizationRegistrationRequest(
            "Shortpw Co", null, null, null, null,
            "biz-shortpw@test.example", null, null, null, null, null, null, null, null, null,
            true, null, null, null, null,
            null, null,
            new OrganizationRegistrationRequest.FirstAdmin(null, null, "shortpw@test.example", null, "ab1", "ab1")
        );
        assertThatThrownBy(() -> registrationService.registerOrganization(request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // The core anti-enumeration guarantee: a duplicate admin email must
    // produce a response that is byte-for-byte the same shape as a fresh
    // success, and must never create a second customer row.
    @Test
    void duplicateEmailNeverCreatesASecondRowAndLooksLikeSuccess() {
        String email = "duplicate-" + System.nanoTime() + "@test.example";
        registrationService.registerOrganization(fullRequest("Original Co", "ORIG-" + System.nanoTime(), email));
        long countBefore = customerRepository.count();

        RegistrationAcceptedResponse duplicateResponse = registrationService.registerOrganization(
            fullRequest("Someone Else Co", "ELSE-" + System.nanoTime(), email));

        assertThat(customerRepository.count()).isEqualTo(countBefore);

        String controlEmail = "unique-control-" + System.nanoTime() + "@test.example";
        RegistrationAcceptedResponse controlResponse = registrationService.registerOrganization(
            fullRequest("Control Co", "CTRL-" + System.nanoTime(), controlEmail));

        assertThat(duplicateResponse.message()).isEqualTo(controlResponse.message());
    }

    @Test
    void verifyEmailWithBogusTokenIsRejected() {
        assertThatThrownBy(() -> registrationService.verifyEmail("not-a-real-token"))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void duplicateOrganizationCodeIsRejectedLoudlyNotGenerically() {
        String code = "DUPTEST-" + System.nanoTime();
        registrationService.registerOrganization(fullRequest("Acme", code, "admin-" + System.nanoTime() + "@acme.example"));

        assertThatThrownBy(() -> registrationService.registerOrganization(
            fullRequest("Acme Two", code.toLowerCase(), "admin2-" + System.nanoTime() + "@acme.example")))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Autowired
    private EmailVerificationService emailVerificationService;
}
