package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * A member's access to one specific product under one specific product-scoped
 * role (e.g. "PMS_ADMIN") — a row existing here is what "assigned" means.
 * Being an {@link OrganizationMember} never implies this, and the
 * organization holding a {@link ProductSubscription} never implies this
 * either — see the Product Suite's two-tier view (org-level vs my-access).
 * {@link #productRole} is a plain string, not a Java enum: the set of valid
 * roles is per-product (PMS_ADMIN/PMS_MANAGER/PMS_USER, RMS_ADMIN/…, etc.)
 * and driven by the backend catalog, not hardcoded here.
 */
@Entity
@Table(name = "organization_product_access")
@Getter
@Setter
public class OrganizationProductAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_member_id", nullable = false)
    private Long organizationMemberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_role", nullable = false, length = 50)
    private String productRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(nullable = false)
    private Instant assignedAt = Instant.now();
}
