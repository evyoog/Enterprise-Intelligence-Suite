package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;

import java.time.Instant;
import java.util.List;

/**
 * A content item as Knowledge Management sees it (working copy + state).
 *
 * @param live          a published version is visible to readers
 * @param liveVersion   label of that version ("1.1"), null if none
 * @param expired       past its expiry date (hidden from readers)
 * @param requiresReview past its review date
 */
public record KnowledgeContentDto(
    Long id,
    KnowledgeContentType contentType,
    String title,
    String shortDescription,
    String slug,
    Long productId,
    Long moduleId,
    Long categoryId,
    String feature,
    List<String> tags,
    String keywords,
    KnowledgeAudience audience,
    List<Long> audienceOrganizationIds,
    boolean requireProductAccess,
    String productVersion,
    String documentationVersion,
    Instant effectiveAt,
    Instant reviewAt,
    Instant expiresAt,
    Instant scheduledAt,
    boolean featured,
    String difficulty,
    String directActionRoute,
    String directActionLabel,
    List<Long> relatedContentIds,
    JsonNode blocks,
    JsonNode typeFields,
    KnowledgeWorkflowState workflowState,
    boolean live,
    String liveVersion,
    boolean expired,
    boolean requiresReview,
    String reviewComment,
    String authorSub,
    String reviewerSub,
    String approverSub,
    Instant createdAt,
    Instant updatedAt,
    Instant publishedAt,
    KnowledgeVideoDto video
) {
}
