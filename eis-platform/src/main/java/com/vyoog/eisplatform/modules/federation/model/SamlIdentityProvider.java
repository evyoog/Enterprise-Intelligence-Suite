package com.vyoog.eisplatform.modules.federation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 4 (2026.3.3): an organization's own external SAML Identity Provider
 * configuration. This app is always the SAML Service Provider (SP) here,
 * never the IdP — {@link #certificatePem} is the IdP's own signing
 * certificate (used to VERIFY assertions it sends us), never a private key;
 * this app has no SAML private key of its own to store, since it never
 * signs anything as an IdP would.
 *
 * <p>Multiple rows per organization are allowed (e.g. staging a replacement
 * IdP before cutting over), but the database enforces at most one
 * {@link #enabled} row per organization ({@code idx_saml_idp_one_enabled_per_org},
 * a partial unique index) — Phase 5's login flow needs exactly one
 * unambiguous IdP per organization to redirect to.
 */
@Entity
@Table(name = "saml_identity_provider")
@Getter
@Setter
public class SamlIdentityProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false)
    private String name;

    /** The IdP's own SAML entity id — an identifier string, not necessarily
     * a dereferenceable URL (SAML only requires it be a URI). */
    @Column(name = "entity_id", nullable = false, length = 500)
    private String entityId;

    /** The IdP's SSO endpoint this app redirects the browser to at login
     * (Phase 5) — the HTTP-Redirect binding's Location. */
    @Column(name = "sso_url", nullable = false, length = 500)
    private String ssoUrl;

    /** PEM-encoded X.509 certificate — used to verify the IdP's assertion
     * signatures (Phase 5), never a private key. */
    @Column(name = "certificate_pem", nullable = false, columnDefinition = "TEXT")
    private String certificatePem;

    /** The raw IdP metadata XML this was parsed from, if it was uploaded as
     * metadata rather than entered field-by-field — kept only for
     * display/re-parsing convenience, never re-derived from automatically
     * on every read (see SamlProviderService#createFromMetadata). */
    @Column(name = "metadata_xml", columnDefinition = "TEXT")
    private String metadataXml;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
