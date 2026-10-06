package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaStatus;

import java.time.Instant;
import java.util.List;

/** Media library payloads (REQ-KNW-003). No credentials or permanent URLs, ever. */
public final class KnowledgeMediaDtos {

    private KnowledgeMediaDtos() {
    }

    /** Metadata the browser sends before uploading (step 2 of the upload flow). */
    public record UploadRequest(String fileName, String contentType, Long size, KnowledgeMediaKind kind,
                                Long productId, Long moduleId, String folder) {
    }

    /** A presigned upload: one PUT URL, or one URL per part for multipart. */
    public record UploadTicket(Long mediaId, String objectKey, String uploadUrl, Instant expiresAt,
                               Multipart multipart, long maxSize) {
    }

    public record Multipart(String uploadId, List<String> partUrls, long partSize) {
    }

    /** Completion report; part ETags only for multipart uploads. */
    public record CompleteRequest(List<String> partEtags) {
    }

    public record UsedBy(Long contentId, String title, String contentType) {
    }

    /** Publishers only: where the file is stored — never credentials (REQ-KNW-003.10). */
    public record StorageInfo(String provider, String bucket, String region, String objectKey, long size,
                              String format, KnowledgeMediaStatus status) {
    }

    public record Media(Long id, KnowledgeMediaKind kind, String fileName, String mimeType, long size,
                        KnowledgeMediaStatus status, int version, Long productId, Long moduleId,
                        Instant createdAt, Instant uploadedAt, List<UsedBy> usedBy, StorageInfo storage) {
    }

    public record TemporaryUrl(String url, Instant expiresAt, String fileName, Long size) {
    }

    public record StorageStatus(boolean configured, String provider, long videoMaxSize, long documentMaxSize,
                                long imageMaxSize, long audioMaxSize, long multipartThreshold,
                                List<String> videoTypes, List<String> documentTypes, List<String> imageTypes,
                                List<String> audioTypes) {
    }
}
