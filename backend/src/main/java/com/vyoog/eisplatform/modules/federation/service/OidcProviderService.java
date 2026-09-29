package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.service.TotpSecretCipher;
import com.vyoog.eisplatform.modules.federation.dto.ClaimMappingDto;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderDto;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderRequest;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderTestResultDto;
import com.vyoog.eisplatform.modules.federation.model.ClaimMapping;
import com.vyoog.eisplatform.modules.federation.model.OidcIdentityProvider;
import com.vyoog.eisplatform.modules.federation.repository.OidcIdentityProviderRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlIdentityProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * REQ-IAM-006 (C22/C27): an organization admin (MANAGE_ORGANIZATION, checked by
 * the controller) manages the organization's OIDC providers, mirroring
 * SamlProviderService. The client secret is encrypted with TotpSecretCipher
 * and never returned. Enabling one provider disables any other enabled SAML or
 * OIDC provider of the organization.
 */
@Service
@RequiredArgsConstructor
public class OidcProviderService {

    static final String DEFAULT_SCOPES = "openid email profile";

    private final OidcIdentityProviderRepository repository;
    private final SamlIdentityProviderRepository samlRepository;
    private final TotpSecretCipher cipher;
    private final OidcProviderClient client;
    private final AuditService auditService;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Transactional(readOnly = true)
    public List<OidcProviderDto> listForOrganization(Long organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtAsc(organizationId).stream().map(this::toDto).toList();
    }

    @Transactional
    public OidcProviderDto create(Long organizationId, OidcProviderRequest request) {
        if (request.clientSecret() == null || request.clientSecret().isBlank()) {
            throw new IllegalArgumentException("A client secret is required.");
        }
        OidcIdentityProvider provider = new OidcIdentityProvider();
        provider.setOrganizationId(organizationId);
        provider.setCreatedAt(Instant.now());
        apply(provider, request);
        provider = repository.save(provider);
        auditService.recordSuccess("OIDC_PROVIDER_CREATED", null, null, null, "OidcIdentityProvider",
            String.valueOf(provider.getId()), organizationId, "Created OIDC provider " + provider.getName());
        return toDto(provider);
    }

    /** A blank client secret keeps the stored one. */
    @Transactional
    public OidcProviderDto update(Long organizationId, Long id, OidcProviderRequest request) {
        OidcIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        apply(provider, request);
        provider = repository.save(provider);
        auditService.recordSuccess("OIDC_PROVIDER_UPDATED", null, null, null, "OidcIdentityProvider",
            String.valueOf(id), organizationId, "Updated OIDC provider " + provider.getName());
        return toDto(provider);
    }

    @Transactional
    public OidcProviderDto setEnabled(Long organizationId, Long id, boolean enabled) {
        OidcIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        if (enabled) {
            // C27: one enabled provider per organization across SAML and OIDC.
            repository.findByOrganizationIdAndEnabledTrue(organizationId)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    existing.setEnabled(false);
                    existing.setUpdatedAt(Instant.now());
                    repository.saveAndFlush(existing);
                });
            samlRepository.findByOrganizationIdAndEnabledTrue(organizationId).ifPresent(saml -> {
                saml.setEnabled(false);
                saml.setUpdatedAt(Instant.now());
                samlRepository.saveAndFlush(saml);
            });
        }
        provider.setEnabled(enabled);
        provider.setUpdatedAt(Instant.now());
        provider = repository.save(provider);
        auditService.recordSuccess(enabled ? "OIDC_PROVIDER_ENABLED" : "OIDC_PROVIDER_DISABLED", null, null, null,
            "OidcIdentityProvider", String.valueOf(id), organizationId, null);
        return toDto(provider);
    }

    @Transactional
    public void delete(Long organizationId, Long id) {
        OidcIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        repository.delete(provider);
        auditService.recordSuccess("OIDC_PROVIDER_DELETED", null, null, null, "OidcIdentityProvider",
            String.valueOf(id), organizationId, "Deleted OIDC provider " + provider.getName());
    }

    /** Reads discovery and checks the issuer and required endpoints. Never
     * throws for a bad configuration; every outcome is in the result. */
    @Transactional(readOnly = true)
    public OidcProviderTestResultDto test(Long organizationId, Long id) {
        OidcIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        List<String> checks = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        OidcProviderClient.Discovery discovery;
        try {
            discovery = client.discover(provider.getIssuerUrl());
            checks.add("Discovery document read from " + provider.getIssuerUrl());
        } catch (IllegalStateException e) {
            errors.add(e.getMessage());
            return new OidcProviderTestResultDto(false, checks, errors);
        }
        if (sameIssuer(discovery.issuer(), provider.getIssuerUrl())) {
            checks.add("Issuer matches: " + discovery.issuer());
        } else {
            errors.add("The discovery document's issuer (" + discovery.issuer() + ") does not match " + provider.getIssuerUrl());
        }
        requirePresent(discovery.authorizationEndpoint(), "authorization endpoint", checks, errors);
        requirePresent(discovery.tokenEndpoint(), "token endpoint", checks, errors);
        requirePresent(discovery.jwksUri(), "JWKS URI", checks, errors);
        return new OidcProviderTestResultDto(errors.isEmpty(), checks, errors);
    }

    public String redirectUri(Long organizationId) {
        return backendUrl + "/oidc/" + organizationId + "/callback";
    }

    String decryptSecret(OidcIdentityProvider provider) {
        return cipher.decrypt(provider.getEncryptedClientSecret());
    }

    static boolean sameIssuer(String a, String b) {
        return a != null && b != null && a.replaceAll("/+$", "").equals(b.replaceAll("/+$", ""));
    }

    private void apply(OidcIdentityProvider provider, OidcProviderRequest request) {
        String issuer = request.issuerUrl().trim();
        URI uri;
        try {
            uri = URI.create(issuer);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The issuer URL is not a valid URL.");
        }
        boolean local = "localhost".equalsIgnoreCase(uri.getHost()) || "127.0.0.1".equals(uri.getHost());
        if (uri.getHost() == null || !("https".equalsIgnoreCase(uri.getScheme()) || (local && "http".equalsIgnoreCase(uri.getScheme())))) {
            throw new IllegalArgumentException("The issuer URL must be an absolute https:// URL.");
        }
        String scopes = request.scopes() == null || request.scopes().isBlank() ? DEFAULT_SCOPES : request.scopes().trim().replaceAll("\\s+", " ");
        if (Arrays.stream(scopes.split(" ")).noneMatch("openid"::equals)) {
            throw new IllegalArgumentException("Scopes must include openid.");
        }
        provider.setName(request.name().trim());
        provider.setIssuerUrl(issuer);
        provider.setClientId(request.clientId().trim());
        if (request.clientSecret() != null && !request.clientSecret().isBlank()) {
            provider.setEncryptedClientSecret(cipher.encrypt(request.clientSecret()));
        }
        provider.setScopes(scopes);
        provider.setUpdatedAt(Instant.now());
    }

    private static void requirePresent(String value, String label, List<String> checks, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add("The discovery document has no " + label);
        } else {
            checks.add("Found the " + label);
        }
    }

    /** Same as SamlProviderService: 404 for an unknown id, 403 for another
     * organization's provider (never confirming it exists there). */
    OidcIdentityProvider findOwnedOrThrow(Long organizationId, Long id) {
        OidcIdentityProvider provider = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OIDC provider not found"));
        if (!provider.getOrganizationId().equals(organizationId)) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        return provider;
    }

    private OidcProviderDto toDto(OidcIdentityProvider p) {
        return new OidcProviderDto(p.getId(), p.getName(), p.getIssuerUrl(), p.getClientId(),
            p.getEncryptedClientSecret() != null, p.getScopes(), p.isEnabled(), redirectUri(p.getOrganizationId()),
            p.getCreatedAt(), p.getUpdatedAt(), ClaimMapping.orEmpty(p.getClaimMapping()).toDto());
    }

    /** REQ-IAM-007 (C28): replace the provider's claim mapping; blank = default. */
    @Transactional
    public OidcProviderDto updateClaimMapping(Long organizationId, Long id, ClaimMappingDto request) {
        OidcIdentityProvider provider = findOwnedOrThrow(organizationId, id);
        ClaimMapping mapping = ClaimMapping.orEmpty(provider.getClaimMapping());
        mapping.apply(request);
        provider.setClaimMapping(mapping);
        provider.setUpdatedAt(Instant.now());
        provider = repository.save(provider);
        auditService.recordSuccess("OIDC_CLAIM_MAPPING_UPDATED", null, null, null, "OidcIdentityProvider",
            String.valueOf(id), organizationId, null);
        return toDto(provider);
    }
}
