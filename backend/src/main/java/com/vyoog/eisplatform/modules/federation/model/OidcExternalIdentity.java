package com.vyoog.eisplatform.modules.federation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** The stable OIDC identity (issuer + subject) linked to a Vyoog customer,
 * per organization — the OIDC counterpart of {@link SamlExternalIdentity}. */
@Entity
@Table(name = "oidc_external_identity")
@Getter
@Setter
public class OidcExternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 500)
    private String issuer;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(length = 255)
    private String email;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
