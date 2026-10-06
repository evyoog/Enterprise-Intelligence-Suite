package com.vyoog.eisplatform.modules.knowledgebase.support;

import com.vyoog.eisplatform.modules.knowledgebase.service.MediaStorageService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A configured, in-memory stand-in for S3 in tests. "Uploading" is
 * simulated with {@link #put}; URLs are fake but carry the expiry so tests
 * can check it.
 */
public class InMemoryMediaStorage implements MediaStorageService {

    public final Map<String, StoredObject> objects = new ConcurrentHashMap<>();
    public final List<String> deleted = new ArrayList<>();
    public final List<Duration> issuedExpiries = new ArrayList<>();

    public void put(String key, long size, String contentType) {
        objects.put(key, new StoredObject(size, contentType, "\"etag-" + key.hashCode() + "\""));
    }

    @Override
    public boolean configured() {
        return true;
    }

    @Override
    public String provider() {
        return "AWS_S3";
    }

    @Override
    public String bucket() {
        return "eis-knowledge-test";
    }

    @Override
    public String region() {
        return "ap-south-1";
    }

    @Override
    public PresignedUrl generateUploadUrl(String objectKey, String contentType, Duration expiry) {
        issuedExpiries.add(expiry);
        return new PresignedUrl("https://s3.test/" + objectKey + "?X-Amz-Expires=" + expiry.toSeconds() + "&method=PUT",
            Instant.now().plus(expiry));
    }

    @Override
    public MultipartUpload startMultipartUpload(String objectKey, String contentType, long size, long partSize,
                                                Duration expiry) {
        int parts = (int) Math.max(1, (size + partSize - 1) / partSize);
        List<String> urls = new ArrayList<>();
        for (int i = 1; i <= parts; i++) {
            urls.add("https://s3.test/" + objectKey + "?partNumber=" + i);
        }
        return new MultipartUpload("upload-" + objectKey.hashCode(), urls, partSize, Instant.now().plus(expiry));
    }

    @Override
    public void completeMultipartUpload(String objectKey, String uploadId, List<String> partEtags) {
        // the test puts the object itself
    }

    @Override
    public void abortMultipartUpload(String objectKey, String uploadId) {
        objects.remove(objectKey);
    }

    @Override
    public Optional<StoredObject> getMetadata(String objectKey) {
        return Optional.ofNullable(objects.get(objectKey));
    }

    @Override
    public PresignedUrl getTemporaryUrl(String objectKey, String downloadName, Duration expiry) {
        issuedExpiries.add(expiry);
        return new PresignedUrl("https://s3.test/" + objectKey + "?X-Amz-Expires=" + expiry.toSeconds() + "&method=GET",
            Instant.now().plus(expiry));
    }

    @Override
    public void delete(String objectKey) {
        objects.remove(objectKey);
        deleted.add(objectKey);
    }
}
