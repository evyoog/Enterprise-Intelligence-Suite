package com.vyoog.eisplatform.modules.knowledgebase.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Storage for knowledge files (REQ-KNW-003.1, BR-MED-001). Implementation:
 * {@link S3MediaStorageService} (private bucket, presigned URLs). Files go
 * browser ↔ storage directly; this interface only issues short-lived URLs
 * and checks, replaces and deletes objects. Nothing here ever returns a
 * credential or a permanent URL.
 */
public interface MediaStorageService {

    boolean configured();

    String provider();

    String bucket();

    String region();

    /** Presigned PUT for one new object (single request upload). */
    PresignedUrl generateUploadUrl(String objectKey, String contentType, Duration expiry);

    /** Starts a multipart upload and presigns every part. */
    MultipartUpload startMultipartUpload(String objectKey, String contentType, long size, long partSize, Duration expiry);

    void completeMultipartUpload(String objectKey, String uploadId, List<String> partEtags);

    void abortMultipartUpload(String objectKey, String uploadId);

    /** HEAD: the stored object's size and type, empty if it does not exist. */
    Optional<StoredObject> getMetadata(String objectKey);

    /** Presigned GET; {@code downloadName} sets Content-Disposition: attachment. */
    PresignedUrl getTemporaryUrl(String objectKey, String downloadName, Duration expiry);

    void delete(String objectKey);

    record PresignedUrl(String url, Instant expiresAt) {
    }

    record MultipartUpload(String uploadId, List<String> partUrls, long partSize, Instant expiresAt) {
    }

    record StoredObject(long size, String contentType, String etag) {
    }
}
