package com.vyoog.eisplatform.modules.product.model;

import com.vyoog.eisplatform.modules.platform.model.Platform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    /**
     * Legacy flat one-time price — kept for backward compatibility (still
     * required at creation), but superseded by {@link #plans} for anything
     * that should actually be sold as a subscription. Not shown when plans
     * exist (see ProductGrid on the frontend); worth removing once nothing
     * still relies on it.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    private String imageUrl;

    /** Where the "Launch" button on this product's card should open. */
    private String launchUrl;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    /** Admin-set flag — whether this app has been wired into the backend-mediated
     * SSO bridge (not auto-detected; the admin flips this on once the target
     * app's own SSO integration is actually built and deployed). */
    @Column(nullable = false)
    private boolean ssoConnected = false;

    /** Which high-level platform(s) (e.g. Thittam) this app is shown under.
     * Many-to-many: the same app can be assigned to more than one platform. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_platforms",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "platform_id")
    )
    private Set<Platform> platforms = new HashSet<>();

    /** Subscription tiers (e.g. Basic/Pro/Enterprise). Empty means "no tiers
     * defined yet" — the flat {@link #price} is what shows in that case. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductPlan> plans = new ArrayList<>();

    /** 02.01.01.03 Version product (sprint 2026.4.1): a plain revision
     * counter, incremented on every {@code updateProduct} call after
     * creation — see ProductService. Not a full content-versioning history
     * (no past revision is kept), just a visible "this has changed N times"
     * count, shown read-only in the admin UI. */
    @Column(nullable = false)
    private Integer version = 1;

    /** 02.01.02.01 Define product hierarchy (sprint 2026.4.1): the product
     * this one is a child of, if any — self-referential, no separate
     * hierarchy table. Null for a top-level product. */
    @Column(name = "parent_product_id")
    private Long parentProductId;

    /** 02.01.02.02 Define variants (sprint 2026.4.1): set alongside
     * {@link #parentProductId} to distinguish sibling variants of the same
     * parent (e.g. "Enterprise", "SMB"). Meaningless without a parent. */
    @Column(name = "variant_label", length = 100)
    private String variantLabel;

    /** 02.01.02.03 Define dependencies (sprint 2026.4.1): other products this
     * one requires — advisory only (nothing today blocks subscribing to this
     * product without the dependency; see the product-lifecycle FRD's
     * business rules for what enforces it later, in 09 Orders). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_dependencies",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "depends_on_product_id")
    )
    private Set<Product> dependsOn = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
