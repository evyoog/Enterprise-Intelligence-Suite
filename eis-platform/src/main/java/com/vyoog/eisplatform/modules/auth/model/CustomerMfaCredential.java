package com.vyoog.eisplatform.modules.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One row per customer — Platform-owned TOTP second factor, layered on top
 * of (not replacing) Keycloak's own username/password authentication. See
 * PlatformMfaService's own javadoc for why this exists as a separate,
 * app-owned credential rather than a Keycloak-native one.
 *
 * {@link #encryptedSecret} holds either a PENDING (enabled = false) or
 * ACTIVE (enabled = true) secret, always encrypted at rest via
 * {@link com.vyoog.eisplatform.modules.auth.service.TotpSecretCipher} —
 * never plaintext. A pending secret that's never verified is inert (it can
 * never gate a login while enabled is false) and is simply overwritten the
 * next time enrollment starts.
 */
@Entity
@Table(name = "customer_mfa")
@Getter
@Setter
public class CustomerMfaCredential {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "encrypted_secret", columnDefinition = "TEXT")
    private String encryptedSecret;

    @Column(name = "secret_set_at")
    private Instant secretSetAt;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
