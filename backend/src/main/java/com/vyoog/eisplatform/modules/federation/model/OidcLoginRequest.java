package com.vyoog.eisplatform.modules.federation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One OIDC sign-in in flight: {@link #state} is the random value sent to the
 * provider and checked on the callback (CSRF), {@link #nonce} is checked in the
 * ID token (replay), {@link #codeVerifier} is the PKCE secret. Single use.
 */
@Entity
@Table(name = "oidc_login_request")
@Getter
@Setter
public class OidcLoginRequest {

    @Id
    @Column(length = 64)
    private String state;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(nullable = false, length = 64)
    private String nonce;

    @Column(name = "code_verifier", nullable = false, length = 128)
    private String codeVerifier;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;
}
