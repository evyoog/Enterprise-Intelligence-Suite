package com.vyoog.eisplatform.modules.integration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * REQ-INT-001.1 (C61): an API key. Only the prefix and a SHA-256 hash of the
 * full key are stored (BR-1). Status is derived: REVOKED if revoked, else
 * EXPIRED once past {@link #expiresAt}, else ACTIVE.
 */
@Entity
@Table(name = "api_key")
@Getter
@Setter
public class ApiKey {

    public static final String SCOPE_READ = "READ";
    public static final String SCOPE_READ_WRITE = "READ_WRITE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_customer_id", nullable = false)
    private Long ownerCustomerId;

    @Column(name = "owner_keycloak_sub", nullable = false)
    private String ownerKeycloakSub;

    /** Keycloak roles copied at creation, without ROLE_ADMIN (C61 default). */
    @Column(name = "owner_authorities", length = 1000)
    private String ownerAuthorities;

    /** C92 (API-1 B): the organization the key belongs to; empty only for a legacy personal key. */
    @Column(name = "organization_id")
    private Long organizationId;

    /** C92 (API-4 B): READ (read-only) or READ_WRITE. */
    @Column(nullable = false, length = 20)
    private String scope = SCOPE_READ_WRITE;

    /** The administrator who created the key (it keeps working if they leave). */
    @Column(name = "created_by_customer_id")
    private Long createdByCustomerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "key_prefix", nullable = false, unique = true, length = 20)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "request_count", nullable = false)
    private long requestCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public String status(Instant now) {
        if (revokedAt != null) {
            return "REVOKED";
        }
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}
