package com.vyoog.eisplatform.modules.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * REQ-TEN-005 (C90): one organization's default for one item of one organization role — a feature permission or a
 * product. No row means the platform role definition applies (permissions) or ON (products, AM-1).
 */
@Entity
@Table(name = "organization_role_default")
@Getter
@Setter
public class OrganizationRoleDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "org_role", nullable = false, length = 20)
    private String orgRole;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    @Column(name = "permission_code", length = 60)
    private String permissionCode;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "updated_by_customer_id")
    private Long updatedByCustomerId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
