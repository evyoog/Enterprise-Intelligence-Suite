package com.vyoog.eisplatform.modules.search.dto;

import java.util.List;

public record GlobalSearchResultDto(
    List<SearchResultItemDto> products,
    List<SearchResultItemDto> knowledgeArticles,
    List<SearchResultItemDto> tickets
) {
}
