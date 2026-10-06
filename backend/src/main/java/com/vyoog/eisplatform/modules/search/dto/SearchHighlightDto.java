package com.vyoog.eisplatform.modules.search.dto;

/** A highlighted word in a result's title or snippet: character offset and length. */
public record SearchHighlightDto(int start, int length) {
}
