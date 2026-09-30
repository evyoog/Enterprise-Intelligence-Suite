package com.vyoog.eisplatform.modules.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** Belongs to EXACTLY ONE of an individual customer or an organization —
 * same ownership shape as {@code ProductSubscription} (see its own javadoc),
 * enforced by a CHECK constraint in schema.sql. REQ-BIL-001.1. */
@Entity
@Table(name = "billing_details")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BillingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    @Column(name = "owner_organization_id")
    private Long ownerOrganizationId;

    @Column(name = "billing_name", nullable = false, length = 200)
    private String billingName;

    @Column(name = "billing_email", nullable = false, length = 255)
    private String billingEmail;

    @Column(name = "address_line1", nullable = false, length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country;

    /** For example GSTIN. Format validation Not specified (FRD Open question 1). */
    @Column(name = "tax_id", length = 50)
    private String taxId;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
