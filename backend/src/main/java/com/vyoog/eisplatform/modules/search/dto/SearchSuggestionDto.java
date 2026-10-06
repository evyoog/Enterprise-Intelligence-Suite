package com.vyoog.eisplatform.modules.search.dto;

/** A type-ahead suggestion: a record whose title matches what was typed so far. */
public record SearchSuggestionDto(String type, Long id, String title, String reference) {
}
