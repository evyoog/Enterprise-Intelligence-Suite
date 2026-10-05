package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** A reader's bookmark (REQ-KNW-005.8, BR-KCEN-004). */
@Entity
@Table(name = "knowledge_bookmark",
    uniqueConstraints = @UniqueConstraint(name = "uq_knowledge_bookmark", columnNames = {"customer_id", "content_id"}))
@Getter
@Setter
public class KnowledgeBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
