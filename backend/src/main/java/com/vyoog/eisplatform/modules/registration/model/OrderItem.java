package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** C100 CA-1 B: one product (and plan) of an order. An order holds one or more; the order's own product and plan columns repeat the first item for
 * the earlier single-product API. */
@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Null means the product's flat price. */
    @Column(name = "plan_id")
    private Long planId;
}
