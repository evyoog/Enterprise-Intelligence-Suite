package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;

import java.time.Instant;
import java.util.List;

/** One content item for a reader (the live version, or a preview of a draft). */
public record KnowledgeItemDto(
    Long id,
    KnowledgeContentType contentType,
    String slug,
    String title,
    String shortDescription,
    JsonNode blocks,
    JsonNode typeFields,
    Long productId,
    String productName,
    String productSlug,
    Long moduleId,
    String moduleName,
    String categoryName,
    String feature,
    List<String> tags,
    String versionLabel,
    String productVersion,
    String documentationVersion,
    Instant publishedAt,
    Instant updatedAt,
    int readingMinutes,
    long views,
    boolean deprecated,
    String difficulty,
    String directActionRoute,
    String directActionLabel,
    List<KnowledgeSummaryDto> related,
    KnowledgeVideoDto video,
    boolean bookmarked,
    boolean preview
) {
}
