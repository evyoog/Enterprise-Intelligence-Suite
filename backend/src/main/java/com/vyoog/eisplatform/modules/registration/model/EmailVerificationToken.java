package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Single-use, expiring email verification token. Only a HASH of the raw
 * token is ever persisted (see EmailVerificationService) — the raw value
 * exists only in the emailed link and is never logged. {@link #usedAt} is
 * set once and never cleared, so a reused link fails instead of silently
 * succeeding again.
 */
@Entity
@Table(name = "email_verification_token")
@Getter
@Setter
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "registrant_type", nullable = false, length = 20)
    private RegistrationOwnerType registrantType;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}
