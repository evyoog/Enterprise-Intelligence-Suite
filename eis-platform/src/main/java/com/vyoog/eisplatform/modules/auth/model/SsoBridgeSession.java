package com.vyoog.eisplatform.modules.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A cross-app "I'm already logged in somewhere" marker. The random {@link #id}
 * (not a Keycloak token) is the only thing that ever reaches the browser, as
 * the HttpOnly `vyoog_sso` cookie — the actual Keycloak refresh token here
 * never leaves this backend. The *other* app's backend calls
 * /internal/sso/token with this id (over a shared-secret-guarded, backend-only
 * channel) to obtain a fresh access token for the same user, which it then
 * exchanges (Keycloak Token Exchange) for its own client's own token. See
 * CLAUDE.md's SSO decisions for the full design.
 */
@Entity
@Table(name = "sso_bridge_session")
@Getter
@Setter
public class SsoBridgeSession {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false)
    private String keycloakSub;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    /** True when {@link #refreshToken} was minted via the shared "eVyoog"
     * impersonation client (see ImpersonationExchangeService) rather than
     * this app's own ROPC client — Keycloak refresh tokens are bound to
     * whichever client requested them (confirmed live: refreshing one under
     * the "wrong" client fails with "Token client and authorized client
     * don't match"), so this flag decides which credentials to refresh with. */
    @Column(nullable = false)
    private boolean impersonated;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;
}
