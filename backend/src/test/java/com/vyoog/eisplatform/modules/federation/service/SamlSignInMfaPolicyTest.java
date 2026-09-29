package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.auth.model.MfaChallengeKind;
import com.vyoog.eisplatform.modules.auth.repository.MfaLoginChallengeRepository;
import com.vyoog.eisplatform.modules.auth.service.FakeImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.ImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.PlatformMfaService;
import com.vyoog.eisplatform.modules.federation.dto.CreateSamlProviderRequest;
import com.vyoog.eisplatform.modules.federation.dto.SamlProviderDto;
import com.vyoog.eisplatform.modules.federation.model.SamlLoginRequest;
import com.vyoog.eisplatform.modules.federation.repository.SamlLoginRequestRepository;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * C29 (2026-09-26, REQ-IAM-001): the organization MFA policy on SAML sign-in.
 * A member of an MFA-required organization never gets a session straight from
 * the assertion: they set up an authenticator during sign-in the first time,
 * and enter a code every time after. Real signed SAML responses, faked
 * Keycloak edges (same convention as SamlAuthenticationServiceTest).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(SamlSignInMfaPolicyTest.TestConfig.class)
class SamlSignInMfaPolicyTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }

        @Bean
        @Primary
        ImpersonationExchangeService fakeImpersonationExchangeService() {
            return new FakeImpersonationExchangeService();
        }
    }

    private static final String IDP_ENTITY_ID = "https://claude-test-idp.example.com/saml/metadata";
    private static final String IDP_SSO_URL = "https://claude-test-idp.example.com/saml/sso";

    @Autowired private SamlAuthenticationService samlAuthenticationService;
    @Autowired private SamlProviderService samlProviderService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SamlLoginRequestRepository loginRequestRepository;
    @Autowired private MfaLoginChallengeRepository challengeRepository;
    @Autowired private PlatformMfaService platformMfaService;
    @Autowired private KeycloakAdminClient keycloakAdminClient;
    @Autowired private ImpersonationExchangeService impersonationExchangeService;

    private final DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();

    @BeforeEach
    void resetFakes() {
        ((FakeKeycloakAdminClient) keycloakAdminClient).reset();
        ((FakeImpersonationExchangeService) impersonationExchangeService).reset();
    }

    private Long orgWithProvider(String code, boolean mfaRequired) throws Exception {
        Organization org = new Organization();
        org.setName("MFA SAML Org " + code);
        org.setCode(code + "-" + System.nanoTime());
        org.setBusinessEmail("biz@mfa-saml.example");
        org.setCountry("India");
        org.setLicensedSeats(10);
        org.setMfaRequired(mfaRequired);
        org = organizationRepository.save(org);
        SamlProviderDto provider = samlProviderService.create(org.getId(), new CreateSamlProviderRequest(
            "Test IdP", null, IDP_ENTITY_ID, IDP_SSO_URL, SamlResponseTestFixture.certificatePem()));
        samlProviderService.setEnabled(org.getId(), provider.id(), true);
        return org.getId();
    }

    private String signIn(Long orgId, String email, MockHttpServletResponse response) throws Exception {
        samlAuthenticationService.buildRedirectUrl(orgId);
        List<SamlLoginRequest> rows = loginRequestRepository.findByOrganizationId(orgId);
        String requestId = rows.get(rows.size() - 1).getId();
        SamlResponseTestFixture.Params params = new SamlResponseTestFixture.Params();
        params.withInResponseTo(requestId)
            .withDestination(samlProviderService.acsUrl(orgId))
            .withIssuer(IDP_ENTITY_ID)
            .withNameId(email)
            .withAudience(samlProviderService.spEntityId(orgId))
            .withAttributes(Map.of("email", email, "firstName", "Grace", "lastName", "Hopper"));
        return samlAuthenticationService.handleAcs(orgId, SamlResponseTestFixture.buildEncodedResponse(params), response);
    }

    private static String param(String url, String name) {
        String marker = name + "=";
        int i = url.indexOf(marker);
        assertThat(i).as("redirect %s carries %s", url, name).isGreaterThan(-1);
        return URLDecoder.decode(url.substring(i + marker.length()), StandardCharsets.UTF_8);
    }

    private String codeFor(String secret) throws Exception {
        return codeGenerator.generate(secret, System.currentTimeMillis() / 1000 / 30);
    }

    @Test
    void firstSamlSignInToAnMfaRequiredOrganizationSetsUpAnAuthenticatorBeforeAnySession() throws Exception {
        Long orgId = orgWithProvider("MFA-REQ", true);
        String email = "grace-" + System.nanoTime() + "@example.com";

        MockHttpServletResponse response = new MockHttpServletResponse();
        String redirect = signIn(orgId, email, response);

        assertThat(redirect).contains("?mfaEnroll=");
        assertThat(response.getHeaders("Set-Cookie")).noneMatch(c -> c.startsWith("eis_rt="));
        String challengeId = param(redirect, "mfaEnroll");
        var challenge = challengeRepository.findById(challengeId).orElseThrow();
        assertThat(challenge.getKind()).isEqualTo(MfaChallengeKind.ENROLL);
        assertThat(challenge.isImpersonated()).isTrue();

        var enrollment = platformMfaService.startChallengeEnrollment(challengeId);
        assertThatThrownBy(() -> platformMfaService.completeChallengeEnrollment(challengeId, "000000"))
            .isInstanceOf(InvalidCredentialsException.class);
        assertThat(challengeRepository.findById(challengeId).orElseThrow().getAttempts()).isEqualTo(1);

        var result = platformMfaService.completeChallengeEnrollment(challengeId, codeFor(enrollment.secret()));
        assertThat(result.login().impersonated()).isTrue();
        assertThat(result.recoveryCodes()).isNotEmpty();
        assertThat(challengeRepository.findById(challengeId)).isEmpty();

        Long customerId = customerRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
        assertThat(platformMfaService.isEnabledFor(customerId)).isTrue();

        // Every later SAML sign-in asks for the code instead.
        String second = signIn(orgId, email, new MockHttpServletResponse());
        String verifyId = param(second, "mfaChallenge");
        assertThat(challengeRepository.findById(verifyId).orElseThrow().getKind()).isEqualTo(MfaChallengeKind.VERIFY);
        var verified = platformMfaService.verifyLoginChallenge(verifyId, codeFor(enrollment.secret()));
        assertThat(verified.impersonated()).isTrue();
    }

    @Test
    void anEnrollChallengeCannotBeUsedAsAVerifyChallengeOrTheOtherWayRound() throws Exception {
        Long orgId = orgWithProvider("MFA-KIND", true);
        String redirect = signIn(orgId, "kind-" + System.nanoTime() + "@example.com", new MockHttpServletResponse());
        String challengeId = param(redirect, "mfaEnroll");

        assertThatThrownBy(() -> platformMfaService.verifyLoginChallenge(challengeId, "123456"))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("no longer valid");
    }

    @Test
    void anOrganizationWithoutTheMfaPolicyStillSignsInDirectly() throws Exception {
        Long orgId = orgWithProvider("MFA-OFF", false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String redirect = signIn(orgId, "direct-" + System.nanoTime() + "@example.com", response);

        assertThat(redirect).doesNotContain("mfa");
        assertThat(response.getHeaders("Set-Cookie")).anyMatch(c -> c.startsWith("eis_rt="));
    }
}
