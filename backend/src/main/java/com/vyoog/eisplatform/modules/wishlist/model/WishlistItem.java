package com.vyoog.eisplatform.modules.wishlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C94 (REQ-MKT-004): one product a customer saved. A product is saved at most once per customer (BR-1). */
@Entity
@Table(name = "wishlist_item", uniqueConstraints = @UniqueConstraint(name = "uq_wishlist_customer_product", columnNames = {"customer_id", "product_id"}))
@Getter
@Setter
public class WishlistItem {

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
