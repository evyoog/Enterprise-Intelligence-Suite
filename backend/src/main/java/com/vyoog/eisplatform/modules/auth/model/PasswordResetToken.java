package com.vyoog.eisplatform.modules.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Single-use, expiring password-reset token — mirrors {@code EmailVerificationToken}'s
 * exact shape and conventions (only a HASH of the raw token is ever
 * persisted; {@link #usedAt} is set once and never cleared). Keyed by
 * {@link #keycloakSub} (the Keycloak user's own id), not a {@code Customer}
 * id — password reset is an identity-level action Keycloak owns; it applies
 * to any real Keycloak account regardless of whether it happens to be
 * linked to a Vyoog {@code Customer} row yet.
 */
@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "keycloak_sub", nullable = false)
    private String keycloakSub;

    /** Denormalized for the confirmation email/audit trail — the lookup
     * itself always goes through {@link #keycloakSub}. */
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}
