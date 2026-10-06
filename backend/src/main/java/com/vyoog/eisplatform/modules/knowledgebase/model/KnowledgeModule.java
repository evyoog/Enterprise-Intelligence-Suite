package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Knowledge taxonomy: a module of a knowledge product (C74). */
@Entity
@Table(name = "knowledge_module",
    uniqueConstraints = @UniqueConstraint(name = "uq_knowledge_module_slug", columnNames = {"product_id", "slug"}))
@Getter
@Setter
public class KnowledgeModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;
}
