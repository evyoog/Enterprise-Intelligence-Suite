package com.vyoog.eisplatform.modules.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Phase 8: the one thing password reset genuinely cannot do without —
 * actually changing a Keycloak user's credential — requires Keycloak's
 * Admin REST API, since ROPC only ever proves a password someone ALREADY
 * knows, and this app's registration flow deliberately never collects or
 * stores one (see PasswordPolicy's own javadoc: "Keycloak is the sole
 * credential store").
 *
 * <p>Before writing a single line of this class, the actual capability was
 * verified LIVE against the real Keycloak server — not assumed: a
 * {@code client_credentials} grant using the exact credentials already
 * configured for {@link ImpersonationExchangeService}
 * ({@code vyoog.internal.impersonation-client-id}/{@code -secret}, the
 * shared {@code eVyoog} confidential client) was decoded and inspected, and
 * separately used to call {@code GET /admin/realms/eVyoog/users?max=1},
 * which returned real user data with HTTP 200 — confirming this client's
 * service account already holds {@code manage-users} (among other
 * realm-management roles) on this realm today, with zero new Keycloak-side
 * configuration required. This is the deciding difference from Phase 7's
 * MFA enrollment and the earlier SAML discussion, where no such
 * already-configured, already-verified admin credential existed to build
 * against — those were correctly left unbuilt rather than shipped blind;
 * this one was actually tested before being written.
 */
@Slf4j
@Service
public class HttpKeycloakAdminClient implements KeycloakAdminClient {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;
    private final String tokenUri;
    private final String adminBaseUri;
    private final String clientId;
    private final String clientSecret;

    public HttpKeycloakAdminClient(
            ObjectMapper objectMapper,
            @Value("${vyoog.keycloak.token-uri}") String tokenUri,
            @Value("${vyoog.keycloak.admin-base-uri}") String adminBaseUri,
            @Value("${vyoog.internal.impersonation-client-id}") String clientId,
            @Value("${vyoog.internal.impersonation-client-secret}") String clientSecret) {
        this.objectMapper = objectMapper;
        this.tokenUri = tokenUri;
        this.adminBaseUri = adminBaseUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /** Case-insensitive exact match, since Keycloak's own {@code exact=true}
     * query flag is not guaranteed case-insensitive across versions — this
     * class re-checks the returned email itself rather than trusting that. */
    @Override
    public Optional<String> findUserIdByEmail(String email) {
        String token = adminToken();
        if (token == null) {
            return Optional.empty();
        }
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adminBaseUri + "/users?exact=true&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Keycloak admin user lookup failed (HTTP {}): {}", response.statusCode(), response.body());
                return Optional.empty();
            }
            JsonNode users = objectMapper.readTree(response.body());
            for (JsonNode user : users) {
                JsonNode userEmail = user.get("email");
                if (userEmail != null && userEmail.asText().equalsIgnoreCase(email)) {
                    return Optional.ofNullable(user.get("id")).map(JsonNode::asText);
                }
            }
            return Optional.empty();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak admin API for user lookup: {}", e.toString());
            return Optional.empty();
        }
    }

    /** Sets a real, permanent (non-temporary — never forces a Keycloak-hosted
     * "update password" page on next login) credential directly. Returns
     * false rather than throwing on failure — callers decide how to surface
     * that (this app's own reset-token stays unused either way, so the user
     * can just retry the same link). */
    @Override
    public boolean resetPassword(String keycloakUserId, String newPassword) {
        String token = adminToken();
        if (token == null) {
            return false;
        }
        String body = "{\"type\":\"password\",\"value\":" + jsonString(newPassword) + ",\"temporary\":false}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adminBaseUri + "/users/" + keycloakUserId + "/reset-password"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204) {
                log.warn("Keycloak admin password reset failed (HTTP {}): {}", response.statusCode(), response.body());
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak admin API for password reset: {}", e.toString());
            return false;
        }
    }

    /** "Re-authentication/logout after reset" — ends every existing session
     * for this user, so a reset password also invalidates any session an
     * attacker (or the user, on another device) already held. Best-effort:
     * the credential change above is what actually matters; a failure here
     * is logged, never thrown, matching {@code ImpersonationExchangeService.logout()}'s
     * own best-effort convention. */
    @Override
    public void logoutAllSessions(String keycloakUserId) {
        String token = adminToken();
        if (token == null) {
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adminBaseUri + "/users/" + keycloakUserId + "/logout"))
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 300) {
                log.warn("Keycloak admin logout-all-sessions returned HTTP {} for user {}", response.statusCode(), keycloakUserId);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak admin API to log out user {} sessions: {}", keycloakUserId, e.toString());
        }
    }

    /** {@code enabled: false} at creation is the normal case for a
     * self-registering organization admin awaiting email verification — see
     * RegistrationService, which flips it via {@link #setEnabled} once
     * verification actually completes. */
    @Override
    public Optional<String> createUser(String email, String firstName, String lastName, String password, boolean enabled) {
        String token = adminToken();
        if (token == null) {
            return Optional.empty();
        }
        String body = "{"
            + "\"username\":" + jsonString(email) + ","
            + "\"email\":" + jsonString(email) + ","
            + "\"firstName\":" + jsonString(firstName) + ","
            + "\"lastName\":" + jsonString(lastName) + ","
            + "\"enabled\":" + enabled + ","
            + "\"emailVerified\":true,"
            + "\"credentials\":[{\"type\":\"password\",\"value\":" + jsonString(password) + ",\"temporary\":false}]"
            + "}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adminBaseUri + "/users"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                log.warn("Keycloak admin user creation failed (HTTP {}): {}", response.statusCode(), response.body());
                return Optional.empty();
            }
            // Keycloak returns no body on success — the new user's id is only
            // in the Location header (".../admin/realms/eVyoog/users/<id>").
            return response.headers().firstValue("Location")
                .map(location -> location.substring(location.lastIndexOf('/') + 1));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak admin API for user creation: {}", e.toString());
            return Optional.empty();
        }
    }

    /** Keycloak's user-update endpoint merges the given fields into the
     * existing representation rather than replacing it wholesale, so a
     * single-field body here only ever touches {@code enabled}. */
    @Override
    public boolean setEnabled(String keycloakUserId, boolean enabled) {
        String token = adminToken();
        if (token == null) {
            return false;
        }
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adminBaseUri + "/users/" + keycloakUserId))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString("{\"enabled\":" + enabled + "}"))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204) {
                log.warn("Keycloak admin set-enabled failed (HTTP {}): {}", response.statusCode(), response.body());
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak admin API to enable user {}: {}", keycloakUserId, e.toString());
            return false;
        }
    }

    /**
     * Phase 3 (2026.3.3): verified live against the real Keycloak server
     * before this was written — {@code GET /admin/realms/eVyoog/users/{id}/sessions}
     * with a real logged-in test user returned real session objects
     * ({@code id}, {@code ipAddress}, {@code start}, {@code lastAccess},
     * {@code clients}) with HTTP 200, and {@code DELETE .../sessions/{id}}
     * on one of them returned 204 and left the other session untouched —
     * confirming this is a real per-session (not per-user) revocation.
     */
    @Override
    public List<KeycloakSessionInfo> listSessions(String keycloakUserId) {
        String token = adminToken();
        if (token == null) {
            return List.of();
        }
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adminBaseUri + "/users/" + keycloakUserId + "/sessions"))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Keycloak admin list-sessions failed (HTTP {}): {}", response.statusCode(), response.body());
                return List.of();
            }
            JsonNode sessions = objectMapper.readTree(response.body());
            List<KeycloakSessionInfo> result = new ArrayList<>();
            for (JsonNode node : sessions) {
                List<String> clients = new ArrayList<>();
                node.path("clients").fieldNames().forEachRemaining(clientId -> clients.add(node.path("clients").path(clientId).asText()));
                result.add(new KeycloakSessionInfo(
                    node.path("id").asText(),
                    node.path("ipAddress").asText(null),
                    node.path("start").asLong(0),
                    node.path("lastAccess").asLong(0),
                    clients
                ));
            }
            return result;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak admin API to list sessions for user {}: {}", keycloakUserId, e.toString());
            return List.of();
        }
    }

    @Override
    public boolean revokeSession(String sessionId) {
        String token = adminToken();
        if (token == null) {
            return false;
        }
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(adminBaseUri + "/sessions/" + sessionId))
            .header("Authorization", "Bearer " + token)
            .DELETE()
            .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 204) {
                log.warn("Keycloak admin revoke-session failed (HTTP {}) for session {}", response.statusCode(), sessionId);
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak admin API to revoke session {}: {}", sessionId, e.toString());
            return false;
        }
    }

    private String adminToken() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUri))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encode(Map.of(
                "grant_type", "client_credentials",
                "client_id", clientId,
                "client_secret", clientSecret
            ))))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Keycloak admin client_credentials grant failed (HTTP {}): {}", response.statusCode(), response.body());
                return null;
            }
            return objectMapper.readTree(response.body()).get("access_token").asText();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak for admin client_credentials grant: {}", e.toString());
            return null;
        }
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String encode(Map<String, String> form) {
        return form.entrySet().stream()
            .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
    }
}
