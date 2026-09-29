package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.modules.federation.model.ClaimMapping;
import com.vyoog.eisplatform.modules.federation.model.OidcExternalIdentity;
import com.vyoog.eisplatform.modules.federation.model.OidcIdentityProvider;
import com.vyoog.eisplatform.modules.federation.model.OidcLoginRequest;
import com.vyoog.eisplatform.modules.federation.repository.OidcExternalIdentityRepository;
import com.vyoog.eisplatform.modules.federation.repository.OidcIdentityProviderRepository;
import com.vyoog.eisplatform.modules.federation.repository.OidcLoginRequestRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REQ-IAM-006 (C27): OIDC sign-in, the same shape as SAML. The user enters
 * their organization code, the browser is sent to the provider (authorization
 * code flow with PKCE, state and nonce), and the callback validates the ID
 * token and creates the normal platform session through FederatedAccountService
 * (JIT MEMBER provisioning, MFA policy per C29).
 *
 * <p>Checked on every callback: state known, for this organization, unused and
 * unexpired; the ID token's signature (JWKS) and expiry; issuer equals the
 * discovered issuer; audience contains our client id; nonce matches; a subject
 * and an email are present.
 */
@Service
@RequiredArgsConstructor
public class OidcAuthenticationService {

    private static final Duration REQUEST_VALIDITY = Duration.ofMinutes(10);

    private final OidcIdentityProviderRepository providerRepository;
    private final OidcLoginRequestRepository loginRequestRepository;
    private final OidcExternalIdentityRepository externalIdentityRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerRepository customerRepository;
    private final OidcProviderService providerService;
    private final OidcProviderClient client;
    private final FederatedAccountService federatedAccountService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String buildRedirectUrl(Long organizationId) {
        OidcIdentityProvider provider = providerRepository.findByOrganizationIdAndEnabledTrue(organizationId)
            .orElseThrow(() -> new OidcLoginException("Single sign-on is not enabled for this organization"));
        OidcProviderClient.Discovery discovery = discover(provider);
        if (discovery.authorizationEndpoint() == null) {
            throw new OidcLoginException("Your identity provider's configuration has no authorization endpoint");
        }

        OidcLoginRequest request = new OidcLoginRequest();
        request.setState(randomToken(32));
        request.setOrganizationId(organizationId);
        request.setProviderId(provider.getId());
        request.setNonce(randomToken(32));
        request.setCodeVerifier(randomToken(48));
        request.setCreatedAt(Instant.now());
        request.setExpiresAt(Instant.now().plus(REQUEST_VALIDITY));
        loginRequestRepository.save(request);

        String separator = discovery.authorizationEndpoint().contains("?") ? "&" : "?";
        return discovery.authorizationEndpoint() + separator
            + "response_type=code"
            + "&client_id=" + enc(provider.getClientId())
            + "&redirect_uri=" + enc(providerService.redirectUri(organizationId))
            + "&scope=" + enc(provider.getScopes())
            + "&state=" + enc(request.getState())
            + "&nonce=" + enc(request.getNonce())
            + "&code_challenge=" + enc(codeChallenge(request.getCodeVerifier()))
            + "&code_challenge_method=S256";
    }

    @Transactional
    public String handleCallback(Long organizationId, String code, String state, String error, HttpServletResponse response) {
        if (state == null || state.isBlank()) {
            throw new OidcLoginException("This sign-in attempt was not recognized");
        }
        OidcLoginRequest request = loginRequestRepository.findById(state)
            .filter(r -> r.getOrganizationId().equals(organizationId))
            .orElseThrow(() -> new OidcLoginException("This sign-in attempt was not recognized"));
        if (request.getConsumedAt() != null) {
            throw new OidcLoginException("This sign-in response has already been used");
        }
        if (request.getExpiresAt().isBefore(Instant.now())) {
            throw new OidcLoginException("This sign-in attempt has expired; please try again");
        }
        // Consumed before anything else, so it can never be replayed.
        request.setConsumedAt(Instant.now());
        loginRequestRepository.save(request);

        if (error != null && !error.isBlank()) {
            throw new OidcLoginException("Your identity provider did not complete the sign-in (" + error + ")");
        }
        if (code == null || code.isBlank()) {
            throw new OidcLoginException("Your identity provider did not return an authorization code");
        }

        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new OidcLoginException("Unknown organization"));
        OidcIdentityProvider provider = providerRepository.findById(request.getProviderId())
            .filter(OidcIdentityProvider::isEnabled)
            .filter(p -> p.getOrganizationId().equals(organizationId))
            .orElseThrow(() -> new OidcLoginException("Single sign-on is not enabled for this organization"));
        OidcProviderClient.Discovery discovery = discover(provider);

        Map<String, Object> claims;
        try {
            String idToken = client.exchangeCode(discovery.tokenEndpoint(), provider.getClientId(),
                providerService.decryptSecret(provider), code, providerService.redirectUri(organizationId),
                request.getCodeVerifier());
            claims = client.verifyIdToken(discovery.jwksUri(), idToken);
        } catch (IllegalStateException e) {
            throw new OidcLoginException(e.getMessage());
        }

        String issuer = stringClaim(claims, "iss");
        if (!OidcProviderService.sameIssuer(issuer, discovery.issuer())) {
            throw new OidcLoginException("Your identity provider's response could not be verified (issuer)");
        }
        if (!audienceContains(claims.get("aud"), provider.getClientId())) {
            throw new OidcLoginException("Your identity provider's response could not be verified (audience)");
        }
        if (!request.getNonce().equals(stringClaim(claims, "nonce"))) {
            throw new OidcLoginException("Your identity provider's response could not be verified (nonce)");
        }
        String subject = stringClaim(claims, "sub");
        if (subject == null || subject.isBlank()) {
            throw new OidcLoginException("The ID token did not include a subject");
        }

        // REQ-IAM-007 (C28): a configured claim is tried first, then the OIDC
        // defaults (email, given_name, family_name, name); fallbacks stay fixed.
        ClaimMapping mapping = ClaimMapping.orEmpty(provider.getClaimMapping());
        String email = firstClaim(claims, ClaimMapping.tryFirst(mapping.getEmail(), List.of("email")));
        if (email == null || email.isBlank()) {
            throw new OidcLoginException("Could not determine an email address for this identity — "
                + "configure your identity provider to include the email claim");
        }
        String firstName = firstNonBlank(
            firstClaim(claims, ClaimMapping.tryFirst(mapping.getFirstName(), List.of("given_name"))),
            firstClaim(claims, ClaimMapping.tryFirst(mapping.getDisplayName(), List.of("name"))),
            localPart(email));
        String lastName = firstNonBlank(firstClaim(claims, ClaimMapping.tryFirst(mapping.getLastName(), List.of("family_name"))), "SSO User");

        Customer customer = resolveOrProvision(organization, discovery.issuer(), subject, email, firstName, lastName);
        return federatedAccountService.finishSignIn(customer, organizationId, email,
            "Signed in via OIDC identity provider \"" + provider.getName() + "\"", "OIDC_LOGIN_SUCCESS", response,
            OidcLoginException::new);
    }

    private Customer resolveOrProvision(Organization organization, String issuer, String subject, String email,
                                        String firstName, String lastName) {
        Optional<OidcExternalIdentity> existing =
            externalIdentityRepository.findByOrganizationIdAndIssuerAndSubject(organization.getId(), issuer, subject);
        if (existing.isPresent()) {
            OidcExternalIdentity identity = existing.get();
            Customer customer = customerRepository.findById(identity.getCustomerId())
                .orElseThrow(() -> new OidcLoginException("The account linked to this identity no longer exists"));
            identity.setEmail(email);
            identity.setLastLoginAt(Instant.now());
            externalIdentityRepository.save(identity);
            federatedAccountService.ensureActiveMembership(organization.getId(), customer.getId(), OidcLoginException::new);
            return customer;
        }
        Customer customer = federatedAccountService.linkOrProvision(organization, email, firstName, lastName, OidcLoginException::new);
        OidcExternalIdentity identity = new OidcExternalIdentity();
        identity.setOrganizationId(organization.getId());
        identity.setIssuer(issuer);
        identity.setSubject(subject);
        identity.setCustomerId(customer.getId());
        identity.setEmail(email);
        identity.setCreatedAt(Instant.now());
        identity.setLastLoginAt(Instant.now());
        externalIdentityRepository.save(identity);
        federatedAccountService.ensureActiveMembership(organization.getId(), customer.getId(), OidcLoginException::new);
        return customer;
    }

    private OidcProviderClient.Discovery discover(OidcIdentityProvider provider) {
        try {
            return client.discover(provider.getIssuerUrl());
        } catch (IllegalStateException e) {
            throw new OidcLoginException("Could not reach your identity provider: " + e.getMessage());
        }
    }

    private static boolean audienceContains(Object aud, String clientId) {
        if (aud instanceof String s) {
            return s.equals(clientId);
        }
        if (aud instanceof Collection<?> list) {
            return list.stream().anyMatch(v -> clientId.equals(String.valueOf(v)));
        }
        return false;
    }

    private static String stringClaim(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : String.valueOf(value);
    }

    private static String firstClaim(Map<String, Object> claims, List<String> names) {
        for (String name : names) {
            String value = stringClaim(claims, name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static String localPart(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private String randomToken(int bytes) {
        byte[] b = new byte[bytes];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    static String codeChallenge(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
