package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;

import java.time.Instant;

/** A published version snapshot (BR-KCON-004). */
public record KnowledgeVersionDto(
    Long id,
    String versionLabel,
    String title,
    String shortDescription,
    JsonNode blocks,
    JsonNode typeFields,
    KnowledgeAudience audience,
    boolean live,
    String publishedBySub,
    Instant publishedAt
) {
}
