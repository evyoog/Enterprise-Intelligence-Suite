package com.vyoog.eisplatform.modules.dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 16: a customer explicitly starring one product — the "favorites"
 * half of personalization. Deliberately keyed by the plain catalog
 * {@code product_id}, not the org-scoped {@code organization_product_access}
 * row — favoriting is a personal bookmark, not an entitlement, and applies
 * the same way whether the caller is an individual or an org member (see
 * this phase's own report for why entitlement is never bypassed by this).
 */
@Entity
@Table(name = "favorite_product")
@Getter
@Setter
public class FavoriteProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
