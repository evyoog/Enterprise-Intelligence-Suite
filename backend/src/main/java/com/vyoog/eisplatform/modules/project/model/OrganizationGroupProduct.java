package com.vyoog.eisplatform.modules.project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** REQ-TEN-009: one product (with the role it gives) that a project owns. One row per (project group, product). */
@Entity
@Table(name = "organization_group_product")
@Getter
@Setter
public class OrganizationGroupProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_role", nullable = false, length = 50)
    private String productRole;

    @Column(nullable = false)
    private Instant addedAt = Instant.now();
}
