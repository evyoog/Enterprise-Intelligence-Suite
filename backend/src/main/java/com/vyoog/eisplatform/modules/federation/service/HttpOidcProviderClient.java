package com.vyoog.eisplatform.modules.federation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/** Real {@link OidcProviderClient} over java.net.http, with Nimbus (via Spring
 * Security) for JWKS signature verification. */
@Component
public class HttpOidcProviderClient implements OidcProviderClient {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Discovery discover(String issuerUrl) {
        String url = issuerUrl.replaceAll("/+$", "") + "/.well-known/openid-configuration";
        try {
            HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Discovery document returned HTTP " + response.statusCode());
            }
            JsonNode doc = objectMapper.readTree(response.body());
            return new Discovery(text(doc, "issuer"), text(doc, "authorization_endpoint"),
                text(doc, "token_endpoint"), text(doc, "jwks_uri"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the discovery document: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading the discovery document");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("The issuer URL is not valid");
        }
    }

    @Override
    public String exchangeCode(String tokenEndpoint, String clientId, String clientSecret, String code,
                               String redirectUri, String codeVerifier) {
        String form = "grant_type=authorization_code"
            + "&code=" + enc(code)
            + "&redirect_uri=" + enc(redirectUri)
            + "&code_verifier=" + enc(codeVerifier)
            + "&client_id=" + enc(clientId);
        String basic = Base64.getEncoder().encodeToString(
            (enc(clientId) + ":" + enc(clientSecret)).getBytes(StandardCharsets.UTF_8));
        try {
            HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder(URI.create(tokenEndpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + basic)
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build(),
                HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());
            if (response.statusCode() != 200 || !body.hasNonNull("id_token")) {
                String reason = body.hasNonNull("error") ? body.get("error").asText() : "HTTP " + response.statusCode();
                throw new IllegalStateException("The identity provider refused the sign-in (" + reason + ")");
            }
            return body.get("id_token").asText();
        } catch (IOException e) {
            throw new IllegalStateException("Could not reach the identity provider's token endpoint");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while contacting the identity provider");
        }
    }

    @Override
    public Map<String, Object> verifyIdToken(String jwksUri, String idToken) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
        OAuth2TokenValidator<Jwt> validator = new JwtTimestampValidator();
        decoder.setJwtValidator(validator);
        try {
            return decoder.decode(idToken).getClaims();
        } catch (JwtException e) {
            throw new IllegalStateException("The ID token could not be verified");
        }
    }

    private static String text(JsonNode doc, String field) {
        return doc.hasNonNull(field) ? doc.get(field).asText() : null;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
