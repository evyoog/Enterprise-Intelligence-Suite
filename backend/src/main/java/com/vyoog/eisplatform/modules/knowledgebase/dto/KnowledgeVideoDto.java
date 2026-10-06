package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;

import java.util.List;

/**
 * Video details. Never contains a storage URL: hosted (AWS_S3) videos are
 * played through GET /knowledge/videos/{id}/play-url (BR-KVID-003).
 *
 * @param thumbnailUrl provider thumbnail (YouTube/external) or a short-lived
 *                     presigned URL for an uploaded thumbnail
 */
public record KnowledgeVideoDto(
    VideoSourceType sourceType,
    String videoUrl,
    String youtubeId,
    Long mediaId,
    Long thumbnailMediaId,
    String thumbnailUrl,
    Integer durationSeconds,
    String channel,
    String transcript,
    JsonNode chapters,
    List<String> subtitleLanguages
) {
}
