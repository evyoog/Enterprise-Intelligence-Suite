package com.vyoog.eisplatform.modules.search.repository;

/**
 * One matching index row.
 *
 * @param tier     1 exact ID, 2 exact phrase, 3 keyword, 4 partial word, 5 typo, 6 semantic
 * @param score    relevance inside its tier (higher is better)
 * @param passage  for semantic rows, the passage that matched
 */
public record SearchRow(
    long documentId,
    String sourceType,
    long sourceId,
    String reference,
    String title,
    String body,
    int tier,
    double score,
    String passage
) {
}
