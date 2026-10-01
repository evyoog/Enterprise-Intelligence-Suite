package com.vyoog.eisplatform.modules.cart.model;

import com.vyoog.eisplatform.modules.product.model.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C59 (REQ-MKT-003): one product and plan in a cart. A product appears at
 * most once per cart (BR-3). {@code unitPriceAtAdd} is the plan price in the
 * currency's smallest unit when added or last confirmed (BR-5, BR-7). */
@Entity
@Table(name = "cart_item", uniqueConstraints = @UniqueConstraint(name = "uq_cart_item_product", columnNames = {"cart_id", "product_id"}))
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "unit_price_at_add", nullable = false)
    private long unitPriceAtAdd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt = Instant.now();
}
