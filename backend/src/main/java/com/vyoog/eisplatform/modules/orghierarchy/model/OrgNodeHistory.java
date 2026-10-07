package com.vyoog.eisplatform.modules.orghierarchy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Append-only record of one move (BR-ORG-009). */
@Entity
@Table(name = "org_node_history")
@Getter
@Setter
public class OrgNodeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "org_node_id", nullable = false)
    private Long orgNodeId;

    @Column(name = "previous_parent_id")
    private Long previousParentId;

    @Column(name = "new_parent_id")
    private Long newParentId;

    @Column(name = "changed_by_customer_id")
    private Long changedByCustomerId;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt = Instant.now();
}
