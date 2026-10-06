package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;

import java.time.Instant;
import java.util.List;

/** One row of the Knowledge Management content list. */
public record KnowledgeContentRowDto(
    Long id,
    KnowledgeContentType contentType,
    String title,
    String shortDescription,
    Long productId,
    Long moduleId,
    Long categoryId,
    List<String> tags,
    KnowledgeAudience audience,
    KnowledgeWorkflowState workflowState,
    boolean live,
    String liveVersion,
    boolean expired,
    boolean requiresReview,
    boolean featured,
    long views,
    Instant updatedAt,
    String videoSource,
    Integer durationSeconds,
    String thumbnailUrl
) {
}
