package com.vyoog.eisplatform.modules.federation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 5 (2026.3.3): the STABLE mapping from one external SAML identity to
 * one Vyoog {@code Customer} — keyed by {@code (organizationId, idpEntityId,
 * nameId)}, deliberately never by {@link #email} alone (an IdP-side email can
 * change over time; NameID is the one thing SAML itself defines as the
 * stable subject identifier) and never by the {@code saml_identity_provider}
 * row's own id (a row an org admin can delete and recreate while pointing at
 * the same real-world IdP). Once this row exists, every future login from
 * the same external identity resolves to the same {@link #customerId},
 * never a duplicate — see SamlAuthenticationService for where this is
 * created (first login, JIT-provisioned or linked to an existing Customer by
 * email) and read (every subsequent login).
 */
@Entity
@Table(name = "saml_external_identity")
@Getter
@Setter
public class SamlExternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /** Copied from {@code SamlIdentityProvider.entityId} at the time this
     * identity was first resolved — not a foreign key to that row, since the
     * provider row itself is disposable (see this class's own javadoc). */
    @Column(name = "idp_entity_id", nullable = false, length = 500)
    private String idpEntityId;

    /** The SAML NameID this IdP asserts for this person — this app's chosen
     * stable external identity key, alongside {@link #idpEntityId} and
     * {@link #organizationId}. */
    @Column(name = "name_id", nullable = false, length = 500)
    private String nameId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** Last known email attribute from this identity — kept for display and
     * re-sync convenience only, never part of the lookup key. */
    @Column(length = 255)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
