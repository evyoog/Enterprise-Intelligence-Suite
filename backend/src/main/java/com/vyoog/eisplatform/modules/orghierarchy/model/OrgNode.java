package com.vyoog.eisplatform.modules.orghierarchy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** One node of an organization's hierarchy tree (REQ-TEN-006). */
@Entity
@EntityListeners(com.vyoog.eisplatform.modules.toolsync.tracking.ToolSyncListener.class)
@Table(name = "org_node")
@Getter
@Setter
public class OrgNode implements com.vyoog.eisplatform.modules.toolsync.tracking.ToolSyncAggregate {

    /** REQ-INT-003: per-aggregate version that only goes up (column sync_version, migration V029); see ToolSyncListener. */
    @Column(name = "sync_version", nullable = false)
    private long syncVersion = 1;

    /** The sync_version this row had when it was loaded or last written; never saved. See ToolSyncListener. */
    @jakarta.persistence.Transient
    private Long loadedSyncVersion;

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
