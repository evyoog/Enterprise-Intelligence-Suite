package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.repository.SearchRow;

import java.util.List;
import java.util.Set;

/**
 * @param rows            matches, best first
 * @param didYouMean      the corrected query when a typo correction was used, else null
 * @param highlightTerms  words to highlight (query words, synonyms, corrections)
 */
record KeywordSearchOutcome(List<SearchRow> rows, String didYouMean, Set<String> highlightTerms) {
}
