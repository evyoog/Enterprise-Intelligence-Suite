package com.vyoog.eisplatform.modules.auth.controller;

import com.vyoog.eisplatform.modules.auth.dto.InternalSsoLogoutRequest;
import com.vyoog.eisplatform.modules.auth.dto.InternalSsoTokenRequest;
import com.vyoog.eisplatform.modules.auth.dto.InternalSsoTokenResponse;
import com.vyoog.eisplatform.modules.auth.model.SsoBridgeSession;
import com.vyoog.eisplatform.modules.auth.service.ImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakPasswordGrantService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakPasswordGrantService.TokenResult;
import com.vyoog.eisplatform.modules.auth.service.SsoBridgeSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

/**
 * Backend-to-backend only — never reachable from a browser (see SecurityConfig:
 * this path is excluded from the public CORS configuration, and every request
 * must carry the shared secret).
 */
@RestController
@RequestMapping("/internal/sso")
public class InternalSsoController {

    private final SsoBridgeSessionService bridgeSessionService;
    private final ImpersonationExchangeService impersonationExchangeService;
    private final KeycloakPasswordGrantService keycloakPasswordGrantService;
    private final String sharedSecret;

    public InternalSsoController(
            SsoBridgeSessionService bridgeSessionService,
            ImpersonationExchangeService impersonationExchangeService,
            KeycloakPasswordGrantService keycloakPasswordGrantService,
            @Value("${vyoog.internal.sso-shared-secret}") String sharedSecret) {
        this.bridgeSessionService = bridgeSessionService;
        this.impersonationExchangeService = impersonationExchangeService;
        this.keycloakPasswordGrantService = keycloakPasswordGrantService;
        this.sharedSecret = sharedSecret;
    }

    /** The *other* Vyoog app's backend calls this to redeem a `vyoog_sso`
     * bridge-session id for a token already scoped to THIS app's own
     * Keycloak client, for the same user — see ImpersonationExchangeService
     * for how, and why a straight client-to-client Token Exchange doesn't
     * work for this. */
    @PostMapping("/token")
    public ResponseEntity<InternalSsoTokenResponse> token(
            @Valid @RequestBody InternalSsoTokenRequest request,
            @RequestHeader(value = "X-Internal-Sso-Secret", required = false) String providedSecret) {
        if (!secretMatches(providedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return bridgeSessionService.find(request.ssoSessionId())
            .flatMap(session -> exchange(session, request.targetClientId()))
            .map(result -> ResponseEntity.ok(
                new InternalSsoTokenResponse(result.accessToken(), result.refreshToken(), result.expiresInSeconds())))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * The *other* app calls this when IT logs out, so this app's own copy of
     * the same bridge session (if it has one — either a real login here, or
     * its own cached copy from an earlier cross-app exchange) also gets torn
     * down. Needed because impersonation-exchanged tokens live in their OWN
     * separate Keycloak session (confirmed live: a fresh login and an
     * impersonation exchange for the same user get different `sid` claims),
     * so ending one side's Keycloak session via the normal end-session call
     * does NOT end the other's — logout has to be explicitly propagated
     * through this bridge instead of relying on a shared Keycloak session.
     * Always 204, even if this app never had a copy of that session — that's
     * a normal, expected case, not an error.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody InternalSsoLogoutRequest request,
            @RequestHeader(value = "X-Internal-Sso-Secret", required = false) String providedSecret) {
        if (!secretMatches(providedSecret)) {
            throw new org.springframework.security.access.AccessDeniedException("Bad internal SSO secret");
        }

        bridgeSessionService.find(request.ssoSessionId()).ifPresent(session -> {
            if (session.isImpersonated()) {
                impersonationExchangeService.logout(session.getRefreshToken());
            } else {
                keycloakPasswordGrantService.logout(session.getRefreshToken());
            }
            bridgeSessionService.delete(request.ssoSessionId());
        });
    }

    private Optional<TokenResult> exchange(SsoBridgeSession session, String targetClientId) {
        return impersonationExchangeService.exchangeForUser(session.getKeycloakSub(), targetClientId);
    }

    private boolean secretMatches(String provided) {
        return provided != null && constantTimeEquals(provided, sharedSecret);
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
