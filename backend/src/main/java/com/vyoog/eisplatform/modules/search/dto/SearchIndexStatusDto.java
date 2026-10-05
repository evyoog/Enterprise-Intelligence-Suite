package com.vyoog.eisplatform.modules.search.dto;

import java.util.Map;

/**
 * Admin view of the search index (C70).
 *
 * @param engine               POSTGRES or BASIC
 * @param keywordIndexInstalled the search_document table with its full-text column exists
 * @param semanticInstalled    pgvector and the search_chunk table exist
 * @param documents            indexed records per type
 * @param chunks / embeddedChunks / pendingChunks documentation passages for semantic search
 * @param embedding            the embedding model's state as reported by ai-service
 * @param settings             the search settings in use (thresholds, chunk size)
 */
public record SearchIndexStatusDto(
    String engine,
    boolean keywordIndexInstalled,
    boolean semanticInstalled,
    Map<String, Long> documents,
    long chunks,
    long embeddedChunks,
    long pendingChunks,
    EmbeddingStatusDto embedding,
    SearchIndexRunDto lastRun,
    Map<String, Object> settings
) {
}
