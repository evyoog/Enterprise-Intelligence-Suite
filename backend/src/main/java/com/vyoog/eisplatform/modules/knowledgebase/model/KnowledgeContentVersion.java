package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Immutable snapshot of one published version (BR-KCON-004). Readers are
 * served from the live snapshot, so drafts never leak. */
@Entity
@Table(name = "knowledge_content_version",
    uniqueConstraints = @UniqueConstraint(name = "uq_knowledge_content_version", columnNames = {"content_id", "version_label"}))
@Getter
@Setter
public class KnowledgeContentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "version_label", nullable = false, length = 10)
    private String versionLabel;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String blocks;

    @Column(name = "type_fields", columnDefinition = "TEXT")
    private String typeFields;

    /** Plain text, for the original article endpoints. */
    @Column(nullable = false, length = 20000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false, length = 20)
    private KnowledgeAudience audience = KnowledgeAudience.PUBLIC;

    @Column(name = "audience_org_ids", length = 1000)
    private String audienceOrgIds;

    @Column(name = "require_product_access", nullable = false)
    private boolean requireProductAccess;

    /** Search-only text of this version (type fields, transcript, chapters, taxonomy). */
    @Column(name = "search_text", columnDefinition = "TEXT")
    private String searchText;

    @Column(name = "effective_at")
    private Instant effectiveAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "published_by_sub", length = 100)
    private String publishedBySub;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;
}
