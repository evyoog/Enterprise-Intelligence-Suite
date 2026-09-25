package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * A registering company. {@link #parentOrganizationId} is a plain nullable
 * self-reference (not a JPA @ManyToOne — no need to ever load the parent's
 * full graph here) that public registration can NEVER set (see
 * OrganizationRegistrationRequest — there's no field for it); it exists from
 * day one purely so a future Vyoog-Admin-only screen can populate it without
 * a schema change. {@link #code} is a plain unique business identifier, with
 * no tenant/schema-provisioning logic attached to it in this phase.
 */
@Entity
@Table(name = "organization")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 100)
    private String type;

    @Column(length = 150)
    private String industry;

    @Column(length = 500)
    private String website;

    @Column(nullable = false)
    private String businessEmail;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(length = 500)
    private String address;

    @Column(length = 50)
    private String gstin;

    @Column(length = 50)
    private String pan;

    @Column(length = 100)
    private String companyRegistrationNumber;

    @Column(length = 100)
    private String taxVatNumber;

    @Column(nullable = false)
    private boolean billingSameAsAddress = true;

    @Column(length = 500)
    private String billingAddress;

    @Column(length = 100)
    private String billingCountry;

    @Column(length = 100)
    private String billingState;

    @Column(length = 100)
    private String billingCity;

    /** Vyoog-Admin-managed only — see this class's own javadoc. */
    @Column(name = "parent_organization_id")
    private Long parentOrganizationId;

    @Column(nullable = false)
    private int licensedSeats = 0;

    /** Phase 7: an ORG_ADMIN-settable policy (MANAGE_ORGANIZATION permission,
     * see OrganizationSelfService#updateMfaPolicy) — when true, a member's
     * login is refused unless their token shows Keycloak's {@code amr}
     * claim contains {@code "otp"}, i.e. they actually used their account's
     * own OTP credential (see AuthController#login and MfaPolicyService). */
    @Column(name = "mfa_required", nullable = false)
    private boolean mfaRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistrationStatus status = RegistrationStatus.PENDING_EMAIL_VERIFICATION;

    /** REQ-TEN-001: platform-admin-managed only (see AdminRegistrationService
     * #suspendOrganization / #activateOrganization / #closeOrganization). */
    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 20)
    private OrganizationLifecycleStatus lifecycleStatus = OrganizationLifecycleStatus.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
