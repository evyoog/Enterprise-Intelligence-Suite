package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;

import java.time.Instant;
import java.util.List;

/** A content card in the Knowledge Center (only visible content, BR-KCEN-001). */
public record KnowledgeSummaryDto(
    Long id,
    KnowledgeContentType contentType,
    String slug,
    String title,
    String shortDescription,
    Long productId,
    String productName,
    String productSlug,
    Long moduleId,
    String moduleName,
    String categoryName,
    List<String> tags,
    int readingMinutes,
    long views,
    String versionLabel,
    Instant updatedAt,
    boolean featured,
    boolean deprecated,
    String difficulty,
    String videoSource,
    Integer durationSeconds,
    String thumbnailUrl
) {
}
