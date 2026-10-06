package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** Video management payloads (REQ-KNW-004). */
public final class KnowledgeVideoDtos {

    private KnowledgeVideoDtos() {
    }

    /**
     * Create or update a video: the content metadata plus the source.
     * YOUTUBE needs youtubeUrl; EXTERNAL_URL needs url (https); AWS_S3 needs
     * mediaId of a completed VIDEO_FILE upload (BR-KVID-001).
     */
    public record VideoRequest(@NotNull @Valid KnowledgeContentRequest content, @NotNull VideoSourceType sourceType,
                               String youtubeUrl, String url, Long mediaId, Integer durationSeconds, String channel,
                               String thumbnailUrl, Long thumbnailMediaId, String transcript, String chapters) {
    }

    public record YouTubeFetchRequest(String url) {
    }

    /** source: DATA_API (key configured) or OEMBED (no key). */
    public record YouTubeDetails(String videoId, String title, String description, Integer durationSeconds,
                                 String channel, String thumbnailUrl, String source) {
    }

    public record TextTrackRequest(String text) {
    }

    public record SubtitleRequest(Long mediaId) {
    }

    public record Summary(long total, long published, long draft, long inReview, long views) {
    }

    /**
     * How to play a video. YOUTUBE: embedId (privacy-enhanced embed);
     * AWS_S3: a short-lived presigned url; EXTERNAL_URL: the url.
     */
    public record PlayInfo(VideoSourceType sourceType, String embedId, String url, Instant expiresAt,
                           java.util.Map<String, String> subtitles) {
    }
}
