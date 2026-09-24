package com.vyoog.eisplatform.modules.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.common.exception.MfaChallengeRequiredException;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KeycloakPasswordGrantService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakPasswordGrantService.class);

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;
    private final String tokenUri;
    private final String endSessionUri;
    private final String clientId;
    private final String clientSecret;
    private final boolean debugLoginErrors;

    public KeycloakPasswordGrantService(
            ObjectMapper objectMapper,
            @Value("${vyoog.keycloak.token-uri}") String tokenUri,
            @Value("${vyoog.keycloak.end-session-uri}") String endSessionUri,
            @Value("${vyoog.keycloak.ropc-client-id}") String clientId,
            @Value("${vyoog.keycloak.ropc-client-secret:}") String clientSecret,
            @Value("${vyoog.auth.debug-login-errors:false}") boolean debugLoginErrors) {
        this.objectMapper = objectMapper;
        this.tokenUri = tokenUri;
        this.endSessionUri = endSessionUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.debugLoginErrors = debugLoginErrors;
    }

    public record TokenResult(String accessToken, String refreshToken, long expiresInSeconds) {
    }

    public TokenResult passwordGrant(String username, String password) {
        return passwordGrant(username, password, null);
    }

    /**
     * Phase 7: {@code totp} is included in the token request only when
     * present — Keycloak's Direct Grant OTP execution reads it from this
     * same form-encoded body, so submitting it (or not) is the entire
     * client-side mechanic for a two-step "password, then code" login, no
     * second grant type or endpoint needed.
     */
    public TokenResult passwordGrant(String username, String password, String totp) {
        Map<String, String> form = new HashMap<>(Map.of(
            "grant_type", "password",
            "username", username,
            "password", password
        ));
        if (totp != null && !totp.isBlank()) {
            form.put("totp", totp);
        }
        return requestToken(withClientAuth(form));
    }

    public TokenResult refresh(String refreshToken) {
        return requestToken(withClientAuth(Map.of(
            "grant_type", "refresh_token",
            "refresh_token", refreshToken
        )));
    }

    /**
     * Ends the real Keycloak session server-side (RP-initiated logout via direct
     * POST, no browser redirect needed) — this is what makes the *other* app's
     * own refresh token fail too, since both belong to the same underlying
     * Keycloak session. Best-effort: a failure here shouldn't block the caller
     * from clearing its own local state.
     */
    public void logout(String refreshToken) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endSessionUri))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encode(withClientAuth(Map.of("refresh_token", refreshToken)))))
            .build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            // Best-effort — local state is cleared regardless (see AuthController).
        }
    }

    private Map<String, String> withClientAuth(Map<String, String> form) {
        Map<String, String> withAuth = new HashMap<>(form);
        withAuth.put("client_id", clientId);
        // Public clients (no secret configured) authenticate with client_id alone.
        if (clientSecret != null && !clientSecret.isBlank()) {
            withAuth.put("client_secret", clientSecret);
        }
        return withAuth;
    }

    private TokenResult requestToken(Map<String, String> form) {
        String grantType = form.get("grant_type");
        log.info("Dispatching {} grant to Keycloak token endpoint {} for client {}", grantType, tokenUri, clientId);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUri))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encode(form)))
            .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("Could not reach Keycloak token endpoint {}: {}", tokenUri, e.getMessage());
            throw new IllegalStateException("Unable to reach Keycloak", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to reach Keycloak", e);
        }

        if (response.statusCode() != 200) {
            // Keep the client-facing response generic by default (this app's
            // anti-enumeration posture, same as PasswordResetService), but
            // retain Keycloak's structured reason in server logs always, and
            // optionally in the response itself when debug-login-errors is
            // on (see application.yml — local debugging only).
            log.warn("Keycloak rejected the {} grant for client {} — HTTP {}: {}",
                grantType, clientId, response.statusCode(), response.body());
            // Phase 7: correct credentials, but this account also needs its
            // OTP code — see MfaChallengeDetector's own javadoc for exactly
            // which real Keycloak error shape this distinguishes from a
            // genuinely wrong password.
            if (MfaChallengeDetector.isOtpChallenge(response.body())) {
                throw new MfaChallengeRequiredException("This account requires a verification code.");
            }
            String message = debugLoginErrors
                ? "Invalid email or password (debug: Keycloak said HTTP " + response.statusCode() + " — " + response.body() + ")"
                : "Invalid email or password";
            throw new InvalidCredentialsException(message);
        }

        log.info("Keycloak accepted the {} grant for client {}", grantType, clientId);

        try {
            JsonNode body = objectMapper.readTree(response.body());
            return new TokenResult(
                body.get("access_token").asText(),
                body.get("refresh_token").asText(),
                body.get("expires_in").asLong()
            );
        } catch (IOException e) {
            throw new IllegalStateException("Malformed response from Keycloak", e);
        }
    }

    private static String encode(Map<String, String> form) {
        return form.entrySet().stream()
            .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
    }
}
