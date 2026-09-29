package com.vyoog.eisplatform.modules.federation.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * In-memory identity provider for OIDC tests: a real RSA key pair signs real
 * RS256 ID tokens, and {@link #verifyIdToken} verifies them with the same
 * Nimbus decoder the real client uses, so signature and expiry checks are
 * genuinely exercised. Tests set {@link #nextClaims} to shape the next token.
 */
public class FakeOidcProviderClient implements OidcProviderClient {

    public static final String ISSUER = "https://idp.oidc-test.example";
    private final KeyPair keyPair;
    private final KeyPair otherKeyPair;
    public Discovery discovery = new Discovery(ISSUER, ISSUER + "/authorize", ISSUER + "/token", ISSUER + "/jwks");
    public boolean discoveryFails = false;
    public boolean signWithOtherKey = false;
    public final Map<String, Object> nextClaims = new HashMap<>();
    public final List<String> exchangedCodes = new ArrayList<>();
    public String lastClientSecret;
    public String lastCodeVerifier;

    public FakeOidcProviderClient() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();
            otherKeyPair = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public void reset() {
        discovery = new Discovery(ISSUER, ISSUER + "/authorize", ISSUER + "/token", ISSUER + "/jwks");
        discoveryFails = false;
        signWithOtherKey = false;
        nextClaims.clear();
        exchangedCodes.clear();
        lastClientSecret = null;
        lastCodeVerifier = null;
    }

    /** Standard claims for a valid token; tests override single entries. */
    public void validClaims(String clientId, String nonce, String subject, String email, Consumer<Map<String, Object>> tweak) {
        nextClaims.clear();
        nextClaims.put("iss", ISSUER);
        nextClaims.put("aud", clientId);
        nextClaims.put("nonce", nonce);
        nextClaims.put("sub", subject);
        nextClaims.put("email", email);
        nextClaims.put("given_name", "Ada");
        nextClaims.put("family_name", "Lovelace");
        nextClaims.put("exp", Instant.now().plusSeconds(300));
        if (tweak != null) {
            tweak.accept(nextClaims);
        }
    }

    @Override
    public Discovery discover(String issuerUrl) {
        if (discoveryFails) {
            throw new IllegalStateException("Discovery document returned HTTP 404");
        }
        return discovery;
    }

    @Override
    public String exchangeCode(String tokenEndpoint, String clientId, String clientSecret, String code,
                               String redirectUri, String codeVerifier) {
        exchangedCodes.add(code);
        lastClientSecret = clientSecret;
        lastCodeVerifier = codeVerifier;
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder();
        nextClaims.forEach((k, v) -> {
            if (v instanceof Instant instant) {
                claims.claim(k, Date.from(instant));
            } else {
                claims.claim(k, v);
            }
        });
        claims.issueTime(new Date());
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims.build());
        try {
            jwt.sign(new RSASSASigner((signWithOtherKey ? otherKeyPair : keyPair).getPrivate()));
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
        return jwt.serialize();
    }

    @Override
    public Map<String, Object> verifyIdToken(String jwksUri, String idToken) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
        decoder.setJwtValidator(new JwtTimestampValidator());
        try {
            return decoder.decode(idToken).getClaims();
        } catch (JwtException e) {
            throw new IllegalStateException("The ID token could not be verified");
        }
    }

    RSAPrivateKey privateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }
}
