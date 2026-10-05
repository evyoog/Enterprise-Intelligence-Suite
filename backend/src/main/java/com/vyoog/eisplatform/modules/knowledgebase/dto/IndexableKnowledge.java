package com.vyoog.eisplatform.modules.knowledgebase.dto;

import java.time.Instant;

/**
 * What the platform search index stores for one public knowledge item
 * (C76): the live version's title and text, plus search-only text
 * (type fields, transcript, chapters, taxonomy names) and keywords.
 */
public record IndexableKnowledge(Long id, String title, String body, String searchText, String keywords,
                                 Instant updatedAt) {
}
