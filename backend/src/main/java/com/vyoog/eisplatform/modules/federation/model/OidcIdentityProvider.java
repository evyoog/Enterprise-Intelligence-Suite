package com.vyoog.eisplatform.modules.federation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * REQ-IAM-006 (C22/C27): an organization's OpenID Connect identity provider.
 * The client secret is stored encrypted (TotpSecretCipher, AES-GCM) and never
 * returned. At most one SAML or OIDC provider is enabled per organization.
 */
@Entity
@Table(name = "oidc_identity_provider")
@Getter
@Setter
public class OidcIdentityProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 255)
    private String name;

    /** The issuer; discovery is read from {issuer}/.well-known/openid-configuration. */
    @Column(name = "issuer_url", nullable = false, length = 500)
    private String issuerUrl;

    @Column(name = "client_id", nullable = false, length = 255)
    private String clientId;

    @Column(name = "encrypted_client_secret", nullable = false, columnDefinition = "TEXT")
    private String encryptedClientSecret;

    @Column(nullable = false, length = 500)
    private String scopes = "openid email profile";

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
