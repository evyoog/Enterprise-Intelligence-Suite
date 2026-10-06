package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePageDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMedia;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeMediaRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Media library (REQ-KNW-003, BR-MED-001, BR-KMED-001–006). The upload flow:
 * the browser sends metadata only → permission, type, size, product and
 * module are checked → a presigned PUT for a new unique key is returned →
 * the browser uploads straight to S3 → completeUpload verifies the object
 * (HEAD: exists, size and type match) and only then marks the item READY.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class KnowledgeMediaService {

    private final KnowledgeMediaRepository mediaRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeVideoRepository videoRepository;
    private final KnowledgeProductRepository productRepository;
    private final KnowledgeModuleRepository moduleRepository;
    private final MediaStorageService storage;
    private final KnowledgeSettings settings;
    private final AuditService auditService;

    public KnowledgeMediaDtos.StorageStatus storageStatus() {
        return new KnowledgeMediaDtos.StorageStatus(storage.configured(), storage.provider(),
            settings.getVideoMaxSize(), settings.getDocumentMaxSize(), settings.getImageMaxSize(),
            settings.getAudioMaxSize(), settings.getMultipartThreshold(),
            List.copyOf(KnowledgeMediaRules.VIDEO.keySet()), List.copyOf(KnowledgeMediaRules.DOCUMENT.keySet()),
            List.copyOf(KnowledgeMediaRules.IMAGE.keySet()), List.copyOf(KnowledgeMediaRules.AUDIO.keySet()));
    }

    // ---- upload -------------------------------------------------------------

    @Transactional
    public KnowledgeMediaDtos.UploadTicket requestUpload(KnowledgeActor actor, KnowledgeMediaDtos.UploadRequest request) {
        Checked checked = check(request);
        KnowledgeMedia media = new KnowledgeMedia();
        media.setKind(request.kind());
        media.setStorageProvider(storage.provider());
        media.setS3Bucket(storage.bucket());
        media.setS3ObjectKey(checked.objectKey());
        media.setFileName(checked.fileName());
        media.setFileSize(request.size());
        media.setMimeType(checked.contentType());
        media.setProductId(request.productId());
        media.setModuleId(request.moduleId());
        media.setUploadedBySub(actor.keycloakSub());
        media.setStatus(KnowledgeMediaStatus.PENDING);
        KnowledgeMediaDtos.UploadTicket ticket = presign(media, checked.objectKey(), request.size(), checked.maxSize());
        mediaRepository.save(media);
        return new KnowledgeMediaDtos.UploadTicket(media.getId(), ticket.objectKey(), ticket.uploadUrl(),
            ticket.expiresAt(), ticket.multipart(), ticket.maxSize());
    }

    private KnowledgeMediaDtos.UploadTicket presign(KnowledgeMedia media, String objectKey, long size, long maxSize) {
        if (size > settings.getMultipartThreshold()) {
            MediaStorageService.MultipartUpload mp = storage.startMultipartUpload(objectKey, media.getMimeType(), size,
                Math.max(5L * 1024 * 1024, settings.getMultipartPartSize()), settings.getUploadUrlExpiry());
            media.setUploadId(mp.uploadId());
            return new KnowledgeMediaDtos.UploadTicket(media.getId(), objectKey, null, mp.expiresAt(),
                new KnowledgeMediaDtos.Multipart(mp.uploadId(), mp.partUrls(), mp.partSize()), maxSize);
        }
        MediaStorageService.PresignedUrl url = storage.generateUploadUrl(objectKey, media.getMimeType(),
            settings.getUploadUrlExpiry());
        return new KnowledgeMediaDtos.UploadTicket(media.getId(), objectKey, url.url(), url.expiresAt(), null, maxSize);
    }

    private record Checked(String fileName, String contentType, String objectKey, long maxSize) {
    }

    /** BR-KMED-002: allowed type, configured size, existing product and module. */
    private Checked check(KnowledgeMediaDtos.UploadRequest request) {
        if (!storage.configured()) {
            throw new KnowledgeException(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_NOT_CONFIGURED",
                "File storage is not configured yet. YouTube and external videos still work.");
        }
        if (request.kind() == null || request.fileName() == null || request.fileName().isBlank()
            || request.size() == null || request.contentType() == null) {
            throw KnowledgeException.badRequest("INVALID_FILE_TYPE", "File name, type, size and kind are required.");
        }
        String fileName = cleanFileName(request.fileName());
        String extension = KnowledgeMediaRules.extension(fileName);
        String contentType = KnowledgeMediaRules.baseContentType(request.contentType());
        Map<String, Set<String>> allowed = KnowledgeMediaRules.typesOf(request.kind());
        if (!allowed.containsKey(extension) || !allowed.get(extension).contains(contentType)) {
            throw KnowledgeException.badRequest("INVALID_FILE_TYPE", "This file type is not allowed here. Allowed: "
                + String.join(", ", allowed.keySet().stream().sorted().toList()) + ".");
        }
        long maxSize = KnowledgeMediaRules.maxSize(request.kind(), settings);
        if (request.size() <= 0) {
            throw KnowledgeException.badRequest("UPLOAD_MISMATCH", "The file is empty.");
        }
        if (request.size() > maxSize) {
            throw KnowledgeException.badRequest("FILE_TOO_LARGE", "The file is too large. The maximum is "
                + humanSize(maxSize) + ".");
        }
        String productSlug = "general";
        String moduleSlug = "general";
        if (request.productId() != null) {
            KnowledgeProduct product = productRepository.findById(request.productId()).orElseThrow(
                () -> KnowledgeException.badRequest("INVALID_CONTENT", "The product does not exist."));
            productSlug = product.getSlug();
        }
        if (request.moduleId() != null) {
            KnowledgeModule module = moduleRepository.findById(request.moduleId()).orElseThrow(
                () -> KnowledgeException.badRequest("INVALID_CONTENT", "The module does not exist."));
            if (!Objects.equals(module.getProductId(), request.productId())) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "The module does not belong to the product.");
            }
            moduleSlug = module.getSlug();
        }
        // BR-MED-001 rule 4: the key is generated here, never the file name.
        String objectKey = KnowledgeMediaRules.prefix(request.kind(), request.folder()) + "/" + productSlug + "/"
            + moduleSlug + "/" + UUID.randomUUID() + "." + extension;
        return new Checked(fileName, contentType, objectKey, maxSize);
    }

    @Transactional
    public KnowledgeMediaDtos.Media completeUpload(KnowledgeActor actor, Long mediaId, KnowledgeMediaDtos.CompleteRequest request) {
        KnowledgeMedia media = find(mediaId);
        boolean replacing = media.getPendingObjectKey() != null;
        if (!replacing && media.getStatus() != KnowledgeMediaStatus.PENDING) {
            throw new KnowledgeException(HttpStatus.CONFLICT, "DUPLICATE_UPLOAD", "This upload was already completed.");
        }
        String key = replacing ? media.getPendingObjectKey() : media.getS3ObjectKey();
        if (media.getUploadId() != null) {
            List<String> etags = request == null || request.partEtags() == null ? List.of() : request.partEtags();
            if (etags.isEmpty()) {
                throw KnowledgeException.badRequest("UPLOAD_MISMATCH", "The uploaded parts are missing.");
            }
            storage.completeMultipartUpload(key, media.getUploadId(), etags);
            media.setUploadId(null);
        }
        Optional<MediaStorageService.StoredObject> stored = storage.getMetadata(key);
        if (stored.isEmpty()) {
            fail(media, actor, "not found in storage");
            throw KnowledgeException.badRequest("UPLOAD_NOT_FOUND", "The upload was not found. Please upload the file again.");
        }
        MediaStorageService.StoredObject object = stored.get();
        String storedType = KnowledgeMediaRules.baseContentType(object.contentType());
        if (object.size() != media.getFileSize() || !storedType.equals(media.getMimeType())) {
            storage.delete(key);
            fail(media, actor, "size or type does not match (" + object.size() + ", " + storedType + ")");
            throw KnowledgeException.badRequest("UPLOAD_MISMATCH",
                "The uploaded file does not match what was declared. Please upload it again.");
        }
        if (replacing) {
            String oldKey = media.getS3ObjectKey();
            media.setS3ObjectKey(key);
            media.setPendingObjectKey(null);
            media.setMediaVersion(media.getMediaVersion() + 1);
            // Retention (REQ-KNW-003 open question 2, default applied): the old
            // object is deleted here; bucket versioning keeps the noncurrent
            // version for the lifecycle period (docs/09-integrations/aws-s3.md).
            safeDelete(oldKey);
            audit(actor, "KNOWLEDGE_MEDIA_REPLACED", media, "Replaced, now version " + media.getMediaVersion());
        } else {
            audit(actor, media.getKind() == KnowledgeMediaKind.VIDEO_FILE ? "KNOWLEDGE_VIDEO_UPLOADED" : "KNOWLEDGE_MEDIA_UPLOADED",
                media, "Uploaded " + media.getFileName());
        }
        media.setEtag(object.etag());
        media.setStatus(KnowledgeMediaStatus.READY);
        media.setUploadedAt(Instant.now());
        return toDto(mediaRepository.save(media), true);
    }

    private void fail(KnowledgeMedia media, KnowledgeActor actor, String reason) {
        if (media.getPendingObjectKey() != null) {
            media.setPendingObjectKey(null);
        } else {
            media.setStatus(KnowledgeMediaStatus.FAILED);
        }
        mediaRepository.save(media);
        auditService.recordSuccess(media.getKind() == KnowledgeMediaKind.VIDEO_FILE ? "KNOWLEDGE_VIDEO_UPLOAD_FAILED"
                : "KNOWLEDGE_MEDIA_UPLOAD_FAILED", actor.keycloakSub(), null, actor.email(), "KnowledgeMedia",
            String.valueOf(media.getId()), null, "Upload verification failed: " + reason);
    }

    @Transactional
    public void abort(KnowledgeActor actor, Long mediaId) {
        KnowledgeMedia media = find(mediaId);
        String key = media.getPendingObjectKey() != null ? media.getPendingObjectKey() : media.getS3ObjectKey();
        if (media.getPendingObjectKey() == null && media.getStatus() != KnowledgeMediaStatus.PENDING) {
            throw KnowledgeException.conflict("Only an upload in progress can be cancelled.");
        }
        if (media.getUploadId() != null) {
            storage.abortMultipartUpload(key, media.getUploadId());
            media.setUploadId(null);
        }
        safeDelete(key);
        if (media.getPendingObjectKey() != null) {
            media.setPendingObjectKey(null);
        } else {
            media.setStatus(KnowledgeMediaStatus.CANCELLED);
        }
        mediaRepository.save(media);
        audit(actor, "KNOWLEDGE_MEDIA_UPLOAD_CANCELLED", media, "Upload cancelled");
    }

    /** Publisher: upload a replacement; completeUpload switches to it (REQ-KNW-003.7). */
    @Transactional
    public KnowledgeMediaDtos.UploadTicket requestReplace(KnowledgeActor actor, Long mediaId,
                                                         KnowledgeMediaDtos.UploadRequest request) {
        KnowledgeMedia media = find(mediaId);
        if (media.getStatus() != KnowledgeMediaStatus.READY) {
            throw KnowledgeException.conflict("Only a stored file can be replaced.");
        }
        KnowledgeMediaDtos.UploadRequest same = new KnowledgeMediaDtos.UploadRequest(request.fileName(),
            request.contentType(), request.size(), media.getKind(), media.getProductId(), media.getModuleId(), request.folder());
        Checked checked = check(same);
        media.setPendingObjectKey(checked.objectKey());
        media.setFileName(checked.fileName());
        media.setFileSize(request.size());
        media.setMimeType(checked.contentType());
        KnowledgeMediaDtos.UploadTicket ticket = presign(media, checked.objectKey(), request.size(), checked.maxSize());
        mediaRepository.save(media);
        return ticket;
    }

    // ---- library ------------------------------------------------------------

    public KnowledgePageDto<KnowledgeMediaDtos.Media> list(KnowledgeMediaKind kind, String query, int page, int size,
                                                          boolean publisher) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Map<Long, List<KnowledgeMediaDtos.UsedBy>> usage = usage();
        List<KnowledgeMedia> all = mediaRepository.findByStatusInOrderByCreatedAtDesc(
            EnumSet.of(KnowledgeMediaStatus.READY, KnowledgeMediaStatus.PENDING));
        List<KnowledgeMedia> filtered = all.stream()
            .filter(m -> kind == null || m.getKind() == kind)
            .filter(m -> q.isEmpty() || m.getFileName().toLowerCase(Locale.ROOT).contains(q))
            .toList();
        int safeSize = Math.max(1, Math.min(size, 100));
        int from = Math.min(Math.max(page, 0) * safeSize, filtered.size());
        List<KnowledgeMediaDtos.Media> items = filtered.subList(from, Math.min(from + safeSize, filtered.size())).stream()
            .map(m -> toDto(m, publisher, usage.getOrDefault(m.getId(), List.of()))).toList();
        return new KnowledgePageDto<>(items, filtered.size(), Math.max(page, 0), safeSize);
    }

    public KnowledgeMediaDtos.Media get(Long mediaId, boolean publisher) {
        return toDto(find(mediaId), publisher);
    }

    /** Short-lived preview URL for staff (library thumbnails and previews). */
    public KnowledgeMediaDtos.TemporaryUrl previewUrl(Long mediaId) {
        KnowledgeMedia media = find(mediaId);
        requireReady(media);
        MediaStorageService.PresignedUrl url = storage.getTemporaryUrl(media.getS3ObjectKey(), null,
            settings.getDownloadUrlExpiry());
        return new KnowledgeMediaDtos.TemporaryUrl(url.url(), url.expiresAt(), media.getFileName(), media.getFileSize());
    }

    /** BR-KMED-005: deleting a file in use needs confirm=true; 409 IN_USE lists where it is used. */
    @Transactional
    public void delete(KnowledgeActor actor, Long mediaId, boolean confirm) {
        KnowledgeMedia media = find(mediaId);
        List<KnowledgeMediaDtos.UsedBy> usedBy = usage().getOrDefault(mediaId, List.of());
        if (!usedBy.isEmpty() && !confirm) {
            throw new KnowledgeException(HttpStatus.CONFLICT, "IN_USE",
                "This file is used by " + usedBy.size() + " content item(s). Confirm to delete it anyway.", usedBy);
        }
        if (media.getStatus() == KnowledgeMediaStatus.READY || media.getStatus() == KnowledgeMediaStatus.PENDING) {
            storage.delete(media.getS3ObjectKey());
        }
        media.setStatus(KnowledgeMediaStatus.DELETED);
        mediaRepository.save(media);
        audit(actor, media.getKind() == KnowledgeMediaKind.VIDEO_FILE ? "KNOWLEDGE_VIDEO_DELETED" : "KNOWLEDGE_MEDIA_DELETED",
            media, "Deleted " + media.getFileName());
    }

    /** Deletes a video's stored files (S3 video and uploaded thumbnail/subtitles). */
    @Transactional
    public void deleteVideoFiles(KnowledgeVideo video, KnowledgeActor actor) {
        List<Long> ids = new ArrayList<>();
        if (video.getMediaId() != null) {
            ids.add(video.getMediaId());
        }
        if (video.getThumbnailMediaId() != null) {
            ids.add(video.getThumbnailMediaId());
        }
        KnowledgeBlocks.parse(video.getSubtitles()).forEach(n -> ids.add(n.asLong()));
        for (Long id : ids) {
            mediaRepository.findById(id).ifPresent(media -> {
                if (media.getStatus() == KnowledgeMediaStatus.READY || media.getStatus() == KnowledgeMediaStatus.PENDING) {
                    safeDelete(media.getS3ObjectKey());
                }
                media.setStatus(KnowledgeMediaStatus.DELETED);
                mediaRepository.save(media);
                if (actor != null) {
                    audit(actor, media.getKind() == KnowledgeMediaKind.VIDEO_FILE ? "KNOWLEDGE_VIDEO_DELETED"
                        : "KNOWLEDGE_MEDIA_DELETED", media, "Deleted with its video");
                }
            });
        }
    }

    /** Short-lived GET for a reader who may see a content item using the file (REQ-KNW-003.8). */
    public KnowledgeMediaDtos.TemporaryUrl temporaryUrl(KnowledgeMedia media, boolean download, java.time.Duration expiry) {
        requireReady(media);
        MediaStorageService.PresignedUrl url = storage.getTemporaryUrl(media.getS3ObjectKey(),
            download ? media.getFileName() : null, expiry);
        return new KnowledgeMediaDtos.TemporaryUrl(url.url(), url.expiresAt(), media.getFileName(), media.getFileSize());
    }

    /** Content ids (all states) that use a file, for reader authorization. */
    public List<Long> contentUsing(Long mediaId) {
        return usage().getOrDefault(mediaId, List.of()).stream().map(KnowledgeMediaDtos.UsedBy::contentId).toList();
    }

    /** Removes uploads never completed after the configured time (REQ-KNW-003.9). */
    @Transactional
    public int cleanupOrphans(Instant now) {
        int removed = 0;
        for (KnowledgeMedia media : mediaRepository.findByStatusAndCreatedAtBefore(KnowledgeMediaStatus.PENDING,
                now.minus(settings.getOrphanAfter()))) {
            if (media.getUploadId() != null) {
                storage.abortMultipartUpload(media.getS3ObjectKey(), media.getUploadId());
                media.setUploadId(null);
            }
            safeDelete(media.getS3ObjectKey());
            media.setStatus(KnowledgeMediaStatus.FAILED);
            mediaRepository.save(media);
            auditService.recordSuccess("KNOWLEDGE_MEDIA_ORPHAN_REMOVED", null, null, null, "KnowledgeMedia",
                String.valueOf(media.getId()), null, "Upload never completed; object removed");
            removed++;
        }
        return removed;
    }

    public KnowledgeMedia find(Long mediaId) {
        return mediaRepository.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    // ---- helpers ------------------------------------------------------------

    /** media id → content items using it (blocks, video files, thumbnails, subtitles). */
    Map<Long, List<KnowledgeMediaDtos.UsedBy>> usage() {
        Map<Long, List<KnowledgeMediaDtos.UsedBy>> usage = new HashMap<>();
        Map<Long, KnowledgeArticle> articles = articleRepository.findAll().stream()
            .collect(Collectors.toMap(KnowledgeArticle::getId, a -> a));
        for (KnowledgeArticle a : articles.values()) {
            for (Long id : KnowledgeBlocks.mediaIds(a.getBlocks())) {
                add(usage, id, a);
            }
        }
        for (KnowledgeVideo v : videoRepository.findAll()) {
            KnowledgeArticle a = articles.get(v.getContentId());
            if (a == null) {
                continue;
            }
            if (v.getMediaId() != null) {
                add(usage, v.getMediaId(), a);
            }
            if (v.getThumbnailMediaId() != null) {
                add(usage, v.getThumbnailMediaId(), a);
            }
            KnowledgeBlocks.parse(v.getSubtitles()).forEach(n -> add(usage, n.asLong(), a));
        }
        return usage;
    }

    private static void add(Map<Long, List<KnowledgeMediaDtos.UsedBy>> usage, Long mediaId, KnowledgeArticle a) {
        List<KnowledgeMediaDtos.UsedBy> list = usage.computeIfAbsent(mediaId, k -> new ArrayList<>());
        if (list.stream().noneMatch(u -> u.contentId().equals(a.getId()))) {
            list.add(new KnowledgeMediaDtos.UsedBy(a.getId(), a.getTitle(), a.getContentType().name()));
        }
    }

    private KnowledgeMediaDtos.Media toDto(KnowledgeMedia m, boolean publisher) {
        return toDto(m, publisher, usage().getOrDefault(m.getId(), List.of()));
    }

    private KnowledgeMediaDtos.Media toDto(KnowledgeMedia m, boolean publisher, List<KnowledgeMediaDtos.UsedBy> usedBy) {
        KnowledgeMediaDtos.StorageInfo info = publisher ? new KnowledgeMediaDtos.StorageInfo(m.getStorageProvider(),
            m.getS3Bucket(), storage.region(), m.getS3ObjectKey(), m.getFileSize(), m.getMimeType(), m.getStatus()) : null;
        return new KnowledgeMediaDtos.Media(m.getId(), m.getKind(), m.getFileName(), m.getMimeType(), m.getFileSize(),
            m.getStatus(), m.getMediaVersion(), m.getProductId(), m.getModuleId(), m.getCreatedAt(), m.getUploadedAt(),
            usedBy, info);
    }

    private void requireReady(KnowledgeMedia media) {
        if (media.getStatus() != KnowledgeMediaStatus.READY) {
            throw new ResourceNotFoundException("File not found");
        }
    }

    private void safeDelete(String key) {
        try {
            storage.delete(key);
        } catch (RuntimeException e) {
            log.warn("Could not delete storage object (will be retried by cleanup): {}", e.getMessage());
        }
    }

    private void audit(KnowledgeActor actor, String action, KnowledgeMedia media, String detail) {
        auditService.recordSuccess(action, actor.keycloakSub(), null, actor.email(), "KnowledgeMedia",
            String.valueOf(media.getId()), null, detail);
    }

    static String cleanFileName(String name) {
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "").trim();
        return base.length() > 255 ? base.substring(base.length() - 255) : base;
    }

    static String humanSize(long bytes) {
        if (bytes >= 1024L * 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
        if (bytes >= 1024L * 1024) {
            return (bytes / (1024 * 1024)) + " MB";
        }
        return (bytes / 1024) + " KB";
    }
}
