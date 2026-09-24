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
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ProductSubscription {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status = SubscriptionStatus.PENDING_SUBSCRIPTION;

    private Instant startedAt;

    private Instant expiresAt;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
