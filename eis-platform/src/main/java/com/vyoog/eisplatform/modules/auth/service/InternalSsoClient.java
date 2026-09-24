package com.vyoog.eisplatform.modules.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * With three-plus apps in the SSO mesh (this one, vyg-pms, vyg-ticket), a
 * bridge session could have originated from any OTHER app, and this one has
 * no way to tell which just from the opaque id — so redeemToken() tries every
 * configured partner in turn and returns the first one that actually has that
 * session (a 404 from a partner that doesn't hold it is the normal, expected
 * case, not an error). notifyLogout() broadcasts to ALL partners, since any
 * of them might be holding their own cached copy of the session.
 */
@Slf4j
@Service
public class InternalSsoClient {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;
    private final List<String> partnerBackendUrls;
    private final String sharedSecret;
    private final String ownClientId;

    public InternalSsoClient(
            ObjectMapper objectMapper,
            @Value("${vyoog.internal.partner-backend-urls}") String partnerBackendUrlsCsv,
            @Value("${vyoog.internal.sso-shared-secret}") String sharedSecret,
            @Value("${vyoog.keycloak.ropc-client-id}") String ownClientId) {
        this.objectMapper = objectMapper;
        this.partnerBackendUrls = Arrays.stream(partnerBackendUrlsCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
        this.sharedSecret = sharedSecret;
        this.ownClientId = ownClientId;
    }

    /** Empty means: none of the configured partners have this bridge session
     * (expired, wrong id, a partner unreachable, or Keycloak refused the
     * impersonation exchange) — callers should treat this exactly like "not
     * authenticated," never as an error. */
    public Optional<KeycloakPasswordGrantService.TokenResult> redeemToken(String ssoSessionId) {
        for (String partnerUrl : partnerBackendUrls) {
            Optional<KeycloakPasswordGrantService.TokenResult> result = redeemFrom(partnerUrl, ssoSessionId);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private Optional<KeycloakPasswordGrantService.TokenResult> redeemFrom(String partnerUrl, String ssoSessionId) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                "ssoSessionId", ssoSessionId,
                "targetClientId", ownClientId));
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(partnerUrl + "/internal/sso/token"))
                .header("Content-Type", "application/json")
                .header("X-Internal-Sso-Secret", sharedSecret)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            JsonNode json = objectMapper.readTree(response.body());
            return Optional.of(new KeycloakPasswordGrantService.TokenResult(
                json.get("accessToken").asText(),
                json.get("refreshToken").asText(),
                json.get("expiresInSeconds").asLong()
            ));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Internal SSO redemption call to {} failed: {}", partnerUrl, e.toString());
            return Optional.empty();
        }
    }

    /** Tells every OTHER app's backend to tear down its own copy of this
     * bridge session too (see InternalSsoController's own /internal/sso/logout
     * for why this explicit broadcast is required — impersonation-exchanged
     * sessions don't share a Keycloak session with the originating login, so
     * ending one side's session never implicitly ends the others'). One
     * partner being unreachable never stops the rest from being notified.
     * Best-effort, fire-and-forget: never throws. */
    public void notifyLogout(String ssoSessionId) {
        for (String partnerUrl : partnerBackendUrls) {
            notifyOne(partnerUrl, ssoSessionId);
        }
    }

    private void notifyOne(String partnerUrl, String ssoSessionId) {
        try {
            String body = objectMapper.writeValueAsString(Map.of("ssoSessionId", ssoSessionId));
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(partnerUrl + "/internal/sso/logout"))
                .header("Content-Type", "application/json")
                .header("X-Internal-Sso-Secret", sharedSecret)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Internal SSO logout notification to {} failed: {}", partnerUrl, e.toString());
        }
    }
}
