package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Knowledge content item (REQ-KNW-002). The table is the original
 * 11.01.01 {@code knowledge_article} table, extended additively
 * (REQ-KNW-001.7): existing rows keep their ids and become type ARTICLE with
 * a Public audience. The columns here are the working copy; readers see the
 * live snapshot in {@link KnowledgeContentVersion} ({@link #liveVersionId}).
 *
 * <p>{@code status} (DRAFT/PUBLISHED) and {@code body} (plain text) are kept
 * in step for the original endpoints and search (REQ-KNW-001.8, BR-KNW-007).
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

    /** Plain text of the blocks (or the article text for old-style articles). */
    @Column(nullable = false, length = 20000)
    private String body;

    /** PUBLISHED while a live version exists and the item is not archived. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    /** Edit counter (REQ-KNW-001.2), incremented on every save. */
    @Column(nullable = false)
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private KnowledgeContentType contentType = KnowledgeContentType.ARTICLE;

    @Column(length = 220)
    private String slug;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    /** Ordered blocks as JSON (REQ-KNW-002.2), validated by KnowledgeBlocks. */
    @Column(columnDefinition = "TEXT")
    private String blocks;

    /** Type-specific fields as JSON (REQ-KNW-002.3). */
    @Column(name = "type_fields", columnDefinition = "TEXT")
    private String typeFields;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(length = 200)
    private String feature;

    /** Comma-separated. */
    @Column(length = 1000)
    private String tags;

    @Column(length = 1000)
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false, length = 20)
    private KnowledgeAudience audience = KnowledgeAudience.PUBLIC;

    /** ",3,7," — organizations for the ORGANIZATION audience. */
    @Column(name = "audience_org_ids", length = 1000)
    private String audienceOrgIds;

    /** Only readers with product access to the item's product (BR-KVS-001 rule 3). */
    @Column(name = "require_product_access", nullable = false)
    private boolean requireProductAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_state", nullable = false, length = 20)
    private KnowledgeWorkflowState workflowState = KnowledgeWorkflowState.DRAFT;

    @Column(name = "live_version_id")
    private Long liveVersionId;

    /** Label of the live version ("1.0"), or "0.1" before the first publish. */
    @Column(name = "current_version_label", nullable = false, length = 10)
    private String currentVersionLabel = "0.1";

    @Column(name = "product_version", length = 50)
    private String productVersion;

    @Column(name = "documentation_version", length = 50)
    private String documentationVersion;

    @Column(name = "effective_at")
    private Instant effectiveAt;

    @Column(name = "review_at")
    private Instant reviewAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    /** Major/minor choice kept for a scheduled publish. */
    @Column(name = "scheduled_bump", length = 10)
    private String scheduledBump;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "deprecated_at")
    private Instant deprecatedAt;

    @Column(name = "author_sub", length = 100)
    private String authorSub;

    @Column(name = "reviewer_sub", length = 100)
    private String reviewerSub;

    @Column(name = "approver_sub", length = 100)
    private String approverSub;

    @Column(name = "updated_by_sub", length = 100)
    private String updatedBySub;

    /** Comment left when a publisher returned the item to Draft. */
    @Column(name = "review_comment", length = 1000)
    private String reviewComment;

    @Column(nullable = false)
    private boolean featured;

    /** Videos and courses: BEGINNER, INTERMEDIATE, ADVANCED. */
    @Column(length = 20)
    private String difficulty;

    /** EIS route opened by the "direct action" link (REQ-KNW-002.8). */
    @Column(name = "direct_action_route", length = 300)
    private String directActionRoute;

    @Column(name = "direct_action_label", length = 100)
    private String directActionLabel;

    /** ",12,15," */
    @Column(name = "related_content_ids", length = 1000)
    private String relatedContentIds;

    /** Extra text for search only: type fields, transcript, chapters, taxonomy names. */
    @Column(name = "search_text", columnDefinition = "TEXT")
    private String searchText;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
