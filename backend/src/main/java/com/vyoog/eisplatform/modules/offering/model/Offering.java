package com.vyoog.eisplatform.modules.offering.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * What a customer is offered (REQ-CAT-005, C85): a named group of one or more catalog products. Prices
 * stay on the products' plans, set by the platform administrator; an offering has no price of its own
 * (OF-2 is not decided).
 */
@Entity
@Table(name = "offering")
@Getter
@Setter
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferingStatus status = OfferingStatus.DRAFT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "offering_product", joinColumns = @JoinColumn(name = "offering_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "product_id", nullable = false)
    private List<Long> productIds = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
