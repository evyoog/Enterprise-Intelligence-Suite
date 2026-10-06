package com.vyoog.eisplatform.modules.knowledgebase.dto;

import java.util.List;

/** Page of results (EIS convention). */
public record KnowledgePageDto<T>(List<T> items, long totalElements, int page, int size) {
}
