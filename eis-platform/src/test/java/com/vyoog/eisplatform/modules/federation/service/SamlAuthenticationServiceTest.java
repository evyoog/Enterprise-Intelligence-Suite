package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.modules.auth.service.FakeImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.ImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.federation.dto.CreateSamlProviderRequest;
import com.vyoog.eisplatform.modules.federation.dto.SamlProviderDto;
import com.vyoog.eisplatform.modules.federation.dto.SsoCheckResponseDto;
import com.vyoog.eisplatform.modules.federation.model.SamlLoginRequest;
import com.vyoog.eisplatform.modules.federation.repository.SamlExternalIdentityRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlLoginRequestRepository;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 5 (2026.3.3): every test here validates a REAL, cryptographically
 * signed SAML Response (see {@link SamlResponseTestFixture}, signed with the
 * real openssl-generated test key/cert in {@code src/test/resources/saml/})
 * against the real OneLogin java-saml-core validation code — never a
 * fabricated/stubbed "valid" flag. Only the Keycloak-facing edges
 * ({@link KeycloakAdminClient}, {@link ImpersonationExchangeService}) are
 * faked, same convention as {@code RegistrationServiceTest}: no test in this
 * suite makes a live network call.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(SamlAuthenticationServiceTest.TestConfig.class)
class SamlAuthenticationServiceTest {

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
    @Autowired private OrganizationMemberRepository organizationMemberRepository;
    @Autowired private SamlExternalIdentityRepository externalIdentityRepository;
    @Autowired private SamlLoginRequestRepository loginRequestRepository;
    @Autowired private KeycloakAdminClient keycloakAdminClient;
    @Autowired private ImpersonationExchangeService impersonationExchangeService;

    private FakeKeycloakAdminClient fakeKeycloak() {
        return (FakeKeycloakAdminClient) keycloakAdminClient;
    }

    private FakeImpersonationExchangeService fakeExchange() {
        return (FakeImpersonationExchangeService) impersonationExchangeService;
    }

    @BeforeEach
    void resetFakes() {
        fakeKeycloak().reset();
        fakeExchange().reset();
    }

    private Organization newOrganization(String code) {
        Organization organization = new Organization();
        organization.setName("SAML Login Test Org " + code);
        organization.setCode(code);
        organization.setBusinessEmail("biz-" + code + "@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        return organizationRepository.save(organization);
    }

    private Long newOrgWithEnabledProvider(String code) throws Exception {
        Organization org = newOrganization(code);
        SamlProviderDto provider = samlProviderService.create(org.getId(), new CreateSamlProviderRequest(
            "Test IdP", null, IDP_ENTITY_ID, IDP_SSO_URL, SamlResponseTestFixture.certificatePem()));
        samlProviderService.setEnabled(org.getId(), provider.id(), true);
        return org.getId();
    }

    private String issueRequestId(Long organizationId) {
        samlAuthenticationService.buildRedirectUrl(organizationId);
        List<SamlLoginRequest> rows = loginRequestRepository.findByOrganizationId(organizationId);
        return rows.get(rows.size() - 1).getId();
    }

    private SamlResponseTestFixture.Params validParamsFor(Long organizationId, String requestId, String email) {
        SamlResponseTestFixture.Params params = new SamlResponseTestFixture.Params();
        params.withInResponseTo(requestId)
            .withDestination(samlProviderService.acsUrl(organizationId))
            .withIssuer(IDP_ENTITY_ID)
            .withNameId(email)
            .withAudience(samlProviderService.spEntityId(organizationId))
            .withAttributes(Map.of("email", email, "firstName", "Ada", "lastName", "Lovelace"));
        return params;
    }

    // ------------------------------------------------------------------
    // sso-check / disabled / unauthorized provider
    // ------------------------------------------------------------------

    @Test
    void ssoCheckReportsUnavailableWhenNoProviderIsConfigured() {
        Organization org = newOrganization("SSO-NONE");
        assertThat(samlAuthenticationService.checkSso(org.getCode()).available()).isFalse();
    }

    @Test
    void ssoCheckReportsUnavailableWhileTheOnlyProviderIsDisabled() throws Exception {
        Organization org = newOrganization("SSO-DISABLED");
        samlProviderService.create(org.getId(), new CreateSamlProviderRequest(
            "Test IdP", null, IDP_ENTITY_ID, IDP_SSO_URL, SamlResponseTestFixture.certificatePem()));
        // Deliberately never enabled.
        assertThat(samlAuthenticationService.checkSso(org.getCode()).available()).isFalse();
        assertThatThrownBy(() -> samlAuthenticationService.buildRedirectUrl(org.getId()))
            .isInstanceOf(SamlLoginException.class);
    }

    @Test
    void ssoCheckReportsAvailableOnceAProviderIsEnabled() throws Exception {
        Long orgId = newOrgWithEnabledProvider("SSO-YES");
        Organization org = organizationRepository.findById(orgId).orElseThrow();
        SsoCheckResponseDto result = samlAuthenticationService.checkSso(org.getCode());
        assertThat(result.available()).isTrue();
        assertThat(result.organizationId()).isEqualTo(orgId);
        assertThat(result.organizationName()).isEqualTo(org.getName());
    }

    @Test
    void loginInitRejectsAnOrganizationWithNoProviderAtAll() {
        Organization org = newOrganization("NO-PROVIDER");
        assertThatThrownBy(() -> samlAuthenticationService.buildRedirectUrl(org.getId()))
            .isInstanceOf(SamlLoginException.class);
    }

    // ------------------------------------------------------------------
    // Valid assertion — the full happy path (JIT provisioning, session, membership)
    // ------------------------------------------------------------------

    @Test
    void validAssertionJitProvisionsANewCustomerAndFinalizesARealSession() throws Exception {
        Long orgId = newOrgWithEnabledProvider("VALID");
        String requestId = issueRequestId(orgId);
        String email = "ada@example.com";
        String encoded = SamlResponseTestFixture.buildEncodedResponse(validParamsFor(orgId, requestId, email));

        MockHttpServletResponse response = new MockHttpServletResponse();
        String redirectTarget = samlAuthenticationService.handleAcs(orgId, encoded, response);

        assertThat(redirectTarget).isEqualTo("http://localhost:5173");
        assertThat(fakeKeycloak().createUserCalls).hasSize(1);
        assertThat(fakeKeycloak().createUserCalls.get(0).email()).isEqualTo(email);
        assertThat(fakeExchange().exchangeCalls).hasSize(1);

        List<String> setCookies = response.getHeaders("Set-Cookie");
        assertThat(setCookies).anyMatch(c -> c.startsWith("eis_rt="));
        assertThat(setCookies).anyMatch(c -> c.startsWith("vyoog_sso="));

        var customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(customer.getKeycloakSub()).isNotBlank();

        var identity = externalIdentityRepository.findByOrganizationIdAndIdpEntityIdAndNameId(orgId, IDP_ENTITY_ID, email)
            .orElseThrow();
        assertThat(identity.getCustomerId()).isEqualTo(customer.getId());

        var membership = organizationMemberRepository.findByOrganizationIdAndCustomerId(orgId, customer.getId())
            .orElseThrow();
        assertThat(membership.getOrgRole()).isEqualTo(OrgRole.MEMBER);
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);

        SamlLoginRequest consumedRequest = loginRequestRepository.findById(requestId).orElseThrow();
        assertThat(consumedRequest.getConsumedAt()).isNotNull();
    }

    @Test
    void aSecondPresentationOfTheSameResponseIsRejectedAsAReplay() throws Exception {
        Long orgId = newOrgWithEnabledProvider("REPLAY");
        String requestId = issueRequestId(orgId);
        String encoded = SamlResponseTestFixture.buildEncodedResponse(
            validParamsFor(orgId, requestId, "replay@example.com"));

        samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse());

        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class)
            .hasMessageContaining("already been used");
    }

    // ------------------------------------------------------------------
    // Invalid assertion / signature / issuer / audience / expired
    // ------------------------------------------------------------------

    @Test
    void malformedResponseBodyIsRejected() throws Exception {
        Long orgId = newOrgWithEnabledProvider("MALFORMED");
        String requestId = issueRequestId(orgId);
        String garbage = java.util.Base64.getEncoder().encodeToString("not xml at all".getBytes());

        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, garbage, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class);
        // Nothing was ever provisioned from garbage input.
        assertThat(fakeKeycloak().createUserCalls).isEmpty();
    }

    @Test
    void tamperedAssertionFailsSignatureValidation() throws Exception {
        Long orgId = newOrgWithEnabledProvider("TAMPERED");
        String requestId = issueRequestId(orgId);
        SamlResponseTestFixture.Params params = validParamsFor(orgId, requestId, "victim@example.com")
            .withTamperAfterSigning();
        String encoded = SamlResponseTestFixture.buildEncodedResponse(params);

        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class)
            .hasMessageContaining("could not be verified");
        assertThat(fakeKeycloak().createUserCalls).isEmpty();
    }

    @Test
    void unsignedAssertionIsRejected() throws Exception {
        Long orgId = newOrgWithEnabledProvider("UNSIGNED");
        String requestId = issueRequestId(orgId);
        SamlResponseTestFixture.Params params = validParamsFor(orgId, requestId, "nosig@example.com")
            .withoutSignature();
        String encoded = SamlResponseTestFixture.buildEncodedResponse(params);

        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class);
    }

    @Test
    void wrongIssuerIsRejected() throws Exception {
        Long orgId = newOrgWithEnabledProvider("BAD-ISSUER");
        String requestId = issueRequestId(orgId);
        SamlResponseTestFixture.Params params = validParamsFor(orgId, requestId, "issuer@example.com")
            .withIssuer("https://not-the-configured-idp.example.com");
        String encoded = SamlResponseTestFixture.buildEncodedResponse(params);

        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class);
    }

    @Test
    void wrongAudienceIsRejected() throws Exception {
        Long orgId = newOrgWithEnabledProvider("BAD-AUDIENCE");
        String requestId = issueRequestId(orgId);
        SamlResponseTestFixture.Params params = validParamsFor(orgId, requestId, "audience@example.com")
            .withAudience("https://some-other-service-provider.example.com");
        String encoded = SamlResponseTestFixture.buildEncodedResponse(params);

        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class);
    }

    @Test
    void expiredAssertionIsRejected() throws Exception {
        Long orgId = newOrgWithEnabledProvider("EXPIRED");
        String requestId = issueRequestId(orgId);
        SamlResponseTestFixture.Params params = validParamsFor(orgId, requestId, "expired@example.com")
            .withNotOnOrAfter(Instant.now().minusSeconds(600));
        String encoded = SamlResponseTestFixture.buildEncodedResponse(params);

        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class);
    }

    // ------------------------------------------------------------------
    // Identity mapping / organization isolation / role mapping
    // ------------------------------------------------------------------

    @Test
    void theSameExternalIdentityAlwaysResolvesToTheSameCustomerNeverADuplicate() throws Exception {
        Long orgId = newOrgWithEnabledProvider("IDENTITY-MAP");
        String email = "stable@example.com";

        String firstRequestId = issueRequestId(orgId);
        String firstEncoded = SamlResponseTestFixture.buildEncodedResponse(validParamsFor(orgId, firstRequestId, email));
        samlAuthenticationService.handleAcs(orgId, firstEncoded, new MockHttpServletResponse());

        String secondRequestId = issueRequestId(orgId);
        String secondEncoded = SamlResponseTestFixture.buildEncodedResponse(validParamsFor(orgId, secondRequestId, email));
        samlAuthenticationService.handleAcs(orgId, secondEncoded, new MockHttpServletResponse());

        assertThat(fakeKeycloak().createUserCalls).hasSize(1);
        assertThat(customerRepository.findByEmailIgnoreCase(email)).isPresent();
        var identity = externalIdentityRepository.findByOrganizationIdAndIdpEntityIdAndNameId(orgId, IDP_ENTITY_ID, email)
            .orElseThrow();
        assertThat(identity.getLastLoginAt()).isNotNull();
    }

    @Test
    void anAssertionIssuedForOneOrganizationCannotBeUsedAgainstAnotherOrganizationsAcs() throws Exception {
        Long orgAId = newOrgWithEnabledProvider("ORG-A");
        Long orgBId = newOrgWithEnabledProvider("ORG-B");

        String requestIdForOrgA = issueRequestId(orgAId);
        // Built with org A's own destination/audience — a genuinely valid
        // response for org A's ACS endpoint.
        String encoded = SamlResponseTestFixture.buildEncodedResponse(
            validParamsFor(orgAId, requestIdForOrgA, "crossorg@example.com"));

        // Posting it to org B's ACS endpoint must be rejected — org B never
        // issued this request (see SamlAuthenticationService's own javadoc
        // on organization isolation).
        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgBId, encoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class);
        assertThat(fakeKeycloak().createUserCalls).isEmpty();
    }

    @Test
    void aSamlLoginNeverGrantsOrganizationAdminEvenWhenTheAssertionClaimsAnAdminGroup() throws Exception {
        Long orgId = newOrgWithEnabledProvider("ROLE-MAP");
        String requestId = issueRequestId(orgId);
        String email = "wouldbeadmin@example.com";

        SamlResponseTestFixture.Params params = validParamsFor(orgId, requestId, email);
        params.withAttributes(Map.of(
            "email", email, "firstName", "Would", "lastName", "BeAdmin",
            "groups", "Administrators", "role", "ORG_ADMIN"));
        String encoded = SamlResponseTestFixture.buildEncodedResponse(params);

        samlAuthenticationService.handleAcs(orgId, encoded, new MockHttpServletResponse());

        var customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow();
        var membership = organizationMemberRepository.findByOrganizationIdAndCustomerId(orgId, customer.getId())
            .orElseThrow();
        assertThat(membership.getOrgRole()).isEqualTo(OrgRole.MEMBER);
    }

    @Test
    void aDeactivatedMembershipIsDeniedEvenWithAValidAssertion() throws Exception {
        Long orgId = newOrgWithEnabledProvider("DEACTIVATED");
        String email = "removed@example.com";

        String firstRequestId = issueRequestId(orgId);
        samlAuthenticationService.handleAcs(orgId,
            SamlResponseTestFixture.buildEncodedResponse(validParamsFor(orgId, firstRequestId, email)),
            new MockHttpServletResponse());

        var customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow();
        var membership = organizationMemberRepository.findByOrganizationIdAndCustomerId(orgId, customer.getId())
            .orElseThrow();
        membership.setStatus(MembershipStatus.INACTIVE);
        organizationMemberRepository.save(membership);

        String secondRequestId = issueRequestId(orgId);
        String secondEncoded = SamlResponseTestFixture.buildEncodedResponse(validParamsFor(orgId, secondRequestId, email));
        assertThatThrownBy(() -> samlAuthenticationService.handleAcs(orgId, secondEncoded, new MockHttpServletResponse()))
            .isInstanceOf(SamlLoginException.class)
            .hasMessageContaining("deactivated");
    }
}
