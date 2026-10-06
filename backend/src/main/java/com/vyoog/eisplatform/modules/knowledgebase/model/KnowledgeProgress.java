package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Reading / watching progress and "recently viewed" (REQ-KNW-005.8). */
@Entity
@Table(name = "knowledge_progress",
    uniqueConstraints = @UniqueConstraint(name = "uq_knowledge_progress", columnNames = {"customer_id", "content_id"}))
@Getter
@Setter
public class KnowledgeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    /** 0–100. */
    @Column(nullable = false)
    private int percent;

    @Column(name = "position_seconds")
    private Integer positionSeconds;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_viewed_at", nullable = false)
    private Instant lastViewedAt = Instant.now();
}
