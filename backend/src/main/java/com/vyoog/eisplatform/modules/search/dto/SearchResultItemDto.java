package com.vyoog.eisplatform.modules.search.dto;

import java.util.List;

/**
 * One row in a Global Search result (01.03.01; REQ-PRT-002/003, C70) — just
 * enough of whichever entity matched to show and link to it; the full record
 * is still one follow-up call away (product/article/ticket detail).
 *
 * @param reference       the record's exact ID, for example "#42"
 * @param matchType       EXACT_ID, EXACT_PHRASE, KEYWORD, PARTIAL, TYPO or SEMANTIC
 *                        (the label shown next to the result)
 * @param score           relevance; higher is better; only comparable within one response
 * @param titleHighlights matching words in {@code title}
 * @param snippetHighlights matching words in {@code snippet}
 */
public record SearchResultItemDto(
    String type,
    Long id,
    String title,
    String snippet,
    String reference,
    String matchType,
    double score,
    List<SearchHighlightDto> titleHighlights,
    List<SearchHighlightDto> snippetHighlights
) {

    public SearchResultItemDto(String type, Long id, String title, String snippet) {
        this(type, id, title, snippet, "#" + id, "KEYWORD", 0, List.of(), List.of());
    }
}
