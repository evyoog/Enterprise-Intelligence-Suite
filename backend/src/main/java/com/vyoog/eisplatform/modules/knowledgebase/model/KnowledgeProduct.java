package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Knowledge taxonomy: a product (C74 — data, seeded, editable by publishers). */
@Entity
@Table(name = "knowledge_product")
@Getter
@Setter
public class KnowledgeProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 1000)
    private String description;

    /** The catalog product (products.id) this knowledge product documents, if any. */
    @Column(name = "catalog_product_id")
    private Long catalogProductId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;
}
