package com.vyoog.eisplatform.modules.knowledgebase.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Knowledge Center settings from {@code eis.knowledge.*} in application.yml,
 * each overridable by an environment variable (C72, docs/09-integrations/aws-s3.md).
 * Sizes and expiries are the defaults applied on 2026-10-05 (REQ-KNW-003 open
 * question 1) until the product owner sets others. Access keys are not
 * settings: they come only from config/secrets.env (BR-SEC-001).
 */
@Component
@Getter
public class KnowledgeSettings {

    /** {@code s3} or {@code none} (uploads and hosted playback switched off). */
    @Value("${eis.knowledge.storage.provider:none}")
    private String storageProvider;

    @Value("${eis.knowledge.storage.bucket:}")
    private String bucket;

    @Value("${eis.knowledge.storage.region:}")
    private String region;

    /** Optional S3-compatible endpoint (for example MinIO in development). */
    @Value("${eis.knowledge.storage.endpoint:}")
    private String endpoint;

    @Value("${eis.knowledge.video.max-size:5368709120}")
    private long videoMaxSize;

    @Value("${eis.knowledge.document.max-size:104857600}")
    private long documentMaxSize;

    @Value("${eis.knowledge.image.max-size:10485760}")
    private long imageMaxSize;

    @Value("${eis.knowledge.audio.max-size:104857600}")
    private long audioMaxSize;

    @Value("${eis.knowledge.multipart-threshold:104857600}")
    private long multipartThreshold;

    /** Part size for multipart uploads (S3 minimum 5 MB). */
    @Value("${eis.knowledge.multipart-part-size:104857600}")
    private long multipartPartSize;

    @Value("${eis.knowledge.upload-url-expiry:PT15M}")
    private Duration uploadUrlExpiry;

    @Value("${eis.knowledge.download-url-expiry:PT5M}")
    private Duration downloadUrlExpiry;

    @Value("${eis.knowledge.playback-url-expiry:PT1H}")
    private Duration playbackUrlExpiry;

    /** Uploads never completed after this long are removed by the cleanup job. */
    @Value("${eis.knowledge.orphan-after:PT24H}")
    private Duration orphanAfter;

    /** Seed the taxonomy, glossary and workflow guides on startup (C74). */
    @Value("${eis.knowledge.seed.enabled:true}")
    private boolean seedEnabled;

    /** Optional YouTube Data API key (config/secrets.env); empty = oEmbed only. */
    @Value("${eis.knowledge.youtube.api-key:}")
    private String youtubeApiKey;

    @Value("${eis.knowledge.youtube.timeout-ms:3000}")
    private int youtubeTimeoutMs;

    public boolean storageConfigured() {
        return "s3".equalsIgnoreCase(storageProvider) && bucket != null && !bucket.isBlank()
            && region != null && !region.isBlank();
    }
}
