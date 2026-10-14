package com.vyoog.eisplatform.modules.serviceinstance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C101 / REQ-SRM-001: one subscription's provisioned tenant in a hosted product. Health comes from the product's reports (SR-1 A, SR-2 A) or, by hand,
 * from a platform administrator (SR-5 B). */
@Entity
@Table(name = "service_instance")
@Getter
@Setter
public class ServiceInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false, unique = true)
    private Long subscriptionId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    @Column(name = "owner_organization_id")
    private Long ownerOrganizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstanceStatus status = InstanceStatus.PROVISIONING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InstanceHealth health = InstanceHealth.UNKNOWN;

    @Column(name = "health_message", length = 300)
    private String healthMessage;

    @Column(name = "last_reported_at")
    private Instant lastReportedAt;

    @Column(name = "tenant_reference", length = 100)
    private String tenantReference;

    @Column(name = "access_url", length = 500)
    private String accessUrl;

    /** SYSTEM (kept in step with the subscription), PRODUCT (a report) or ADMIN (set by hand). */
    @Column(name = "last_changed_by", nullable = false, length = 10)
    private String lastChangedBy = "SYSTEM";

    @Column(name = "last_change_note", length = 300)
    private String lastChangeNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
