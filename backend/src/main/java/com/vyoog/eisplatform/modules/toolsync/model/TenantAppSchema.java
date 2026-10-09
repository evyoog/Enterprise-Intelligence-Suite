package com.vyoog.eisplatform.modules.toolsync.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Where an organization's data lives in one tool and how far it has been synchronized. REQ-INT-003, table {@code tenant_app_schema}. */
@Entity
@Table(name = "tenant_app_schema", uniqueConstraints = @UniqueConstraint(name = "uq_tenant_app_schema", columnNames = {"organization_id", "product_id"}))
@Getter
@Setter
public class TenantAppSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** The {@code tenantRef} of every message: the platform organization id as a string. */
    @Column(name = "tenant_ref", nullable = false, length = 100)
    private String tenantRef;

    /** Which datasource the TOOL should put the tenant on: a reference the tool has in its own configuration, never a URL. */
    @Column(name = "datasource_ref", nullable = false, length = 60)
    private String datasourceRef = "default";

    @Column(name = "schema_name", length = 63)
    private String schemaName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantSchemaStatus status = TenantSchemaStatus.PENDING;

    @Column(name = "schema_version", length = 50)
    private String schemaVersion;

    @Column(name = "last_synced_version", nullable = false)
    private long lastSyncedVersion;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
