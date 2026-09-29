package com.vyoog.eisplatform.modules.knowledgebase.dto;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeArticleRequest(@NotBlank String title, @NotBlank String body) {
}
