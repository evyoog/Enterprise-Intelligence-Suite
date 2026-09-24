package com.vyoog.eisplatform.modules.product.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** One subscription tier (e.g. "Basic", "Pro", "Enterprise") belonging to a Product. */
@Entity
@Table(name = "product_plans")
@Getter
@Setter
public class ProductPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", nullable = false, length = 20)
    private BillingPeriod billingPeriod;

    /** Display order left-to-right on the storefront (lower shows first). */
    private Integer sortOrder;
}
