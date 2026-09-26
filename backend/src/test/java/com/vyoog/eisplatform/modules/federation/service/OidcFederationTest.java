package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.auth.service.FakeImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.ImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.federation.dto.CreateSamlProviderRequest;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderDto;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderRequest;
import com.vyoog.eisplatform.modules.federation.model.OidcLoginRequest;
import com.vyoog.eisplatform.modules.federation.repository.OidcIdentityProviderRepository;
import com.vyoog.eisplatform.modules.federation.repository.OidcLoginRequestRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlIdentityProviderRepository;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * REQ-IAM-006 (C22/C27): OIDC provider management and sign-in, end to end
 * against an in-memory identity provider that signs real RS256 ID tokens.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(OidcFederationTest.TestConfig.class)
class OidcFederationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean @Primary KeycloakAdminClient fakeKeycloakAdminClient() { return new FakeKeycloakAdminClient(); }
        @Bean @Primary ImpersonationExchangeService fakeImpersonationExchangeService() { return new FakeImpersonationExchangeService(); }
        @Bean @Primary OidcProviderClient fakeOidcProviderClient() { return new FakeOidcProviderClient(); }
    }

    private static final String CLIENT_ID = "eis-client";

    @Autowired private OidcProviderService providerService;
    @Autowired private OidcAuthenticationService authService;
    @Autowired private SamlAuthenticationService samlAuthenticationService;
    @Autowired private SamlProviderService samlProviderService;
    @Autowired private OidcIdentityProviderRepository oidcRepository;
    @Autowired private SamlIdentityProviderRepository samlRepository;
    @Autowired private OidcLoginRequestRepository loginRequestRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private KeycloakAdminClient keycloakAdminClient;
    @Autowired private ImpersonationExchangeService impersonationExchangeService;
    @Autowired private OidcProviderClient oidcClient;

    private FakeOidcProviderClient idp() { return (FakeOidcProviderClient) oidcClient; }

    @BeforeEach
    void reset() {
        ((FakeKeycloakAdminClient) keycloakAdminClient).reset();
        ((FakeImpersonationExchangeService) impersonationExchangeService).reset();
        idp().reset();
    }

    private Organization org(boolean mfaRequired) {
        Organization org = new Organization();
        org.setName("OIDC Org");
        org.setCode("OIDC-" + System.nanoTime());
        org.setBusinessEmail("biz@oidc.example");
        org.setCountry("India");
        org.setLicensedSeats(10);
        org.setMfaRequired(mfaRequired);
        return organizationRepository.save(org);
    }

    private OidcProviderRequest request(String secret) {
        return new OidcProviderRequest("Azure AD", FakeOidcProviderClient.ISSUER, CLIENT_ID, secret, null);
    }

    private Long enabledProvider(Organization org) {
        OidcProviderDto p = providerService.create(org.getId(), request("s3cret"));
        providerService.setEnabled(org.getId(), p.id(), true);
        return p.id();
    }

    /** Starts sign-in and returns the stored request (state, nonce, verifier). */
    private OidcLoginRequest start(Organization org) {
        String url = authService.buildRedirectUrl(org.getId());
        assertThat(url).startsWith(FakeOidcProviderClient.ISSUER + "/authorize?response_type=code")
            .contains("client_id=" + CLIENT_ID).contains("code_challenge_method=S256").contains("scope=openid+email+profile");
        List<OidcLoginRequest> rows = loginRequestRepository.findByOrganizationId(org.getId());
        return rows.stream().filter(r -> url.contains("state=" + r.getState())).findFirst().orElseThrow();
    }

    // ---------------------------------------------------------------- provider management

    @Test
    void theClientSecretIsStoredEncryptedAndNeverReturned() {
        Organization org = org(false);
        OidcProviderDto created = providerService.create(org.getId(), request("s3cret"));
        assertThat(created.clientSecretSet()).isTrue();
        assertThat(created.scopes()).isEqualTo("openid email profile");
        assertThat(created.redirectUri()).endsWith("/oidc/" + org.getId() + "/callback");
        String stored = oidcRepository.findById(created.id()).orElseThrow().getEncryptedClientSecret();
        assertThat(stored).isNotBlank().doesNotContain("s3cret");

        providerService.update(org.getId(), created.id(), new OidcProviderRequest("Renamed", FakeOidcProviderClient.ISSUER, CLIENT_ID, "", null));
        assertThat(oidcRepository.findById(created.id()).orElseThrow().getEncryptedClientSecret()).isEqualTo(stored);
    }

    @Test
    void invalidConfigurationIsRefused() {
        Organization org = org(false);
        assertThatThrownBy(() -> providerService.create(org.getId(), request(null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> providerService.create(org.getId(),
            new OidcProviderRequest("x", "http://idp.example", CLIENT_ID, "s", null))).hasMessageContaining("https");
        assertThatThrownBy(() -> providerService.create(org.getId(),
            new OidcProviderRequest("x", FakeOidcProviderClient.ISSUER, CLIENT_ID, "s", "email profile"))).hasMessageContaining("openid");
        Long id = providerService.create(org.getId(), request("s")).id();
        assertThatThrownBy(() -> providerService.setEnabled(org(false).getId(), id, true))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ForbiddenException.class);
        assertThatThrownBy(() -> providerService.setEnabled(org.getId(), -1L, true)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void onlyOneSamlOrOidcProviderIsEnabledPerOrganization() throws Exception {
        Organization org = org(false);
        var saml = samlProviderService.create(org.getId(), new CreateSamlProviderRequest(
            "SAML IdP", null, "https://saml.example/entity", "https://saml.example/sso", SamlResponseTestFixture.certificatePem()));
        samlProviderService.setEnabled(org.getId(), saml.id(), true);

        Long oidcId = enabledProvider(org);
        assertThat(samlRepository.findById(saml.id()).orElseThrow().isEnabled()).isFalse();
        assertThat(samlAuthenticationService.checkSso(org.getCode()).protocol()).isEqualTo("OIDC");

        samlProviderService.setEnabled(org.getId(), saml.id(), true);
        assertThat(oidcRepository.findById(oidcId).orElseThrow().isEnabled()).isFalse();
        assertThat(samlAuthenticationService.checkSso(org.getCode()).protocol()).isEqualTo("SAML");
    }

    @Test
    void testReportsDiscoveryIssuerAndEndpoints() {
        Organization org = org(false);
        Long id = providerService.create(org.getId(), request("s")).id();
        assertThat(providerService.test(org.getId(), id).success()).isTrue();

        idp().discovery = new OidcProviderClient.Discovery("https://other.example", null, FakeOidcProviderClient.ISSUER + "/token", null);
        var result = providerService.test(org.getId(), id);
        assertThat(result.success()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("does not match")).anyMatch(e -> e.contains("authorization endpoint"));

        idp().discoveryFails = true;
        assertThat(providerService.test(org.getId(), id).errors()).anyMatch(e -> e.contains("404"));
    }

    // ---------------------------------------------------------------- sign-in

    @Test
    void aValidSignInProvisionsAMemberAndFinalizesTheSessionAndTheSameSubjectMapsBackToTheSameCustomer() {
        Organization org = org(false);
        enabledProvider(org);
        String email = "ada-" + System.nanoTime() + "@example.com";

        OidcLoginRequest first = start(org);
        idp().validClaims(CLIENT_ID, first.getNonce(), "sub-1", email, null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String redirect = authService.handleCallback(org.getId(), "code-1", first.getState(), null, response);

        assertThat(redirect).isEqualTo("http://localhost:5173");
        assertThat(response.getHeaders("Set-Cookie")).anyMatch(c -> c.startsWith("eis_rt="));
        assertThat(idp().lastClientSecret).isEqualTo("s3cret");
        assertThat(OidcAuthenticationService.codeChallenge(idp().lastCodeVerifier)).isNotBlank();
        var customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(customer.getFirstName()).isEqualTo("Ada");
        var membership = memberRepository.findByOrganizationIdAndCustomerId(org.getId(), customer.getId()).orElseThrow();
        assertThat(membership.getOrgRole()).isEqualTo(OrgRole.MEMBER);

        OidcLoginRequest second = start(org);
        idp().validClaims(CLIENT_ID, second.getNonce(), "sub-1", "changed-" + email, null);
        authService.handleCallback(org.getId(), "code-2", second.getState(), null, new MockHttpServletResponse());
        assertThat(customerRepository.findByEmailIgnoreCase("changed-" + email)).isEmpty();
        assertThat(((FakeKeycloakAdminClient) keycloakAdminClient).createUserCalls).hasSize(1);
    }

    @Test
    void tamperedOrMismatchedTokensAreRefused() {
        Organization org = org(false);
        enabledProvider(org);

        OidcLoginRequest r1 = start(org);
        idp().validClaims(CLIENT_ID, "wrong-nonce", "s", "n@example.com", null);
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r1.getState(), null, new MockHttpServletResponse()))
            .isInstanceOf(OidcLoginException.class).hasMessageContaining("nonce");

        OidcLoginRequest r2 = start(org);
        idp().validClaims("someone-else", r2.getNonce(), "s", "n@example.com", null);
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r2.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("audience");

        OidcLoginRequest r3 = start(org);
        idp().validClaims(CLIENT_ID, r3.getNonce(), "s", "n@example.com", c -> c.put("iss", "https://evil.example"));
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r3.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("issuer");

        OidcLoginRequest r4 = start(org);
        idp().validClaims(CLIENT_ID, r4.getNonce(), "s", "n@example.com", null);
        idp().signWithOtherKey = true;
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r4.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("could not be verified");

        idp().signWithOtherKey = false;
        OidcLoginRequest r5 = start(org);
        idp().validClaims(CLIENT_ID, r5.getNonce(), "s", "n@example.com", c -> c.put("exp", Instant.now().minusSeconds(600)));
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r5.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("could not be verified");

        OidcLoginRequest r6 = start(org);
        idp().validClaims(CLIENT_ID, r6.getNonce(), "s", null, c -> c.remove("email"));
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r6.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("email");
    }

    @Test
    void stateIsSingleUseAndBoundToTheOrganization() {
        Organization org = org(false);
        Organization other = org(false);
        enabledProvider(org);
        OidcLoginRequest r = start(org);
        idp().validClaims(CLIENT_ID, r.getNonce(), "replay", "replay-" + System.nanoTime() + "@example.com", null);

        assertThatThrownBy(() -> authService.handleCallback(other.getId(), "c", r.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("not recognized");
        authService.handleCallback(org.getId(), "c", r.getState(), null, new MockHttpServletResponse());
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("already been used");
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", "unknown", null, new MockHttpServletResponse()))
            .hasMessageContaining("not recognized");

        OidcLoginRequest denied = start(org);
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), null, denied.getState(), "access_denied", new MockHttpServletResponse()))
            .hasMessageContaining("access_denied");
    }

    @Test
    void theOrganizationMfaPolicyAppliesAfterOidcSignIn() {
        Organization org = org(true);
        enabledProvider(org);
        OidcLoginRequest r = start(org);
        idp().validClaims(CLIENT_ID, r.getNonce(), "mfa-sub", "mfa-" + System.nanoTime() + "@example.com", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String redirect = authService.handleCallback(org.getId(), "c", r.getState(), null, response);

        assertThat(URLDecoder.decode(redirect, StandardCharsets.UTF_8)).contains("?mfaEnroll=");
        assertThat(response.getHeaders("Set-Cookie")).noneMatch(c -> c.startsWith("eis_rt="));
    }

    @Test
    void aDeactivatedMembershipIsRefused() {
        Organization org = org(false);
        enabledProvider(org);
        String email = "gone-" + System.nanoTime() + "@example.com";
        OidcLoginRequest r1 = start(org);
        idp().validClaims(CLIENT_ID, r1.getNonce(), "gone-sub", email, null);
        authService.handleCallback(org.getId(), "c", r1.getState(), null, new MockHttpServletResponse());
        var customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow();
        var member = memberRepository.findByOrganizationIdAndCustomerId(org.getId(), customer.getId()).orElseThrow();
        member.setStatus(MembershipStatus.INACTIVE);
        memberRepository.save(member);

        OidcLoginRequest r2 = start(org);
        idp().validClaims(CLIENT_ID, r2.getNonce(), "gone-sub", email, null);
        assertThatThrownBy(() -> authService.handleCallback(org.getId(), "c", r2.getState(), null, new MockHttpServletResponse()))
            .hasMessageContaining("deactivated");
    }

    @Test
    void signInNeedsAnEnabledProvider() {
        Organization org = org(false);
        providerService.create(org.getId(), request("s"));
        assertThatThrownBy(() -> authService.buildRedirectUrl(org.getId())).isInstanceOf(OidcLoginException.class);
        assertThat(samlAuthenticationService.checkSso(org.getCode()).available()).isFalse();
    }
}
