package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 11.01.01 Knowledge Articles (sprint 2027.1.1). 11.01.02 AI Knowledge
 * (index/retrieve/validate against an embedding store) is NOT built — no
 * vector store or embeddings-model decision exists yet (C12/C39); search is
 * plain case-insensitive text matching on title/body, which already
 * satisfies "Search article" without needing one.
 */
@Entity
@Table(name = "knowledge_article")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    /** 02.01.01.03-style plain revision counter (see {@code Product#version}'s
     * own javadoc) — incremented on every edit after creation. Not a full
     * content-versioning history. */
    @Column(nullable = false)
    private Integer version = 1;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
