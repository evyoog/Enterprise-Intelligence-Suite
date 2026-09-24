package com.vyoog.eisplatform.modules.dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 17: one search query a customer actually typed on the storefront —
 * NOT tied to any product id (a query might match zero products, and the
 * catalog it searched is free to change later), so this has no foreign key
 * to the product catalog at all, unlike FavoriteProduct/ProductUsage.
 */
@Entity
@Table(name = "search_history_entry")
@Getter
@Setter
public class SearchHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 200)
    private String query;

    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt = Instant.now();
}
