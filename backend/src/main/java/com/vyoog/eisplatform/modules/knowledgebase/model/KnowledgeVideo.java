package com.vyoog.eisplatform.modules.knowledgebase.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Video details of a VIDEO content item (REQ-KNW-004). Title, description,
 * taxonomy, audience and workflow live on the content item itself
 * ({@link KnowledgeArticle}); this row holds the source and text tracks.
 * Required fields depend on the source (BR-KVID-001).
 */
@Entity
@Table(name = "knowledge_video")
@Getter
@Setter
public class KnowledgeVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false, unique = true)
    private Long contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_source_type", nullable = false, length = 20)
    private VideoSourceType sourceType;

    /** EXTERNAL_URL: the https URL; YOUTUBE: the original URL. */
    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    /** YOUTUBE: the 11-character video id. */
    @Column(name = "video_id", length = 20)
    private String videoId;

    /** AWS_S3: the video file in the media library. */
    @Column(name = "media_id")
    private Long mediaId;

    /** Uploaded thumbnail (media library). */
    @Column(name = "thumbnail_media_id")
    private Long thumbnailMediaId;

    /** YouTube/external thumbnail (a public image URL from the provider). */
    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(length = 200)
    private String channel;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    /** JSON [{ "seconds": 0, "title": "Introduction" }]. */
    @Column(columnDefinition = "TEXT")
    private String chapters;

    /** JSON { "en": mediaId, "es": mediaId } (VTT files). */
    @Column(columnDefinition = "TEXT")
    private String subtitles;
}
