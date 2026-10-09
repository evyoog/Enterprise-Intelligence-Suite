package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * A subscription belongs to EXACTLY ONE of an individual customer or an
 * organization — never both, never neither (enforced by a CHECK constraint
 * in schema.sql, mirrored here by the two nullable owner columns). References
 * the existing {@code products} table by id only; nothing about the catalog
 * is duplicated on this entity.
 */
@Entity
@Table(name = "product_subscription")
@EntityListeners({AuditingEntityListener.class, com.vyoog.eisplatform.modules.toolsync.tracking.ToolSyncListener.class})
@Getter
@Setter
public class ProductSubscription implements com.vyoog.eisplatform.modules.toolsync.tracking.ToolSyncAggregate {

    /** REQ-INT-003: per-aggregate version that only goes up (column sync_version, migration V029); see ToolSyncListener. */
    @Column(name = "sync_version", nullable = false)
    private long syncVersion = 1;

    /** The sync_version this row had when it was loaded or last written; never saved. See ToolSyncListener. */
    @jakarta.persistence.Transient
    private Long loadedSyncVersion;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    private RegistrationOwnerType ownerType;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    @Column(name = "owner_organization_id")
    private Long ownerOrganizationId;

    /** 07.01.02 Subscription Changes (sprint 2026.4.3): which of the
     * product's pricing tiers (see {@code product_plans}) this subscription
     * is on — null means the product's flat, un-tiered price (see
     * Product#price's own javadoc). Not a JPA relationship (no need to load
     * a plan's full row here) — see SubscriptionService#changePlan for the
     * one place this is validated against the subscription's own product. */
    @Column(name = "plan_id")
    private Long planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status = SubscriptionStatus.PENDING_SUBSCRIPTION;

    private Instant startedAt;

    /** The end of the current term; for a paid Monthly/Yearly plan this is
     * also the renewal date (REQ-SUB-004.1). */
    private Instant expiresAt;

    /** REQ-SUB-003.1 (C63): seats of an organization subscription (1–100 000);
     * always 1 for an individual subscription. */
    @Column(nullable = false)
    private int quantity = 1;

    /** REQ-SUB-004.1 (C64): renewed automatically on its renewal date. On by
     * default; turning it off is the FRD's open question 1. */
    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = true;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
