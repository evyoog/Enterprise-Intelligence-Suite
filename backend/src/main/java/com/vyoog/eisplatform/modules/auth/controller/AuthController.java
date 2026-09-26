package com.vyoog.eisplatform.modules.auth.controller;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.common.exception.PlatformMfaChallengeRequiredException;
import com.vyoog.eisplatform.common.exception.PlatformMfaEnrollmentRequiredException;
import com.vyoog.eisplatform.common.util.JwtPayloadUtil;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.dto.LoginRequest;
import com.vyoog.eisplatform.modules.auth.dto.MfaEnrollResponse;
import com.vyoog.eisplatform.modules.auth.dto.MfaEnrollmentChallengeRequest;
import com.vyoog.eisplatform.modules.auth.dto.MfaLoginVerifyRequest;
import com.vyoog.eisplatform.modules.auth.dto.SignInEnrollmentResponse;
import com.vyoog.eisplatform.modules.auth.dto.TokenResponse;
import com.vyoog.eisplatform.modules.auth.service.ImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.InternalSsoClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakPasswordGrantService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakPasswordGrantService.TokenResult;
import com.vyoog.eisplatform.modules.auth.service.PlatformMfaService;
import com.vyoog.eisplatform.modules.auth.service.SessionCookieService;
import com.vyoog.eisplatform.modules.auth.service.SignInMfaGate;
import com.vyoog.eisplatform.modules.auth.model.MfaChallengeKind;
import com.vyoog.eisplatform.modules.auth.service.SsoBridgeSessionService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = SessionCookieService.REFRESH_COOKIE;
    private static final String SSO_COOKIE = SessionCookieService.SSO_COOKIE;

    private final KeycloakPasswordGrantService keycloakPasswordGrantService;
    private final ImpersonationExchangeService impersonationExchangeService;
    private final SsoBridgeSessionService bridgeSessionService;
    private final InternalSsoClient internalSsoClient;
    private final SignInMfaGate signInMfaGate;
    private final PlatformMfaService platformMfaService;
    private final CurrentCustomerResolver currentCustomerResolver;
    private final AuditService auditService;
    private final SessionCookieService sessionCookieService;
    private final String ssoCookieDomain;
    private final String ownClientId;

    public AuthController(
            KeycloakPasswordGrantService keycloakPasswordGrantService,
            ImpersonationExchangeService impersonationExchangeService,
            SsoBridgeSessionService bridgeSessionService,
            InternalSsoClient internalSsoClient,
            SignInMfaGate signInMfaGate,
            PlatformMfaService platformMfaService,
            CurrentCustomerResolver currentCustomerResolver,
            AuditService auditService,
            SessionCookieService sessionCookieService,
            @Value("${vyoog.internal.sso-cookie-domain:}") String ssoCookieDomain,
            @Value("${vyoog.keycloak.ropc-client-id}") String ownClientId) {
        this.keycloakPasswordGrantService = keycloakPasswordGrantService;
        this.impersonationExchangeService = impersonationExchangeService;
        this.bridgeSessionService = bridgeSessionService;
        this.internalSsoClient = internalSsoClient;
        this.signInMfaGate = signInMfaGate;
        this.platformMfaService = platformMfaService;
        this.currentCustomerResolver = currentCustomerResolver;
        this.auditService = auditService;
        this.sessionCookieService = sessionCookieService;
        this.ssoCookieDomain = ssoCookieDomain;
        this.ownClientId = ownClientId;
    }

    /**
     * Phase 7: {@code request.totp()} flows straight through to Keycloak's
     * own Direct Grant OTP execution (see KeycloakPasswordGrantService) — if
     * that account has OTP configured and this request didn't include a
     * valid code, Keycloak itself rejects the grant and
     * MfaChallengeRequiredException surfaces a distinct 401 the frontend can
     * react to by asking for one (see GlobalExceptionHandler). That's
     * Keycloak-native MFA, for the handful of accounts that already had it
     * configured directly in Keycloak before Phase 2 (2026.3.3) — untouched,
     * still fully supported, still enforced by Keycloak itself.
     *
     * <p>Phase 2 (2026.3.3) adds a SECOND, independent gate that runs only
     * after a real Keycloak grant already succeeded: if this customer has
     * Platform TOTP enabled ({@link PlatformMfaService}), the session is
     * NOT finalized here — a {@link PlatformMfaChallengeRequiredException}
     * is thrown instead, carrying an opaque challenge id the frontend must
     * complete via {@code POST /auth/mfa/verify} before any cookie is set
     * or any access token is actually usable. This is deliberately checked
     * BEFORE the organization-level {@code mfaRequired} policy below — a
     * customer who personally enrolled in Platform MFA is always challenged,
     * regardless of their organization's own policy.
     */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResult result;
        try {
            result = keycloakPasswordGrantService.passwordGrant(request.email(), request.password(), request.totp());
        } catch (RuntimeException ex) {
            // MfaChallengeRequiredException (asking for an OTP) is a normal
            // step in a real login, not a failed one — only a genuine
            // rejection (bad credentials, disabled account, etc.) counts.
            if (!(ex instanceof com.vyoog.eisplatform.common.exception.MfaChallengeRequiredException)) {
                auditService.recordFailure("LOGIN_FAILURE", request.email(), ex.getMessage());
            }
            throw ex;
        }

        Map<String, Object> claims = JwtPayloadUtil.decodePayload(result.accessToken());
        String sub = String.valueOf(claims.get("sub"));
        List<String> amr = amrOf(claims);
        boolean legacyMfaVerified = amr.contains("otp");
        Long customerId = currentCustomerResolver.resolveByKeycloakSub(sub).map(Customer::getId).orElse(null);

        // C29: an authenticator code, or authenticator set-up when the
        // organization requires MFA and the member has none yet.
        var step = signInMfaGate.check(customerId, sub, amr, false, result.accessToken(), result.refreshToken());
        if (step.isPresent()) {
            throw pendingStepException(step.get());
        }

        return finalizeSession(response, sub, customerId, request.email(), result.accessToken(), result.refreshToken(),
            result.expiresInSeconds(), legacyMfaVerified);
    }

    /**
     * Phase 2 (2026.3.3): completes a login that {@link #login} parked on a
     * Platform MFA challenge — the real Keycloak tokens obtained during the
     * password grant were held server-side (see MfaLoginChallenge) the
     * whole time; the browser only ever had the opaque challenge id, never
     * a usable token, so there is no session to forge by skipping this step.
     */
    @PostMapping("/mfa/verify")
    public TokenResponse verifyPlatformMfa(@Valid @RequestBody MfaLoginVerifyRequest request, HttpServletResponse response) {
        PlatformMfaService.LoginChallengeResult result = platformMfaService.verifyLoginChallenge(request.challengeId(), request.code());
        long expiresInSeconds = expiresInSecondsOf(result.accessToken());
        return finalizeSession(response, result.keycloakSub(), result.customerId(), null,
            result.accessToken(), result.refreshToken(), expiresInSeconds, true, result.impersonated());
    }

    /** C29: first half of setting up an authenticator during sign-in — returns
     * the QR code and manual key for the held ENROLL challenge. No password:
     * the user has just signed in. */
    @PostMapping("/mfa/enroll/start")
    public MfaEnrollResponse startSignInEnrollment(@Valid @RequestBody MfaEnrollmentChallengeRequest request) {
        return platformMfaService.startChallengeEnrollment(request.challengeId());
    }

    /** C29: second half — a valid first code enables the authenticator and
     * only then turns the held sign-in into a session. */
    @PostMapping("/mfa/enroll/complete")
    public SignInEnrollmentResponse completeSignInEnrollment(@Valid @RequestBody MfaLoginVerifyRequest request,
                                                             HttpServletResponse response) {
        PlatformMfaService.ChallengeEnrollmentResult result =
            platformMfaService.completeChallengeEnrollment(request.challengeId(), request.code());
        var login = result.login();
        TokenResponse token = finalizeSession(response, login.keycloakSub(), login.customerId(), null,
            login.accessToken(), login.refreshToken(), expiresInSecondsOf(login.accessToken()), true, login.impersonated());
        return new SignInEnrollmentResponse(token.accessToken(), token.expiresInSeconds(), token.mfaVerified(), result.recoveryCodes());
    }

    private static RuntimeException pendingStepException(SignInMfaGate.PendingStep step) {
        return step.kind() == MfaChallengeKind.ENROLL
            ? new PlatformMfaEnrollmentRequiredException(
                "Your organization requires two-factor authentication. Set up an authenticator app to finish signing in.",
                step.challengeId())
            : new PlatformMfaChallengeRequiredException("This account requires a verification code.", step.challengeId());
    }

    /** The one place a session actually becomes real: sets both cookies,
     * creates the cross-app SSO bridge row, records the audit success, and
     * returns the access token to the caller. Reached either directly from
     * {@link #login} (no Platform MFA required) or from
     * {@link #verifyPlatformMfa} (Platform MFA challenge just passed) — by
     * design, this is the ONLY method that calls {@code setRefreshCookie}/
     * {@code setSsoCookie} for a fresh login, so neither path can
     * accidentally skip the org-policy check that already ran before either
     * call site reaches here. */
    private TokenResponse finalizeSession(HttpServletResponse response, String sub, Long customerId, String email,
                                           String accessToken, String refreshToken, long expiresInSeconds, boolean mfaVerified) {
        return finalizeSession(response, sub, customerId, email, accessToken, refreshToken, expiresInSeconds, mfaVerified, false);
    }

    /** {@code impersonated}: the tokens came from a token exchange (a SAML or
     * OIDC sign-in parked on an MFA challenge), see SessionCookieService. */
    private TokenResponse finalizeSession(HttpServletResponse response, String sub, Long customerId, String email,
                                           String accessToken, String refreshToken, long expiresInSeconds, boolean mfaVerified,
                                           boolean impersonated) {
        sessionCookieService.finalizeBrowserSession(response, sub, refreshToken, impersonated);

        auditService.recordSuccess("LOGIN_SUCCESS", sub, customerId, email, "Customer", sub, null, null);

        return new TokenResponse(accessToken, expiresInSeconds, mfaVerified);
    }

    private static long expiresInSecondsOf(String accessToken) {
        Object exp = JwtPayloadUtil.decodePayload(accessToken).get("exp");
        long expEpochSeconds = exp instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(exp));
        return Math.max(0, expEpochSeconds - Instant.now().getEpochSecond());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
                                  @CookieValue(name = SSO_COOKIE, required = false) String ssoSessionId,
                                  HttpServletResponse response) {
        if (refreshToken == null) {
            throw new InvalidCredentialsException("No session to refresh");
        }
        TokenResult result = refreshWithRightCredentials(refreshToken, ssoSessionId);
        sessionCookieService.setRefreshCookie(response, result.refreshToken());
        if (ssoSessionId != null) {
            bridgeSessionService.updateRefreshToken(ssoSessionId, result.refreshToken());
        }
        return new TokenResponse(result.accessToken(), result.expiresInSeconds(), mfaVerifiedOf(result.accessToken()));
    }

    /**
     * The one endpoint both "am I logged in" checks and the ~20s cross-tab
     * polling loop call. Three cases, in order:
     *  1. Own refresh cookie works → normal, unchanged session.
     *  2. No own cookie (or it's stale — see below), but vyoog_sso points at
     *     a bridge row THIS app itself already has a local copy of (either
     *     its own real login, or a previously-cached cross-app exchange) →
     *     refresh directly, no new exchange needed.
     *  3. No own cookie, vyoog_sso points at a row only the OTHER app has →
     *     redeem it there (backend-to-backend impersonation exchange) — the
     *     actual cross-app auto-login.
     * Anything else: 401, meaning genuinely signed out everywhere.
     *
     * A present-but-invalid eis_rt cookie must NOT short-circuit straight to
     * 401 — found live: a stale cookie from before this app's Keycloak
     * client became confidential (see decision 18) failed its refresh, and
     * that failure used to propagate directly instead of falling through to
     * the vyoog_sso bridge check, making cross-app auto-login look broken
     * even though the bridge itself was completely fine. Any real "own
     * cookie" attempt failure now falls through to case 2/3 exactly as if
     * eis_rt had never been sent at all.
     */
    @GetMapping("/session")
    public TokenResponse session(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
                                  @CookieValue(name = SSO_COOKIE, required = false) String ssoSessionId,
                                  HttpServletResponse response) {
        if (refreshToken != null) {
            try {
                return refresh(refreshToken, ssoSessionId, response);
            } catch (RuntimeException ignored) {
                // Falls through below — see this method's own javadoc.
            }
        }

        if (ssoSessionId == null) {
            throw new InvalidCredentialsException("Not authenticated");
        }

        return bridgeSessionService.find(ssoSessionId)
            .map(bridge -> {
                // Impersonation-exchanged refresh tokens can't be renewed via
                // a normal refresh grant (confirmed live: Keycloak rejects it
                // under EITHER eVyoog's or this app's own client credentials
                // with "Session doesn't have required client") — so instead
                // of refreshing, just redo the exchange; we already have the
                // sub cached locally, no need to even call the other app again.
                TokenResult result = bridge.isImpersonated()
                    ? impersonationExchangeService.exchangeForUser(bridge.getKeycloakSub(), ownClientId)
                        .orElseThrow(() -> new InvalidCredentialsException("Not authenticated"))
                    : keycloakPasswordGrantService.refresh(bridge.getRefreshToken());
                sessionCookieService.setRefreshCookie(response, result.refreshToken());
                bridgeSessionService.updateRefreshToken(ssoSessionId, result.refreshToken());
                return new TokenResponse(result.accessToken(), result.expiresInSeconds(), mfaVerifiedOf(result.accessToken()));
            })
            .orElseGet(() -> {
                TokenResult exchanged = internalSsoClient.redeemToken(ssoSessionId)
                    .orElseThrow(() -> new InvalidCredentialsException("Not authenticated"));
                sessionCookieService.setRefreshCookie(response, exchanged.refreshToken());
                String sub = String.valueOf(JwtPayloadUtil.decodePayload(exchanged.accessToken()).get("sub"));
                bridgeSessionService.createOrUpdate(ssoSessionId, sub, exchanged.refreshToken());
                return new TokenResponse(exchanged.accessToken(), exchanged.expiresInSeconds(), mfaVerifiedOf(exchanged.accessToken()));
            });
    }

    /** Cheap polling target — same three-way logic as /session, just without
     * the frontend needing to distinguish "first load" from "still alive?". */
    @GetMapping("/ping")
    public TokenResponse ping(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
                               @CookieValue(name = SSO_COOKIE, required = false) String ssoSessionId,
                               HttpServletResponse response) {
        return session(refreshToken, ssoSessionId, response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
                        @CookieValue(name = SSO_COOKIE, required = false) String ssoSessionId,
                        HttpServletResponse response) {
        if (ssoSessionId != null) {
            bridgeSessionService.find(ssoSessionId).ifPresent(bridge -> {
                String sub = bridge.getKeycloakSub();
                Long customerId = currentCustomerResolver.resolveByKeycloakSub(sub).map(c -> c.getId()).orElse(null);
                auditService.recordSuccess("LOGOUT", sub, customerId, null, "Customer", sub, null, null);
            });
        }
        if (refreshToken != null) {
            // Ends this app's own Keycloak session server-side. Which
            // credentials that requires depends on whether this cookie holds
            // a real login's own refresh token or one from a cross-app
            // impersonation exchange — see ImpersonationExchangeService's
            // javadoc for why those need different client credentials, same
            // as refreshWithRightCredentials() above.
            boolean impersonated = ssoSessionId != null && bridgeSessionService.find(ssoSessionId)
                .map(com.vyoog.eisplatform.modules.auth.model.SsoBridgeSession::isImpersonated)
                .orElse(false);
            if (impersonated) {
                impersonationExchangeService.logout(refreshToken);
            } else {
                keycloakPasswordGrantService.logout(refreshToken);
            }
        }
        if (ssoSessionId != null) {
            bridgeSessionService.delete(ssoSessionId);
            // Impersonation-exchanged sessions are their OWN separate
            // Keycloak session (confirmed live — different `sid` than the
            // real login that started the bridge), so ending this app's own
            // session above never implicitly ends the other app's — this
            // explicit call is what actually makes logout propagate.
            internalSsoClient.notifyLogout(ssoSessionId);
        }
        sessionCookieService.clearCookie(response, REFRESH_COOKIE, SessionCookieService.REFRESH_COOKIE_PATH, null);
        sessionCookieService.clearCookie(response, SSO_COOKIE, "/", ssoCookieDomain);
    }

    /** Dispatches to whichever client actually owns this refresh token — see
     * ImpersonationExchangeService's own javadoc for why this distinction is
     * required at all (Keycloak ties a refresh token to whichever client
     * requested it). Defaults to this app's own client when there's no
     * bridge row to consult (the ordinary, non-cross-app case). */
    private TokenResult refreshWithRightCredentials(String refreshToken, String ssoSessionId) {
        return ssoSessionId == null
            ? keycloakPasswordGrantService.refresh(refreshToken)
            : bridgeSessionService.find(ssoSessionId)
                .map(bridge -> bridge.isImpersonated()
                    ? impersonationExchangeService.exchangeForUser(bridge.getKeycloakSub(), ownClientId)
                        .orElseThrow(() -> new InvalidCredentialsException("Not authenticated"))
                    : keycloakPasswordGrantService.refresh(refreshToken))
                .orElseGet(() -> keycloakPasswordGrantService.refresh(refreshToken));
    }

    /** Phase 7: whether Keycloak's own {@code amr} claim on this specific
     * token shows an OTP challenge was completed — informational only here;
     * see {@code #login} for the one place this is actually enforced. */
    private static boolean mfaVerifiedOf(String accessToken) {
        return amrOf(JwtPayloadUtil.decodePayload(accessToken)).contains("otp");
    }

    @SuppressWarnings("unchecked")
    private static List<String> amrOf(Map<String, Object> claims) {
        Object amr = claims.get("amr");
        return amr instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }

}
