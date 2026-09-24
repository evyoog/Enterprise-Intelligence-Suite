package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.federation.dto.CreateSamlProviderRequest;
import com.vyoog.eisplatform.modules.federation.dto.SamlProviderDto;
import com.vyoog.eisplatform.modules.federation.dto.SamlProviderTestResultDto;
import com.vyoog.eisplatform.modules.federation.dto.UpdateSamlProviderRequest;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 4 (2026.3.3): every assertion here runs against REAL SAML metadata
 * XML containing a REAL, freshly-generated (openssl) self-signed X.509
 * certificate — never a fabricated/truncated stand-in — parsed by the real
 * OneLogin java-saml-core library, the same one production code uses.
 */
@SpringBootTest
@ActiveProfiles("test")
class SamlProviderServiceTest {

    @Autowired
    private SamlProviderService samlProviderService;
    @Autowired
    private OrganizationRepository organizationRepository;

    private Long newOrganization() {
        Organization organization = new Organization();
        organization.setName("SAML Test Org " + System.nanoTime());
        organization.setCode("SAML-TEST-" + System.nanoTime());
        organization.setBusinessEmail("biz-" + System.nanoTime() + "@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        return organizationRepository.save(organization).getId();
    }

    /** A real, valid (10-year) self-signed certificate, base64-encoded, no
     * PEM headers — generated via {@code openssl req -x509 -newkey rsa:2048}. */
    private static final String REAL_CERT_B64 =
        "MIIDTTCCAjWgAwIBAgIUJ03pL1gABrPCwyS5bGjDTqyJVjgwDQYJKoZIhvcNAQELBQAwNjERMA8GA1UEAwwIVGVzdCBJZFAxFDASBgNVBAoMC0NsYXVkZSBUZXN0MQswCQYDVQQGEwJJTjAeFw0yNjA5MTgwODM2NDBaFw0zNjA5MTUwODM2NDBaMDYxETAPBgNVBAMMCFRlc3QgSWRQMRQwEgYDVQQKDAtDbGF1ZGUgVGVzdDELMAkGA1UEBhMCSU4wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC03PatDujIle1Pg8Bm7zXjpzFfkSjtkfvsJLwkdzwHxfRdo4mY93v6zTU+VcS+MHysCOZtK+zhwCiHT04ODVk6O87pE/H3gEnLm7ATSdNQv05wUrW9zibTidefIFlJPKALGluRjXJToABu5rZcddrOQwfRFSJ11pYCONbOum4zy1PXdBtDzIRB0n8rqwTKM78UGm5n5adEhZssLvGuR3ARnlI6UqbasrJlneCbjogd02swz8BUY+GpR848BC7qlO4UVgrh4PDr1lT3fWem2PuNhjSz0BfY2wrrRzSxiww/awzp+2vkfibMUKl4Nz26w4JdUqys4FfHwG0Fu88rRf7RAgMBAAGjUzBRMB0GA1UdDgQWBBT0Z/7P/cirgh4lyl/TmnWccWNk6zAfBgNVHSMEGDAWgBT0Z/7P/cirgh4lyl/TmnWccWNk6zAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBJqAkw0qzQWpWL7NHk8oFPHcVB6aRRWOzvsME6nbE7Rc8MJ8X+9lcaYMzO0hzIVNQGr77S/5KN40vRglHIdN9hCN3v+e+SpfeQIIM6lf4kWuwjO2QyBchwJoLsOnFWXw0fJi2sCmiLSNrjbCyGZln0n23JETQbl6SY8gbd9ru/S9XeWJ/Eob8BIVORTOPHGT2m70oyx7swR5XBvoeWQEKMDyxGtDK84chvbWW52WY27qLgfK+HBEiF4TdGawjoUAWLqfQPqLNFRzP/yZ2ci8HHzw9WwI1eXcMlVh0E7AXpkk24I14ZxYNRRXiEXvm/zOTSLx1JMDyORByXD24Z8TDd";

    private static String realIdpMetadata() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"https://claude-test-idp.example.com/saml/metadata\">\n"
            + "  <IDPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n"
            + "    <KeyDescriptor use=\"signing\">\n"
            + "      <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n"
            + "        <ds:X509Data>\n"
            + "          <ds:X509Certificate>" + REAL_CERT_B64 + "</ds:X509Certificate>\n"
            + "        </ds:X509Data>\n"
            + "      </ds:KeyInfo>\n"
            + "    </KeyDescriptor>\n"
            + "    <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"https://claude-test-idp.example.com/saml/sso\"/>\n"
            + "    <SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"https://claude-test-idp.example.com/saml/sso\"/>\n"
            + "  </IDPSSODescriptor>\n"
            + "</EntityDescriptor>\n";
    }

    private static String pemCert() {
        StringBuilder sb = new StringBuilder("-----BEGIN CERTIFICATE-----\n");
        String c = REAL_CERT_B64;
        for (int i = 0; i < c.length(); i += 64) {
            sb.append(c, i, Math.min(i + 64, c.length())).append('\n');
        }
        return sb.append("-----END CERTIFICATE-----\n").toString();
    }

    @Test
    void creatingFromRealMetadataXmlExtractsEntityIdSsoUrlAndCertificate() {
        SamlProviderDto dto = samlProviderService.create(newOrganization(),
            new CreateSamlProviderRequest("Test IdP", realIdpMetadata(), null, null, null));

        assertThat(dto.entityId()).isEqualTo("https://claude-test-idp.example.com/saml/metadata");
        assertThat(dto.ssoUrl()).isEqualTo("https://claude-test-idp.example.com/saml/sso");
        assertThat(dto.certificatePem()).contains("BEGIN CERTIFICATE");
        assertThat(dto.certificateFingerprint()).isNotBlank();
        assertThat(dto.certificateExpired()).isFalse();
        assertThat(dto.enabled()).isFalse();
    }

    @Test
    void creatingFromManualFieldsWorksTooAndValidatesTheCertificate() {
        SamlProviderDto dto = samlProviderService.create(newOrganization(),
            new CreateSamlProviderRequest("Manual IdP", null,
                "https://manual-idp.example.com/metadata", "https://manual-idp.example.com/sso", pemCert()));

        assertThat(dto.entityId()).isEqualTo("https://manual-idp.example.com/metadata");
        assertThat(dto.certificateExpired()).isFalse();
    }

    @Test
    void rejectsManualFieldsWithAnUnparseableCertificate() {
        Long org = newOrganization();
        assertThatThrownBy(() -> samlProviderService.create(org,
            new CreateSamlProviderRequest("Bad Cert", null, "https://x.example.com/m", "https://x.example.com/sso", "not-a-real-cert")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMetadataXmlThatDeclaresADoctype() {
        String malicious = "<?xml version=\"1.0\"?>\n"
            + "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>\n"
            + "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"&xxe;\"></EntityDescriptor>";

        Long org = newOrganization();
        assertThatThrownBy(() -> samlProviderService.create(org,
            new CreateSamlProviderRequest("Malicious", malicious, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMetadataMissingAnSsoUrl() {
        String noSso = "<?xml version=\"1.0\"?>\n"
            + "<EntityDescriptor xmlns=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"https://no-sso.example.com\">\n"
            + "  <IDPSSODescriptor protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n"
            + "    <KeyDescriptor use=\"signing\">\n"
            + "      <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><ds:X509Data><ds:X509Certificate>" + REAL_CERT_B64 + "</ds:X509Certificate></ds:X509Data></ds:KeyInfo>\n"
            + "    </KeyDescriptor>\n"
            + "  </IDPSSODescriptor>\n"
            + "</EntityDescriptor>";

        Long org = newOrganization();
        assertThatThrownBy(() -> samlProviderService.create(org,
            new CreateSamlProviderRequest("No SSO", noSso, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresEitherMetadataOrAllThreeManualFields() {
        Long org = newOrganization();
        assertThatThrownBy(() -> samlProviderService.create(org,
            new CreateSamlProviderRequest("Incomplete", null, "https://x.example.com", null, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void enablingOneProviderDisablesTheOrganizationsPreviouslyEnabledOne() {
        Long org = newOrganization();
        SamlProviderDto first = samlProviderService.create(org, new CreateSamlProviderRequest("First", realIdpMetadata(), null, null, null));
        SamlProviderDto second = samlProviderService.create(org, new CreateSamlProviderRequest("Second", null,
            "https://second-idp.example.com", "https://second-idp.example.com/sso", pemCert()));

        SamlProviderDto firstEnabled = samlProviderService.setEnabled(org, first.id(), true);
        assertThat(firstEnabled.enabled()).isTrue();

        SamlProviderDto secondEnabled = samlProviderService.setEnabled(org, second.id(), true);
        assertThat(secondEnabled.enabled()).isTrue();

        SamlProviderDto firstAfter = samlProviderService.listForOrganization(org).stream()
            .filter(p -> p.id().equals(first.id())).findFirst().orElseThrow();
        assertThat(firstAfter.enabled()).isFalse();
    }

    @Test
    void cannotAccessAnotherOrganizationsProvider() {
        Long orgA = newOrganization();
        Long orgB = newOrganization();
        SamlProviderDto provider = samlProviderService.create(orgA,
            new CreateSamlProviderRequest("Org A's IdP", realIdpMetadata(), null, null, null));

        assertThatThrownBy(() -> samlProviderService.update(orgB, provider.id(), new UpdateSamlProviderRequest("renamed", null, null, null, null)))
            .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> samlProviderService.setEnabled(orgB, provider.id(), true))
            .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> samlProviderService.delete(orgB, provider.id()))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void testEndpointReportsARealValidCertificateAndUrlAsPassing() {
        Long org = newOrganization();
        SamlProviderDto provider = samlProviderService.create(org,
            new CreateSamlProviderRequest("Test target", realIdpMetadata(), null, null, null));

        SamlProviderTestResultDto result = samlProviderService.test(org, provider.id());

        assertThat(result.checks()).anyMatch(c -> c.contains("Certificate is well-formed and currently valid"));
        assertThat(result.checks()).anyMatch(c -> c.contains("SSO URL is a well-formed"));
        // The SSO URL points at a domain that doesn't exist — the
        // connectivity check must genuinely fail, not silently pass.
        assertThat(result.success()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("Could not reach the SSO URL"));
    }

    @Test
    void generatesRealSpMetadataXmlScopedToTheOrganization() {
        Long org = newOrganization();
        String xml = samlProviderService.getSpMetadataXml(org);

        assertThat(xml).contains("EntityDescriptor");
        assertThat(xml).contains("/saml/" + org + "/metadata");
        assertThat(xml).contains("/saml/" + org + "/acs");
        assertThat(xml).contains("AssertionConsumerService");
    }

    @Test
    void updatingWithNewManualFieldsReplacesTheOldConfiguration() {
        Long org = newOrganization();
        SamlProviderDto provider = samlProviderService.create(org,
            new CreateSamlProviderRequest("Original", realIdpMetadata(), null, null, null));

        SamlProviderDto updated = samlProviderService.update(org, provider.id(),
            new UpdateSamlProviderRequest("Renamed", null, "https://updated.example.com", "https://updated.example.com/sso", pemCert()));

        assertThat(updated.name()).isEqualTo("Renamed");
        assertThat(updated.entityId()).isEqualTo("https://updated.example.com");
    }

    @Test
    void deletingAProviderRemovesItFromTheList() {
        Long org = newOrganization();
        SamlProviderDto provider = samlProviderService.create(org,
            new CreateSamlProviderRequest("To delete", realIdpMetadata(), null, null, null));
        samlProviderService.delete(org, provider.id());

        assertThat(samlProviderService.listForOrganization(org)).isEmpty();
    }
}
