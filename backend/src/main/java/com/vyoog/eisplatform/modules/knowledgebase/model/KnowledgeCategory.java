package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Knowledge taxonomy: a category (C74). {@code scope} limits it to one
 * content type (for example TROUBLESHOOTING or VIDEO); null = any type. */
@Entity
@Table(name = "knowledge_category",
    uniqueConstraints = @UniqueConstraint(name = "uq_knowledge_category_slug", columnNames = {"scope", "slug"}))
@Getter
@Setter
public class KnowledgeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private KnowledgeContentType scope;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;
}
