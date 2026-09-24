package com.vyoog.eisplatform.modules.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A single-use Platform MFA recovery code — only a SHA-256 hash is ever
 * persisted (same convention as PasswordResetToken/EmailVerificationToken),
 * never the raw code. The raw value exists only in
 * {@link com.vyoog.eisplatform.modules.auth.service.PlatformMfaService}'s
 * return value at generation time, shown to the customer exactly once.
 */
@Entity
@Table(name = "mfa_recovery_code")
@Getter
@Setter
public class MfaRecoveryCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
