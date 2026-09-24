package com.vyoog.eisplatform.modules.federation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 5 (2026.3.3): one row per AuthnRequest this app itself has sent to an
 * IdP (see SamlAuthenticationService#buildRedirectUrl), keyed by that
 * request's own SAML id ({@link #id} — the same value the library later
 * reports as the incoming Response's {@code InResponseTo}). Three things
 * this enables together:
 * <ul>
 *   <li>Rejects unsolicited responses outright — an incoming Response whose
 *   {@code InResponseTo} doesn't match a row here was never asked for.</li>
 *   <li>{@link #expiresAt} bounds how long a login attempt may take.</li>
 *   <li>{@link #consumedAt}, set exactly once inside the same transaction
 *   that finalizes a session, is the actual replay defense: a second
 *   presentation of the same signed Response is refused even though the
 *   signature itself would still validate.</li>
 * </ul>
 */
@Entity
@Table(name = "saml_login_request")
@Getter
@Setter
public class SamlLoginRequest {

    /** The SAML AuthnRequest's own id string (library-generated), not a
     * database-generated surrogate — this IS the value looked up against an
     * incoming Response's InResponseTo. */
    @Id
    private String id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;
}
