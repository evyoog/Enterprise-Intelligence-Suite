package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.auth.service.FakeImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.ImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.federation.dto.ClaimMappingDto;
import com.vyoog.eisplatform.modules.federation.dto.CreateSamlProviderRequest;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderRequest;
import com.vyoog.eisplatform.modules.federation.model.OidcLoginRequest;
import com.vyoog.eisplatform.modules.federation.model.SamlLoginRequest;
import com.vyoog.eisplatform.modules.federation.repository.OidcLoginRequestRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlLoginRequestRepository;
import com.vyoog.eisplatform.modules.registration.model.Organization;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-IAM-007 (C23/C28): configurable claim mapping per SAML and OIDC provider.
 * Configured name first, then the defaults; fallbacks fixed; blank = default. */
@SpringBootTest
@ActiveProfiles("test")
@Import(ClaimMappingTest.TestConfig.class)
class ClaimMappingTest {

    @TestConfiguration
    static class TestConfig {
        @Bean @Primary KeycloakAdminClient fakeKeycloakAdminClient() { return new FakeKeycloakAdminClient(); }
        @Bean @Primary ImpersonationExchangeService fakeImpersonationExchangeService() { return new FakeImpersonationExchangeService(); }
        @Bean @Primary OidcProviderClient fakeOidcProviderClient() { return new FakeOidcProviderClient(); }
    }

    private static final String IDP_ENTITY_ID = "https://claude-test-idp.example.com/saml/metadata";

    @Autowired private SamlProviderService samlProviderService;
    @Autowired private SamlAuthenticationService samlAuthenticationService;
    @Autowired private SamlLoginRequestRepository samlLoginRequestRepository;
    @Autowired private OidcProviderService oidcProviderService;
    @Autowired private OidcAuthenticationService oidcAuthenticationService;
    @Autowired private OidcLoginRequestRepository oidcLoginRequestRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private KeycloakAdminClient keycloakAdminClient;
    @Autowired private ImpersonationExchangeService impersonationExchangeService;
    @Autowired private OidcProviderClient oidcClient;

    @BeforeEach
    void reset() {
        ((FakeKeycloakAdminClient) keycloakAdminClient).reset();
        ((FakeImpersonationExchangeService) impersonationExchangeService).reset();
        ((FakeOidcProviderClient) oidcClient).reset();
    }

    private Organization org() {
        Organization org = new Organization();
        org.setName("Mapping Org");
        org.setCode("MAP-" + System.nanoTime());
        org.setBusinessEmail("biz@mapping.example");
        org.setCountry("India");
        org.setLicensedSeats(10);
        return organizationRepository.save(org);
    }

    private Long samlProvider(Organization org) throws Exception {
        var p = samlProviderService.create(org.getId(), new CreateSamlProviderRequest(
            "Test IdP", null, IDP_ENTITY_ID, "https://claude-test-idp.example.com/saml/sso", SamlResponseTestFixture.certificatePem()));
        samlProviderService.setEnabled(org.getId(), p.id(), true);
        return p.id();
    }

    private void samlSignIn(Organization org, String nameId, Map<String, String> attributes) throws Exception {
        samlAuthenticationService.buildRedirectUrl(org.getId());
        List<SamlLoginRequest> rows = samlLoginRequestRepository.findByOrganizationId(org.getId());
        SamlResponseTestFixture.Params params = new SamlResponseTestFixture.Params();
        params.withInResponseTo(rows.get(rows.size() - 1).getId())
            .withDestination(samlProviderService.acsUrl(org.getId()))
            .withIssuer(IDP_ENTITY_ID)
            .withNameId(nameId)
            .withAudience(samlProviderService.spEntityId(org.getId()))
            .withAttributes(attributes);
        samlAuthenticationService.handleAcs(org.getId(), SamlResponseTestFixture.buildEncodedResponse(params), new MockHttpServletResponse());
    }

    @Test
    void savingAMappingStoresTrimmedNamesAndBlankMeansDefault() throws Exception {
        Organization org = org();
        Long id = samlProvider(org);
        var dto = samlProviderService.updateClaimMapping(org.getId(), id, new ClaimMappingDto(" corpMail ", "", null, "fullName"));
        assertThat(dto.claimMapping()).isEqualTo(new ClaimMappingDto("corpMail", null, null, "fullName"));
        assertThat(samlProviderService.listForOrganization(org.getId()).get(0).claimMapping().email()).isEqualTo("corpMail");

        var cleared = samlProviderService.updateClaimMapping(org.getId(), id, new ClaimMappingDto(null, null, null, null));
        assertThat(cleared.claimMapping()).isEqualTo(new ClaimMappingDto(null, null, null, null));
        assertThatThrownBy(() -> samlProviderService.updateClaimMapping(org().getId(), id, new ClaimMappingDto(null, null, null, null)))
            .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> samlProviderService.updateClaimMapping(org.getId(), -1L, new ClaimMappingDto(null, null, null, null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void samlUsesTheConfiguredAttributeFirstThenTheDefaults() throws Exception {
        Organization org = org();
        Long id = samlProvider(org);
        samlProviderService.updateClaimMapping(org.getId(), id, new ClaimMappingDto("corpMail", "prenom", "nom", null));
        String corpEmail = "corp-" + System.nanoTime() + "@example.com";

        Map<String, String> attrs = new HashMap<>();
        attrs.put("corpMail", corpEmail);
        attrs.put("email", "default-" + corpEmail);
        attrs.put("prenom", "Grace");
        attrs.put("firstName", "Default");
        attrs.put("nom", "Hopper");
        samlSignIn(org, "saml-" + System.nanoTime(), attrs);

        var customer = customerRepository.findByEmailIgnoreCase(corpEmail).orElseThrow();
        assertThat(customer.getFirstName()).isEqualTo("Grace");
        assertThat(customer.getLastName()).isEqualTo("Hopper");
    }

    @Test
    void samlFallsBackToTheDefaultsWhenTheConfiguredAttributeIsMissing() throws Exception {
        Organization org = org();
        Long id = samlProvider(org);
        samlProviderService.updateClaimMapping(org.getId(), id, new ClaimMappingDto("corpMail", "prenom", null, null));
        String email = "fallback-" + System.nanoTime() + "@example.com";

        samlSignIn(org, "saml-" + System.nanoTime(), Map.of("email", email, "firstName", "Ada"));

        var customer = customerRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(customer.getFirstName()).isEqualTo("Ada");
        assertThat(customer.getLastName()).isEqualTo("SSO User");
    }

    @Test
    void oidcUsesTheConfiguredClaimsThenTheOidcDefaults() {
        Organization org = org();
        FakeOidcProviderClient idp = (FakeOidcProviderClient) oidcClient;
        var p = oidcProviderService.create(org.getId(), new OidcProviderRequest("Okta", FakeOidcProviderClient.ISSUER, "eis", "s", null));
        oidcProviderService.setEnabled(org.getId(), p.id(), true);
        var saved = oidcProviderService.updateClaimMapping(org.getId(), p.id(), new ClaimMappingDto("upn", null, null, "displayName"));
        assertThat(saved.claimMapping().email()).isEqualTo("upn");

        String upn = "upn-" + System.nanoTime() + "@example.com";
        String url = oidcAuthenticationService.buildRedirectUrl(org.getId());
        OidcLoginRequest request = oidcLoginRequestRepository.findByOrganizationId(org.getId()).stream()
            .filter(r -> url.contains("state=" + r.getState())).findFirst().orElseThrow();
        idp.validClaims("eis", request.getNonce(), "oidc-" + System.nanoTime(), "default-" + upn, c -> {
            c.put("upn", upn);
            c.remove("given_name");
            c.put("displayName", "Katherine");
            c.put("name", "Default Name");
        });
        oidcAuthenticationService.handleCallback(org.getId(), "code", request.getState(), null, new MockHttpServletResponse());

        var customer = customerRepository.findByEmailIgnoreCase(upn).orElseThrow();
        assertThat(customer.getFirstName()).isEqualTo("Katherine");
        assertThat(customer.getLastName()).isEqualTo("Lovelace");
    }
}
