package com.vyoog.eisplatform.modules.search.dto;

/** One row in a Global Search result (sprint 2027.1.3, 01.03.01) — just
 * enough of whichever entity matched to show and link to it; the full
 * record is still one follow-up call away (product/article/ticket detail). */
public record SearchResultItemDto(
    String type,
    Long id,
    String title,
    String snippet
) {
}
