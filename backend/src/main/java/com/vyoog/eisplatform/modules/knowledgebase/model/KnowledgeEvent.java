package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Analytics event (REQ-KNW-006.1). Only the organization id is stored, never
 * the reader (BR-KANL-004); personal history lives in KnowledgeProgress. */
@Entity
@Table(name = "knowledge_event")
@Getter
@Setter
public class KnowledgeEvent {

    public enum Type { CONTENT_VIEWED, VIDEO_PLAYED, VIDEO_PROGRESS, DOWNLOADED, FEEDBACK_GIVEN, TICKET_CREATED_FROM_KNOWLEDGE, BOOKMARKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private Type eventType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "version_label", length = 10)
    private String versionLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 30)
    private KnowledgeContentType contentType;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "source_type", length = 20)
    private String sourceType;

    @Column(name = "organization_id")
    private Long organizationId;

    /** VIDEO_PROGRESS: 25/50/75/100. */
    private Integer percent;

    private Integer seconds;

    /** Hash of the reader (or anonymous session) for the 30-minute view rule
     * (BR-KANL-002); not reversible to a person. */
    @Column(name = "viewer_hash", length = 64)
    private String viewerHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
