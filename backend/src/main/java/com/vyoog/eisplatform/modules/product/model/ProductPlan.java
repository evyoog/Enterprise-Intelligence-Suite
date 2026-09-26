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

    /** 02.05.02.01 Configure currency (sprint 2026.4.1) — see Currency's own javadoc. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency = Currency.USD;

    /** 02.03.01.03 Define usage limits (sprint 2026.4.1): the included unit
     * allowance before {@link #overageCharge} applies (e.g. "1000 API calls").
     * Null means unlimited/not metered. */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /** 02.03.01.04 Define included features (sprint 2026.4.1): a short,
     * free-text, comma-separated list shown on the plan card. Not a
     * structured feature-flag model — that doesn't exist anywhere in this
     * backend yet. */
    @Column(name = "included_features", length = 1000)
    private String includedFeatures;

    /** 02.03.02.02 Define usage price (sprint 2026.4.1): price per unit
     * beyond a metered plan with no fixed {@link #usageLimit}. Null for a
     * flat subscription plan. */
    @Column(name = "usage_price", precision = 12, scale = 4)
    private BigDecimal usagePrice;

    /** 02.03.02.03 Define tier price (sprint 2026.4.1): a short free-text
     * description of volume/tier pricing bands (e.g. "1-100 units $2/unit,
     * 101+ $1.50/unit"). A real tiered-pricing engine belongs to 08 Billing
     * & Payments (a later sprint) — this is advisory display text only. */
    @Column(name = "tier_pricing", length = 500)
    private String tierPricing;

    /** 02.03.02.04 Define overage charge (sprint 2026.4.1): price per unit
     * once {@link #usageLimit} is exceeded. Null means no overage charge
     * (usage above the limit is simply not covered — 09 Orders/enforcement
     * do not exist yet either). */
    @Column(name = "overage_charge", precision = 12, scale = 4)
    private BigDecimal overageCharge;
}
