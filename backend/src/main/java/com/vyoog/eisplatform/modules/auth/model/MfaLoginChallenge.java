package com.vyoog.eisplatform.modules.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A login that passed Keycloak's own password check but is waiting on the
 * Platform TOTP second factor before a real session is created. {@link #id}
 * is a random opaque token — the ONLY thing the browser ever sees; the real
 * Keycloak access/refresh tokens obtained during the password grant are held
 * here, server-side only, until the challenge is verified or expires. Same
 * "opaque id, raw token stays server-side" shape as
 * {@link SsoBridgeSession}, deliberately — see AuthController#login /
 * #verifyPlatformMfa for the two places this is created/consumed.
 */
@Entity
@Table(name = "mfa_login_challenge")
@Getter
@Setter
public class MfaLoginChallenge {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "keycloak_sub", nullable = false)
    private String keycloakSub;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** C29: VERIFY (enter a code) or ENROLL (set up an authenticator first). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MfaChallengeKind kind = MfaChallengeKind.VERIFY;

    /** True when the held tokens came from a token exchange (SAML / OIDC
     * sign-in), so the session is finalized as an impersonated one. */
    @Column(nullable = false)
    private boolean impersonated = false;
}
