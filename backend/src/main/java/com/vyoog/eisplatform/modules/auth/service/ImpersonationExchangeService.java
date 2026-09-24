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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mints a token for the *other* app's own client, for a user already known to
 * be recently authenticated (via the vyoog_sso bridge — see
 * SsoBridgeSessionService), with no password and no Keycloak UI.
 *
 * This is NOT "take App A's token and narrow it to App B's audience" — Keycloak's
 * Standard Token Exchange rejects that outright for two public/PKCE clients
 * ("Client is not the holder of the token": confirmed live, a client can only
 * present a subject_token that was issued TO ITSELF). The correct mechanism for
 * "mint a token for user X under client Y, with no credentials, on behalf of a
 * different client" is Keycloak's IMPERSONATION exchange: authenticate as a
 * trusted confidential service client (the shared "eVyoog" client, already used
 * elsewhere in this realm), pass requested_subject (a Keycloak user id, not a
 * token), and audience (the target app's own client). Requires, all granted via
 * the Admin Console (see CLAUDE.md's SSO decisions for the full story of finding
 * these three — each one alone still 403'd):
 *  1. eVyoog's service account holds the realm-management "impersonation" role.
 *  2. eVyoog is confidential (Client authentication on) — Keycloak silently
 *     refuses to let a public client be an exchange audience at all, and by
 *     extension exchanger.
 *  3. The realm's Users → Permissions → "impersonate" permission has a policy
 *     naming eVyoog as an allowed client — a DIFFERENT, realm-wide permission
 *     from any per-client "token-exchange" permission (that one only governs
 *     the `audience` side, e.g. which clients may exchange INTO eis-pms-ui).
 *
 * One consequence worth knowing: the resulting token's azp is "eVyoog" (the
 * impersonating identity), not the target client, even though the target
 * client is in `aud`. Its refresh token also turns out not to be renewable
 * via a normal refresh grant at all — confirmed live, both eVyoog's own
 * credentials AND the target app's own credentials get
 * "Session doesn't have required client" / "Token client and authorized
 * client don't match" respectively. So callers never refresh an exchanged
 * token — they just call exchangeForUser() again (cheap: no password, no
 * network call to the other app, since the sub is already cached locally —
 * see SsoBridgeSession.impersonated, which callers check to know whether a
 * bridge row needs re-exchanging here or a normal refresh via the app's own
 * ROPC client). logout() below still works fine with the stored refresh
 * token, though — only renewal is broken, not revocation.
 */
@Slf4j
@Service
public class ImpersonationExchangeService {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;
    private final String tokenUri;
    private final String endSessionUri;
    private final String impersonationClientId;
    private final String impersonationClientSecret;

    public ImpersonationExchangeService(
            ObjectMapper objectMapper,
            @Value("${vyoog.keycloak.token-uri}") String tokenUri,
            @Value("${vyoog.keycloak.end-session-uri}") String endSessionUri,
            @Value("${vyoog.internal.impersonation-client-id}") String impersonationClientId,
            @Value("${vyoog.internal.impersonation-client-secret}") String impersonationClientSecret) {
        this.objectMapper = objectMapper;
        this.tokenUri = tokenUri;
        this.endSessionUri = endSessionUri;
        this.impersonationClientId = impersonationClientId;
        this.impersonationClientSecret = impersonationClientSecret;
    }

    /** Empty means Keycloak refused the exchange (missing impersonation
     * permission, or the subject no longer exists) — callers should treat
     * this like "not authenticated," never crash. */
    public Optional<KeycloakPasswordGrantService.TokenResult> exchangeForUser(String keycloakSub, String targetClientId) {
        return requestToken(Map.of(
            "grant_type", "urn:ietf:params:oauth:grant-type:token-exchange",
            "requested_subject", keycloakSub,
            "audience", targetClientId
        ), "impersonation exchange for target client " + targetClientId);
    }

    /** Ends the impersonation-created session behind a refresh token from
     * {@link #exchangeForUser} — via eVyoog's own credentials, same reason as
     * refresh() above. This is a genuinely SEPARATE Keycloak session from
     * whichever real login originally created the bridge (confirmed live:
     * they carry different `sid` claims), so ending it here does NOT end the
     * other app's own real session — see InternalSsoController's own
     * /internal/sso/logout, which is what makes logout actually propagate
     * between apps despite that. Best-effort: never throws. */
    public void logout(String refreshToken) {
        Map<String, String> form = new HashMap<>();
        form.put("client_id", impersonationClientId);
        form.put("client_secret", impersonationClientSecret);
        form.put("refresh_token", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endSessionUri))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encode(form)))
            .build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak to end impersonated session: {}", e.toString());
        }
    }

    private Optional<KeycloakPasswordGrantService.TokenResult> requestToken(Map<String, String> form, String opDescription) {
        Map<String, String> withAuth = new HashMap<>(form);
        withAuth.put("client_id", impersonationClientId);
        withAuth.put("client_secret", impersonationClientSecret);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUri))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encode(withAuth)))
            .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("Unable to reach Keycloak for {}: {}", opDescription, e.toString());
            return Optional.empty();
        }

        if (response.statusCode() != 200) {
            log.warn("{} failed (HTTP {}): {}", opDescription, response.statusCode(), response.body());
            return Optional.empty();
        }

        try {
            JsonNode body = objectMapper.readTree(response.body());
            return Optional.of(new KeycloakPasswordGrantService.TokenResult(
                body.get("access_token").asText(),
                body.get("refresh_token").asText(),
                body.get("expires_in").asLong()
            ));
        } catch (IOException e) {
            log.warn("Malformed response from Keycloak for {}: {}", opDescription, e.toString());
            return Optional.empty();
        }
    }

    private static String encode(Map<String, String> form) {
        return form.entrySet().stream()
            .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
    }
}
