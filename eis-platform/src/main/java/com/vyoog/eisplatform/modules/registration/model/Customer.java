package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * A Vyoog person identity — NOT a Keycloak user. Created directly by
 * individual registration, or as the first ORG_ADMIN of an organization
 * registration (see RegistrationService). {@link #keycloakSub} stays null
 * until a platform admin manually creates the matching Keycloak user and
 * links it (see AdminRegistrationService) — this is the seam automatic
 * Keycloak provisioning can slot into later without any schema change.
 */
@Entity
@Table(name = "customer")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String mobile;

    @Column(length = 100)
    private String country;

    private String companyName;

    @Column(length = 150)
    private String jobTitle;

    @Column(length = 150)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistrationStatus status = RegistrationStatus.PENDING_EMAIL_VERIFICATION;

    /** Null until a platform admin links a manually-created Keycloak user —
     * see this class's own javadoc. */
    @Column(name = "keycloak_sub")
    private String keycloakSub;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
