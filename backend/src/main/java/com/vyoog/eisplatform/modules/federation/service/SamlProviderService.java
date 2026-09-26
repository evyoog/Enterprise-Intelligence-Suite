package com.vyoog.eisplatform.modules.federation.service;

import com.onelogin.saml2.settings.IdPMetadataParser;
import com.onelogin.saml2.settings.Metadata;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Util;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.federation.dto.*;
import com.vyoog.eisplatform.modules.federation.model.SamlIdentityProvider;
import com.vyoog.eisplatform.modules.federation.dto.ClaimMappingDto;
import com.vyoog.eisplatform.modules.federation.model.ClaimMapping;
import com.vyoog.eisplatform.modules.federation.repository.OidcIdentityProviderRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlIdentityProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 4 (2026.3.3): Identity Federation foundation — organization-owned
 * SAML IdP configuration (metadata parsing, SP metadata generation,
 * certificate validation) and nothing about the actual login flow yet
 * (that's Phase 5's job, once this exists to build on). This app is always
 * the SAML Service Provider; see {@link SamlIdentityProvider}'s own javadoc.
 *
 * <p>Every piece of XML this class touches originates from a third party
 * (a customer's own IdP, or whatever an org admin pastes) — {@link SamlXmlSecurity}
 * is applied before any of it reaches the SAML library, and the OneLogin
 * java-saml-core library itself (not a hand-rolled parser) does the actual
 * SAML-aware parsing/certificate handling — see this class's own method
 * javadocs for exactly which library call does what.
 */
@Service
@RequiredArgsConstructor
public class SamlProviderService {

    private final SamlIdentityProviderRepository repository;
    private final OidcIdentityProviderRepository oidcProviderRepository;
    private final AuditService auditService;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Value("${app.backend-url}")
    private String backendUrl;

    @Transactional(readOnly = true)
    public List<SamlProviderDto> listForOrganization(Long organizationId) {
        return repository.findByOrganizationId(organizationId).stream().map(this::toDto).toList();
    }

    @Transactional
    public SamlProviderDto create(Long organizationId, CreateSamlProviderRequest request) {
        SamlIdentityProvider provider = new SamlIdentityProvider();
        provider.setOrganizationId(organizationId);
        provider.setName(request.name());
        provider.setEnabled(false);
        provider.setCreatedAt(Instant.now());
        applyFields(provider, request.metadataXml(), request.entityId(), request.ssoUrl(), request.certificatePem());
        provider.setUpdatedAt(Instant.now());
        provider = repository.save(provider);

        auditService.recordSuccess("SAML_PROVIDER_CREATED", null, null, null,
            "SamlIdentityProvider", String.valueOf(provider.getId()), organizationId,
            "Created SAML provider '" + provider.getName() + "' for organization " + organizationId);
        return toDto(provider);
    }

    @Transactional
    public SamlProviderDto update(Long organizationId, Long id, UpdateSamlProviderRequest request) {
        SamlIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        if (request.name() != null && !request.name().isBlank()) {
            provider.setName(request.name());
        }
        if (request.metadataXml() != null || request.entityId() != null || request.ssoUrl() != null || request.certificatePem() != null) {
            applyFields(provider, request.metadataXml(), request.entityId(), request.ssoUrl(), request.certificatePem());
        }
        provider.setUpdatedAt(Instant.now());
        provider = repository.save(provider);

        auditService.recordSuccess("SAML_PROVIDER_UPDATED", null, null, null,
            "SamlIdentityProvider", String.valueOf(id), organizationId, "Updated SAML provider " + id);
        return toDto(provider);
    }

    /**
     * Enforces the "at most one enabled provider per organization" rule at
     * the application level too (not just the DB's partial unique index) —
     * so a caller gets a clear error message instead of a raw constraint
     * violation, and so ENABLING one atomically disables whichever other
     * provider in this org was previously enabled, in the same transaction.
     *
     * <p>The explicit {@code saveAndFlush} on the disable step is load-bearing,
     * not decorative — found live against real Postgres (never caught by the
     * H2-backed test suite, since H2's auto-generated test schema has no
     * partial unique index to violate at all): without an explicit flush,
     * Hibernate is free to order the two pending UPDATEs however it likes at
     * end-of-transaction flush time, and flushing "enable the new one"
     * before "disable the old one" trips the DB's own
     * {@code idx_saml_idp_one_enabled_per_org} partial unique index —
     * Postgres checks a non-deferred unique constraint per-statement, not
     * at commit, so even a single transaction can violate it if the two
     * writes land in the wrong order within it.
     */
    @Transactional
    public SamlProviderDto setEnabled(Long organizationId, Long id, boolean enabled) {
        SamlIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        if (enabled) {
            repository.findByOrganizationIdAndEnabledTrue(organizationId)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    existing.setEnabled(false);
                    existing.setUpdatedAt(Instant.now());
                    repository.saveAndFlush(existing);
                });
            // C27: one enabled provider per organization across SAML and OIDC.
            oidcProviderRepository.findByOrganizationIdAndEnabledTrue(organizationId).ifPresent(oidc -> {
                oidc.setEnabled(false);
                oidc.setUpdatedAt(Instant.now());
                oidcProviderRepository.saveAndFlush(oidc);
            });
        }
        provider.setEnabled(enabled);
        provider.setUpdatedAt(Instant.now());
        provider = repository.save(provider);

        auditService.recordSuccess(enabled ? "SAML_PROVIDER_ENABLED" : "SAML_PROVIDER_DISABLED", null, null, null,
            "SamlIdentityProvider", String.valueOf(id), organizationId, null);
        return toDto(provider);
    }

    @Transactional
    public void delete(Long organizationId, Long id) {
        SamlIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        repository.delete(provider);
        auditService.recordSuccess("SAML_PROVIDER_DELETED", null, null, null,
            "SamlIdentityProvider", String.valueOf(id), organizationId, "Deleted SAML provider " + provider.getName());
    }

    /**
     * Phase 4 (foundation) scope — see {@link SamlProviderTestResultDto}'s
     * own javadoc for exactly what this does and doesn't verify. Never
     * throws on a failed check; every outcome is reported back in the
     * result so a bad configuration is diagnosable rather than a 500.
     */
    @Transactional(readOnly = true)
    public SamlProviderTestResultDto test(Long organizationId, Long id) {
        SamlIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        List<String> checks = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            X509Certificate cert = Util.loadCert(provider.getCertificatePem());
            cert.checkValidity();
            checks.add("Certificate is well-formed and currently valid (expires " + cert.getNotAfter().toInstant() + ").");
        } catch (CertificateExpiredException e) {
            errors.add("The IdP certificate has expired.");
        } catch (CertificateNotYetValidException e) {
            errors.add("The IdP certificate is not valid yet.");
        } catch (CertificateException e) {
            errors.add("The IdP certificate could not be parsed: " + e.getMessage());
        }

        try {
            new URI(provider.getSsoUrl()).toURL();
            checks.add("SSO URL is a well-formed, absolute URL.");
        } catch (Exception e) {
            errors.add("The SSO URL is not a valid absolute URL: " + e.getMessage());
        }

        if (provider.getEntityId() == null || provider.getEntityId().isBlank()) {
            errors.add("Entity ID is missing.");
        } else {
            checks.add("Entity ID is present.");
        }

        // Best-effort reachability only — a real IdP's SSO endpoint commonly
        // rejects a bare GET with 4xx/5xx (it expects a real SAML request),
        // so this only fails the check for a genuine connection failure
        // (DNS/connect/timeout), never a non-2xx status.
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(provider.getSsoUrl()))
                .timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            checks.add("SSO URL responded to a connectivity check (HTTP " + response.statusCode() + ").");
        } catch (Exception e) {
            String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            errors.add("Could not reach the SSO URL: " + reason);
        }

        return new SamlProviderTestResultDto(errors.isEmpty(), checks, errors);
    }

    /**
     * This platform's own SP metadata for one organization — a real,
     * externally-fetchable SAML metadata document an IdP admin configures
     * their side FROM, generated by the SAML library's own
     * {@link Metadata} class (never hand-built XML). Deliberately
     * per-organization (its own ACS URL, path-scoped by organization id) so
     * Phase 5's login callback can unambiguously resolve which organization
     * (and therefore which {@link SamlIdentityProvider}) an incoming
     * response belongs to, purely from the URL it arrived on — never from
     * anything inside the (unauthenticated, pre-login) SAML response itself.
     * Left unsigned deliberately: this app has no SP signing keypair
     * provisioned, and signed SP metadata is optional in the SAML spec, not
     * a hard requirement most IdPs impose.
     */
    @Transactional(readOnly = true)
    public String getSpMetadataXml(Long organizationId) {
        Map<String, Object> values = new HashMap<>();
        values.put(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, spEntityId(organizationId));
        values.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY, acsUrl(organizationId));
        values.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_BINDING_PROPERTY_KEY, "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        values.put(SettingsBuilder.SP_NAMEIDFORMAT_PROPERTY_KEY, "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        values.put(SettingsBuilder.STRICT_PROPERTY_KEY, true);
        Saml2Settings settings = new SettingsBuilder().fromValues(values).build();
        try {
            return new Metadata(settings).getMetadataString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate SP metadata", e);
        }
    }

    /** Package-visible (not private): SamlAuthenticationService (Phase 5)
     * needs the exact same SP entity id when building the AuthnRequest and
     * validating an incoming Response, so both call this one method rather
     * than each constructing the URL independently. */
    String spEntityId(Long organizationId) {
        return backendUrl + "/saml/" + organizationId + "/metadata";
    }

    /** Package-visible for the same reason as {@link #spEntityId} — the ACS
     * URL used here MUST match the one an IdP was actually configured
     * against (from this same class's SP metadata), or assertion validation
     * (Destination/Recipient checks) fails. */
    String acsUrl(Long organizationId) {
        return backendUrl + "/saml/" + organizationId + "/acs";
    }

    /**
     * Fills entity id / SSO URL / certificate either by parsing real IdP
     * metadata XML (via {@link IdPMetadataParser}, the library's own
     * metadata-aware parser — never a hand-rolled XML reader) or from
     * directly-supplied manual fields — see CreateSamlProviderRequest's own
     * javadoc for which one wins when both are present.
     */
    private void applyFields(SamlIdentityProvider provider, String metadataXml, String entityId, String ssoUrl, String certificatePem) {
        if (metadataXml != null && !metadataXml.isBlank()) {
            SamlXmlSecurity.rejectUnsafeXml(metadataXml);
            try {
                Document document = Util.convertStringToDocument(metadataXml);
                Map<String, Object> parsed = IdPMetadataParser.parseXML(document);
                Saml2Settings settings = new SettingsBuilder().fromValues(parsed).build();

                String parsedEntityId = settings.getIdpEntityId();
                URL parsedSsoUrl = settings.getIdpSingleSignOnServiceUrl();
                X509Certificate parsedCert = settings.getIdpx509cert();

                if (parsedEntityId == null || parsedEntityId.isBlank()) {
                    throw new IllegalArgumentException("Could not find an IdP entity id in the provided metadata");
                }
                if (parsedSsoUrl == null) {
                    throw new IllegalArgumentException("Could not find an IdP SSO URL in the provided metadata");
                }
                if (parsedCert == null) {
                    throw new IllegalArgumentException("Could not find an IdP signing certificate in the provided metadata");
                }

                provider.setEntityId(parsedEntityId);
                provider.setSsoUrl(parsedSsoUrl.toString());
                provider.setCertificatePem(Util.convertToPem(parsedCert));
                provider.setMetadataXml(metadataXml);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not parse the provided IdP metadata: " + e.getMessage(), e);
            }
            return;
        }

        if (entityId == null || entityId.isBlank() || ssoUrl == null || ssoUrl.isBlank()
            || certificatePem == null || certificatePem.isBlank()) {
            throw new IllegalArgumentException(
                "Provide either metadataXml, or all three of entityId/ssoUrl/certificatePem.");
        }
        try {
            new URI(ssoUrl).toURL();
        } catch (Exception e) {
            throw new IllegalArgumentException("ssoUrl is not a valid absolute URL");
        }
        try {
            Util.loadCert(certificatePem);
        } catch (CertificateException e) {
            throw new IllegalArgumentException("certificatePem could not be parsed as an X.509 certificate: " + e.getMessage());
        }
        provider.setEntityId(entityId);
        provider.setSsoUrl(ssoUrl);
        provider.setCertificatePem(certificatePem);
        provider.setMetadataXml(null);
    }

    private SamlIdentityProvider findOwnedOrThrow(Long organizationId, Long id) {
        SamlIdentityProvider provider = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SAML provider not found"));
        if (!provider.getOrganizationId().equals(organizationId)) {
            // Same anti-enumeration posture as OrganizationSelfService's own
            // cross-organization checks — never confirm a provider id exists
            // in someone else's organization.
            throw new ForbiddenException("You do not have permission to do this");
        }
        return provider;
    }

    private SamlProviderDto toDto(SamlIdentityProvider provider) {
        Instant expiresAt = null;
        boolean expired = false;
        try {
            X509Certificate cert = Util.loadCert(provider.getCertificatePem());
            expiresAt = cert.getNotAfter().toInstant();
            expired = expiresAt.isBefore(Instant.now());
        } catch (CertificateException ignored) {
            // Surfaced via fingerprint being null below, not thrown here —
            // a list view should never 500 because one row's cert is malformed.
        }
        return new SamlProviderDto(
            provider.getId(),
            provider.getName(),
            provider.getEntityId(),
            provider.getSsoUrl(),
            provider.getCertificatePem(),
            fingerprint(provider.getCertificatePem()),
            expiresAt,
            expired,
            provider.isEnabled(),
            provider.getCreatedAt(),
            provider.getUpdatedAt(),
            ClaimMapping.orEmpty(provider.getClaimMapping()).toDto()
        );
    }

    /** REQ-IAM-007 (C28): replace the provider's claim mapping; blank = default. */
    @Transactional
    public SamlProviderDto updateClaimMapping(Long organizationId, Long id, ClaimMappingDto request) {
        SamlIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        ClaimMapping mapping = ClaimMapping.orEmpty(provider.getClaimMapping());
        mapping.apply(request);
        provider.setClaimMapping(mapping);
        provider.setUpdatedAt(Instant.now());
        provider = repository.save(provider);
        auditService.recordSuccess("SAML_CLAIM_MAPPING_UPDATED", null, null, null,
            "SamlIdentityProvider", String.valueOf(id), organizationId, null);
        return toDto(provider);
    }

    private static String fingerprint(String certificatePem) {
        try {
            X509Certificate cert = Util.loadCert(certificatePem);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cert.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X:", b));
            }
            return sb.substring(0, sb.length() - 1);
        } catch (Exception e) {
            return null;
        }
    }
}
