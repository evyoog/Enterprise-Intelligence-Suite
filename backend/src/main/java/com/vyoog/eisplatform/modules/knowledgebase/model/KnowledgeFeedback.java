package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** "Was this helpful?", outdated reports and suggestions (REQ-KNW-005.13). */
@Entity
@Table(name = "knowledge_feedback")
@Getter
@Setter
public class KnowledgeFeedback {

    public enum Kind { VOTE, OUTDATED, SUGGESTION }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "version_label", length = 10)
    private String versionLabel;

    @Column(name = "customer_id")
    private Long customerId;

    /** Token subject of the voter, so a second vote replaces the first (BR-KCEN-003). */
    @Column(name = "voter_sub", length = 100)
    private String voterSub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Kind kind;

    private Boolean helpful;

    @Column(length = 40)
    private String reason;

    @Column(name = "comment_text", length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
