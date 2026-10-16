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

    // "My profile" (C107, migration V046): every field below is optional and
    // person-owned; validation limits live in ProfileValidator.
    @Column(name = "preferred_name", length = 60)
    private String preferredName;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(length = 500)
    private String bio;

    @Column(length = 100)
    private String department;

    @Column(name = "address_line1", length = 150)
    private String addressLine1;

    @Column(name = "address_line2", length = 150)
    private String addressLine2;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(name = "postal_code", length = 12)
    private String postalCode;

    @Column(name = "linkedin_url", length = 200)
    private String linkedinUrl;

    @Column(name = "website_url", length = 200)
    private String websiteUrl;

    /** Stamped when the person (or a platform admin) saves the profile form. */
    @Column(name = "profile_updated_at")
    private Instant profileUpdatedAt;

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
