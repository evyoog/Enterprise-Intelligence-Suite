package com.vyoog.eisplatform.modules.serviceinstance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C101 SR-5: the credential a product uses to report the health of its own instances. Only a hash is stored; the key is shown once when created. */
@Entity
@Table(name = "service_reporter_key")
@Getter
@Setter
public class ServiceReporterKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(length = 100)
    private String label;

    @Column(name = "key_prefix", nullable = false, length = 12)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(name = "created_by_keycloak_sub", length = 100)
    private String createdByKeycloakSub;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
