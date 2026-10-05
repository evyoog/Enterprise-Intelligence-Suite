package com.vyoog.eisplatform.modules.knowledgebase.dto;

import jakarta.validation.constraints.Size;

public record KnowledgeCommentRequest(@Size(max = 1000) String comment) {
}
