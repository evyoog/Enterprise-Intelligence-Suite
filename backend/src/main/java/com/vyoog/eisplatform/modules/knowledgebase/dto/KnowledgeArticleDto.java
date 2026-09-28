package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;

import java.time.Instant;

public record KnowledgeArticleDto(
    Long id,
    String title,
    String body,
    ArticleStatus status,
    Integer version,
    Instant updatedAt
) {
}
