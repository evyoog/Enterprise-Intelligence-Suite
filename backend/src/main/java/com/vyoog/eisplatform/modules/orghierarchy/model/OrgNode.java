package com.vyoog.eisplatform.modules.orghierarchy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** One node of an organization's hierarchy tree (REQ-TEN-006). */
@Entity
@Table(name = "org_node")
@Getter
@Setter
public class OrgNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /** Null only for the single root of the organization (BR-ORG-003). */
    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType;

    @Column(length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
