package com.vyoog.eisplatform.modules.federation.service;

import com.onelogin.saml2.authn.AuthnRequest;
import com.onelogin.saml2.authn.SamlResponse;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Util;
import com.vyoog.eisplatform.modules.auth.service.SessionCookieService;
import com.vyoog.eisplatform.modules.federation.dto.SsoCheckResponseDto;
import com.vyoog.eisplatform.modules.federation.model.SamlExternalIdentity;
import com.vyoog.eisplatform.modules.federation.model.SamlIdentityProvider;
import com.vyoog.eisplatform.modules.federation.model.SamlLoginRequest;
import com.vyoog.eisplatform.modules.federation.repository.OidcIdentityProviderRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlExternalIdentityRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlIdentityProviderRepository;
import com.vyoog.eisplatform.modules.federation.repository.SamlLoginRequestRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Phase 5 (2026.3.3): the actual SAML login flow — this app is always the
 * Service Provider (see {@link SamlIdentityProvider}'s own javadoc);
 * everything about parsing an organization's IdP configuration and this
 * app's own SP metadata stays in {@link SamlProviderService} (Phase 4), this
 * class only ever builds an outgoing AuthnRequest and validates an incoming
 * Response.
 *
 * <p><b>Organization isolation</b> (this platform's own explicit requirement
 * — an identity belonging to Organization A must never gain Organization B
 * access): the {@code organizationId} in every URL here is routing only,
 * never trusted on its own. Three independent checks each separately
 * enforce isolation: (1) {@link SamlLoginRequest} rows are looked up by the
 * incoming response's own {@code InResponseTo} AND filtered by the
 * organization id in the URL — a response whose original request was issued
 * for a different organization is rejected before any signature check even
 * runs; (2) the SAML library's own Audience/Destination validation compares
 * against THIS organization's own SP entity id/ACS URL, both of which embed
 * the organization id, so an assertion actually minted for another
 * organization's AuthnRequest fails that check too; (3) signature
 * verification uses THIS organization's own stored IdP certificate.
 *
 * <p><b>Stable identity + no privilege escalation</b>: see
 * {@link SamlExternalIdentity} for the stable-key design, and
 * {@link #ensureActiveMembership} for why a SAML-authenticated user is
 * always added as a plain {@link OrgRole#MEMBER} — no SAML attribute or
 * group claim is ever read to decide organization-admin access.
 *
 * <p><b>Known limitation, disclosed rather than worked around</b>:
 * java-saml-core 2.9.0 exposes no configurable clock-skew tolerance for
 * assertion timestamp validation — {@link SamlResponse#isValid} applies the
 * library's own strict, spec-correct comparison with zero artificial
 * tolerance. A real IdP with a noticeably drifted clock will fail
 * validation; this is treated as a correct rejection, not a bug to patch
 * around with hand-rolled timestamp logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamlAuthenticationService {

    private static final Duration REQUEST_VALIDITY = Duration.ofMinutes(10);

    private static final List<String> EMAIL_ATTRIBUTES = List.of(
        "email", "emailaddress", "mail",
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
        "urn:oid:0.9.2342.19200300.100.1.3");
    private static final List<String> FIRST_NAME_ATTRIBUTES = List.of(
        "firstname", "givenname",
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
        "urn:oid:2.5.4.42");
    private static final List<String> LAST_NAME_ATTRIBUTES = List.of(
        "lastname", "surname", "sn",
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
        "urn:oid:2.5.4.4");
    private static final List<String> DISPLAY_NAME_ATTRIBUTES = List.of(
        "displayname", "name", "cn");

    private final SamlIdentityProviderRepository identityProviderRepository;
    private final OidcIdentityProviderRepository oidcProviderRepository;
    private final SamlExternalIdentityRepository externalIdentityRepository;
    private final SamlLoginRequestRepository loginRequestRepository;
    private final SamlProviderService samlProviderService;
    private final OrganizationRepository organizationRepository;
    private final CustomerRepository customerRepository;
    private final FederatedAccountService federatedAccountService;



    /** Public, pre-login lookup the SPA calls (via {@code fetch}) before
     * navigating anywhere — see {@link SsoCheckResponseDto}'s own javadoc.
     * Organization codes are already a public business identifier elsewhere
     * in this app (registration/join flows), so confirming one exists here
     * is not a new enumeration surface. */
    @Transactional(readOnly = true)
    public SsoCheckResponseDto checkSso(String organizationCode) {
        if (organizationCode == null || organizationCode.isBlank()) {
            return new SsoCheckResponseDto(false, null, null, null);
        }
        // REQ-IAM-006 (C27): at most one SAML or OIDC provider is enabled, so
        // the protocol tells the web app which sign-in to start.
        return organizationRepository.findByCodeIgnoreCase(organizationCode)
            .map(org -> {
                if (identityProviderRepository.findByOrganizationIdAndEnabledTrue(org.getId()).isPresent()) {
                    return new SsoCheckResponseDto(true, org.getId(), org.getName(), "SAML");
                }
                if (oidcProviderRepository.findByOrganizationIdAndEnabledTrue(org.getId()).isPresent()) {
                    return new SsoCheckResponseDto(true, org.getId(), org.getName(), "OIDC");
                }
                return new SsoCheckResponseDto(false, null, null, null);
            })
            .orElseGet(() -> new SsoCheckResponseDto(false, null, null, null));
    }

    /**
     * Builds a real SP-initiated AuthnRequest (via the OneLogin library's own
     * {@link AuthnRequest}, never hand-built XML) and records a
     * {@link SamlLoginRequest} row for it, then returns the full HTTP-Redirect
     * binding URL to send the browser to. Called from a real top-level GET
     * navigation, never {@code fetch} — see SamlLoginController.
     */
    @Transactional
    public String buildRedirectUrl(Long organizationId) {
        SamlIdentityProvider provider = identityProviderRepository.findByOrganizationIdAndEnabledTrue(organizationId)
            .orElseThrow(() -> new SamlLoginException("Single sign-on is not enabled for this organization"));

        Saml2Settings settings = buildSettings(organizationId, provider);
        AuthnRequest authnRequest = new AuthnRequest(settings);

        SamlLoginRequest loginRequest = new SamlLoginRequest();
        loginRequest.setId(authnRequest.getId());
        loginRequest.setOrganizationId(organizationId);
        loginRequest.setCreatedAt(Instant.now());
        loginRequest.setExpiresAt(Instant.now().plus(REQUEST_VALIDITY));
        loginRequestRepository.save(loginRequest);

        try {
            String encoded = authnRequest.getEncodedAuthnRequest();
            String separator = provider.getSsoUrl().contains("?") ? "&" : "?";
            return provider.getSsoUrl() + separator + "SAMLRequest="
                + URLEncoder.encode(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SamlLoginException("Could not build the SAML login request: " + e.getMessage());
        }
    }

    /**
     * Validates an incoming SAML Response (see this class's own javadoc for
     * the organization-isolation reasoning) and, once valid, resolves or
     * JIT-provisions the corresponding Vyoog account and finalizes a real
     * browser session exactly the same way {@code AuthController} does for a
     * normal login (same cookies, via {@link SessionCookieService}). Returns
     * the frontend URL the caller should redirect the browser to; throws
     * {@link SamlLoginException} on any rejection.
     */
    @Transactional
    public String handleAcs(Long organizationId, String samlResponseParam, HttpServletResponse response) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new SamlLoginException("Unknown organization"));
        SamlIdentityProvider provider = identityProviderRepository.findByOrganizationIdAndEnabledTrue(organizationId)
            .orElseThrow(() -> new SamlLoginException("Single sign-on is not enabled for this organization"));

        if (samlResponseParam == null || samlResponseParam.isBlank()) {
            throw new SamlLoginException("Missing SAML response");
        }

        String decodedXml;
        try {
            decodedXml = new String(Util.base64decoder(samlResponseParam), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SamlLoginException("Malformed SAML response");
        }
        // Belt-and-braces hardening BEFORE this untrusted, third-party XML is
        // parsed at all — same convention, same reasoning, as
        // SamlProviderService#applyFields (Phase 4).
        try {
            SamlXmlSecurity.rejectUnsafeXml(decodedXml);
        } catch (IllegalArgumentException e) {
            throw new SamlLoginException("Malformed or unsafe SAML response: " + e.getMessage());
        }

        String inResponseTo;
        try {
            Document document = Util.convertStringToDocument(decodedXml);
            inResponseTo = document.getDocumentElement().getAttribute("InResponseTo");
        } catch (Exception e) {
            throw new SamlLoginException("Could not parse the SAML response");
        }
        if (inResponseTo == null || inResponseTo.isBlank()) {
            throw new SamlLoginException("Unsolicited SAML responses are not accepted");
        }

        SamlLoginRequest loginRequest = loginRequestRepository.findById(inResponseTo)
            .filter(r -> r.getOrganizationId().equals(organizationId))
            .orElseThrow(() -> new SamlLoginException("This sign-in attempt was not recognized"));
        if (loginRequest.getConsumedAt() != null) {
            throw new SamlLoginException("This sign-in response has already been used");
        }
        if (loginRequest.getExpiresAt().isBefore(Instant.now())) {
            throw new SamlLoginException("This sign-in attempt has expired; please try again");
        }

        Saml2Settings settings = buildSettings(organizationId, provider);
        SamlResponse samlResponse;
        try {
            samlResponse = new SamlResponse(settings, samlProviderService.acsUrl(organizationId), samlResponseParam);
        } catch (Exception e) {
            throw new SamlLoginException("Could not process the SAML response: " + e.getMessage());
        }

        if (!samlResponse.isValid(inResponseTo)) {
            Exception cause = samlResponse.getValidationException();
            log.warn("SAML assertion rejected for organization {} (provider '{}'): {}",
                organizationId, provider.getName(), cause != null ? cause.getMessage() : "unknown reason");
            throw new SamlLoginException("Your identity provider's response could not be verified");
        }

        // Replay defense: consumed exactly once, inside this same
        // transaction, before any provisioning/session work below — see
        // SamlLoginRequest's own javadoc.
        loginRequest.setConsumedAt(Instant.now());
        loginRequestRepository.save(loginRequest);

        String nameId;
        Map<String, List<String>> attributes;
        try {
            nameId = samlResponse.getNameId();
            attributes = samlResponse.getAttributes();
        } catch (Exception e) {
            throw new SamlLoginException("Could not read the SAML assertion");
        }
        if (nameId == null || nameId.isBlank()) {
            throw new SamlLoginException("The SAML assertion did not include a NameID");
        }

        String email = firstAttribute(attributes, EMAIL_ATTRIBUTES);
        if ((email == null || email.isBlank()) && nameId.contains("@")) {
            email = nameId;
        }
        if (email == null || email.isBlank()) {
            throw new SamlLoginException(
                "Could not determine an email address for this identity — "
                    + "configure your identity provider to send an email attribute, "
                    + "or use the emailAddress NameID format");
        }

        String firstName = firstAttribute(attributes, FIRST_NAME_ATTRIBUTES);
        if (firstName == null || firstName.isBlank()) {
            firstName = firstAttribute(attributes, DISPLAY_NAME_ATTRIBUTES);
        }
        if (firstName == null || firstName.isBlank()) {
            int at = email.indexOf('@');
            firstName = at > 0 ? email.substring(0, at) : email;
        }
        String lastName = firstAttribute(attributes, LAST_NAME_ATTRIBUTES);
        if (lastName == null || lastName.isBlank()) {
            lastName = "SSO User";
        }

        Customer customer = resolveOrProvisionCustomer(organization, provider, nameId, email, firstName, lastName);

        return federatedAccountService.finishSignIn(customer, organizationId, email,
            "Signed in via SAML identity provider \"" + provider.getName() + "\"", "SAML_LOGIN_SUCCESS", response,
            SamlLoginException::new);
    }

    /**
     * Resolves the {@link SamlExternalIdentity#findByOrganizationIdAndIdpEntityIdAndNameId
     * stable identity} to a Vyoog {@link Customer}, JIT-provisioning both a
     * real (never password-known) Keycloak user and a Customer row on first
     * login when no existing account matches by email, or linking to an
     * existing Customer (by email) otherwise — never creating a duplicate.
     */
    private Customer resolveOrProvisionCustomer(Organization organization, SamlIdentityProvider provider,
                                                 String nameId, String email, String firstName, String lastName) {
        Optional<SamlExternalIdentity> existingIdentity = externalIdentityRepository
            .findByOrganizationIdAndIdpEntityIdAndNameId(organization.getId(), provider.getEntityId(), nameId);

        if (existingIdentity.isPresent()) {
            SamlExternalIdentity identity = existingIdentity.get();
            Customer customer = customerRepository.findById(identity.getCustomerId())
                .orElseThrow(() -> new SamlLoginException("The account linked to this identity no longer exists"));
            identity.setEmail(email);
            identity.setLastLoginAt(Instant.now());
            externalIdentityRepository.save(identity);
            federatedAccountService.ensureActiveMembership(organization.getId(), customer.getId(), SamlLoginException::new);
            return customer;
        }

        Customer customer = federatedAccountService.linkOrProvision(organization, email, firstName, lastName, SamlLoginException::new);

        SamlExternalIdentity identity = new SamlExternalIdentity();
        identity.setOrganizationId(organization.getId());
        identity.setIdpEntityId(provider.getEntityId());
        identity.setNameId(nameId);
        identity.setCustomerId(customer.getId());
        identity.setEmail(email);
        identity.setCreatedAt(Instant.now());
        identity.setLastLoginAt(Instant.now());
        externalIdentityRepository.save(identity);

        federatedAccountService.ensureActiveMembership(organization.getId(), customer.getId(), SamlLoginException::new);
        return customer;
    }

    private Saml2Settings buildSettings(Long organizationId, SamlIdentityProvider provider) {
        Map<String, Object> values = new HashMap<>();
        values.put(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, samlProviderService.spEntityId(organizationId));
        values.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY, samlProviderService.acsUrl(organizationId));
        values.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_BINDING_PROPERTY_KEY, "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        values.put(SettingsBuilder.SP_NAMEIDFORMAT_PROPERTY_KEY, "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        values.put(SettingsBuilder.IDP_ENTITYID_PROPERTY_KEY, provider.getEntityId());
        values.put(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_URL_PROPERTY_KEY, provider.getSsoUrl());
        values.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, provider.getCertificatePem());
        values.put(SettingsBuilder.STRICT_PROPERTY_KEY, true);
        // See this class's own javadoc: WANT_ASSERTIONS_SIGNED requires a
        // signature to be present (not merely valid-if-present), and
        // REJECT_DEPRECATED_ALGORITHM refuses weak/old signature algorithms
        // (e.g. SHA-1).
        values.put(SettingsBuilder.SECURITY_WANT_ASSERTIONS_SIGNED, true);
        values.put(SettingsBuilder.SECURITY_WANT_XML_VALIDATION, true);
        values.put(SettingsBuilder.SECURITY_REJECT_DEPRECATED_ALGORITHM, true);
        return new SettingsBuilder().fromValues(values).build();
    }

    private static String firstAttribute(Map<String, List<String>> attributes, List<String> candidateNames) {
        if (attributes == null) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            for (String candidate : candidateNames) {
                if (entry.getKey().equalsIgnoreCase(candidate)) {
                    String value = entry.getValue().get(0);
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        }
        return null;
    }
}
