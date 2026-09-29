package com.vyoog.eisplatform.modules.federation.service;

import java.util.Map;

/**
 * REQ-IAM-006: the network edge of OIDC sign-in, kept behind an interface so
 * tests never call a real identity provider (same convention as
 * KeycloakAdminClient). Issuer, audience and nonce are checked by
 * OidcAuthenticationService, not here.
 */
public interface OidcProviderClient {

    record Discovery(String issuer, String authorizationEndpoint, String tokenEndpoint, String jwksUri) {
    }

    /** Reads {issuer}/.well-known/openid-configuration. Throws IllegalStateException on failure. */
    Discovery discover(String issuerUrl);

    /** Authorization-code exchange (client_secret_basic, PKCE). Returns the raw ID token. */
    String exchangeCode(String tokenEndpoint, String clientId, String clientSecret, String code,
                        String redirectUri, String codeVerifier);

    /** Verifies the ID token's signature against the provider's JWKS and its
     * expiry, and returns its claims. Throws IllegalStateException when invalid. */
    Map<String, Object> verifyIdToken(String jwksUri, String idToken);
}
