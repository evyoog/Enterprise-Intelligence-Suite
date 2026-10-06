package com.vyoog.eisplatform.modules.search.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One searchable record in the search index (REQ-PRT-002, C70). Built from a
 * product, a published knowledge article or a support ticket by
 * {@code SearchDocumentBuilder}. The PostgreSQL table also has a generated
 * {@code tsv} column (full-text vector over the *_folded columns, English,
 * Spanish and unstemmed); it is not mapped here, so the same entity works on
 * the H2 test database.
 */
@Entity
@Table(name = "search_document",
    uniqueConstraints = @UniqueConstraint(name = "uq_search_document_source", columnNames = {"source_type", "source_id"}))
@Getter
@Setter
public class SearchDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SearchSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SearchVisibility visibility;

    @Column(name = "owner_customer_id")
    private Long ownerCustomerId;

    /** The exact ID a user can type to find this record, for example "#42". */
    @Column(nullable = false, length = 40)
    private String reference;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(length = 2000)
    private String keywords;

    @Column(name = "title_folded", nullable = false, length = 300)
    private String titleFolded;

    @Column(name = "body_folded", columnDefinition = "TEXT")
    private String bodyFolded;

    @Column(name = "keywords_folded", length = 2000)
    private String keywordsFolded;

    @Column(name = "content_updated_at")
    private Instant contentUpdatedAt;

    @Column(name = "indexed_at", nullable = false)
    private Instant indexedAt;
}
