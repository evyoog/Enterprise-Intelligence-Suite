package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/** Create or update a content item's working copy (REQ-KNW-002.1–.3, .8). */
public record KnowledgeContentRequest(
    @NotNull KnowledgeContentType contentType,
    @NotBlank @Size(max = 200) String title,
    @Size(max = 500) String shortDescription,
    @Size(max = 200) String slug,
    Long productId,
    Long moduleId,
    Long categoryId,
    @Size(max = 200) String feature,
    List<String> tags,
    @Size(max = 1000) String keywords,
    KnowledgeAudience audience,
    List<Long> audienceOrganizationIds,
    Boolean requireProductAccess,
    @Size(max = 50) String productVersion,
    @Size(max = 50) String documentationVersion,
    Instant effectiveAt,
    Instant reviewAt,
    Instant expiresAt,
    Boolean featured,
    @Size(max = 20) String difficulty,
    @Size(max = 300) String directActionRoute,
    @Size(max = 100) String directActionLabel,
    List<Long> relatedContentIds,
    JsonNode blocks,
    JsonNode typeFields
) {
}
