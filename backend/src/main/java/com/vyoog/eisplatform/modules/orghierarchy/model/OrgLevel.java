package com.vyoog.eisplatform.modules.orghierarchy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A level type of an organization's hierarchy; lower rank = higher in the tree (BR-ORG-005, BR-ORG-006). */
@Entity
@Table(name = "org_level")
@Getter
@Setter
public class OrgLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "level_rank", nullable = false)
    private int levelRank;
}
