package com.vyoog.eisplatform.modules.search.dto;

import java.util.List;

/**
 * Global Search response (01.03.01; REQ-PRT-002/003, C70).
 *
 * @param products / knowledgeArticles / tickets the results grouped by type (as before C70)
 * @param results        every result in one list, best first
 * @param didYouMean     a corrected query when the query had a likely typo, else null
 * @param semanticStatus USED, UNAVAILABLE (the model did not answer; keyword
 *                       results only), DISABLED (not configured), NOT_APPLICABLE
 *                       (keyword-only request, blank query, or only private record types)
 * @param engine         POSTGRES (search index) or BASIC (fallback when the index is not installed)
 */
public record GlobalSearchResultDto(
    List<SearchResultItemDto> products,
    List<SearchResultItemDto> knowledgeArticles,
    List<SearchResultItemDto> tickets,
    List<SearchResultItemDto> results,
    String didYouMean,
    String semanticStatus,
    String engine,
    long tookMs
) {

    public GlobalSearchResultDto(List<SearchResultItemDto> products, List<SearchResultItemDto> knowledgeArticles,
                                 List<SearchResultItemDto> tickets) {
        this(products, knowledgeArticles, tickets, concat(products, knowledgeArticles, tickets), null,
            "NOT_APPLICABLE", "BASIC", 0);
    }

    private static List<SearchResultItemDto> concat(List<SearchResultItemDto> a, List<SearchResultItemDto> b,
                                                    List<SearchResultItemDto> c) {
        List<SearchResultItemDto> all = new java.util.ArrayList<>(a);
        all.addAll(b);
        all.addAll(c);
        return all;
    }
}
