package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * S3 implementation of {@link MediaStorageService} (C72). Credentials: the
 * AWS default chain (an IAM role on ECS) unless both
 * EIS_KNOWLEDGE_AWS_ACCESS_KEY_ID and EIS_KNOWLEDGE_AWS_SECRET_ACCESS_KEY are
 * set in config/secrets.env. They are used only to sign requests here and
 * are never logged or returned. AWS error details are logged server-side;
 * callers get STORAGE_UNAVAILABLE (REQ-KNW-003.11).
 */
@Service
@Slf4j
public class S3MediaStorageService implements MediaStorageService {

    private final KnowledgeSettings settings;
    private final String accessKeyId;
    private final String secretAccessKey;
    private volatile S3Client client;
    private volatile S3Presigner presigner;

    public S3MediaStorageService(KnowledgeSettings settings,
                                 @Value("${EIS_KNOWLEDGE_AWS_ACCESS_KEY_ID:}") String accessKeyId,
                                 @Value("${EIS_KNOWLEDGE_AWS_SECRET_ACCESS_KEY:}") String secretAccessKey) {
        this.settings = settings;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    @Override
    public boolean configured() {
        return settings.storageConfigured();
    }

    @Override
    public String provider() {
        return configured() ? "AWS_S3" : "NONE";
    }

    @Override
    public String bucket() {
        return settings.getBucket();
    }

    @Override
    public String region() {
        return settings.getRegion();
    }

    @Override
    public PresignedUrl generateUploadUrl(String objectKey, String contentType, Duration expiry) {
        try {
            var presigned = presigner().presignPutObject(b -> b.signatureDuration(expiry)
                .putObjectRequest(p -> p.bucket(bucket()).key(objectKey).contentType(contentType)));
            return new PresignedUrl(presigned.url().toString(), presigned.expiration());
        } catch (SdkException e) {
            throw unavailable("create an upload URL", e);
        }
    }

    @Override
    public MultipartUpload startMultipartUpload(String objectKey, String contentType, long size, long partSize,
                                                Duration expiry) {
        try {
            String uploadId = client().createMultipartUpload(b -> b.bucket(bucket()).key(objectKey)
                .contentType(contentType)).uploadId();
            int parts = (int) Math.max(1, (size + partSize - 1) / partSize);
            List<String> urls = new ArrayList<>(parts);
            Instant expiresAt = null;
            for (int part = 1; part <= parts; part++) {
                final int number = part;
                var presigned = presigner().presignUploadPart(b -> b.signatureDuration(expiry)
                    .uploadPartRequest(p -> p.bucket(bucket()).key(objectKey).uploadId(uploadId).partNumber(number)));
                urls.add(presigned.url().toString());
                expiresAt = presigned.expiration();
            }
            return new MultipartUpload(uploadId, urls, partSize, expiresAt);
        } catch (SdkException e) {
            throw unavailable("start a multipart upload", e);
        }
    }

    @Override
    public void completeMultipartUpload(String objectKey, String uploadId, List<String> partEtags) {
        List<CompletedPart> parts = new ArrayList<>();
        for (int i = 0; i < partEtags.size(); i++) {
            parts.add(CompletedPart.builder().partNumber(i + 1).eTag(partEtags.get(i)).build());
        }
        try {
            client().completeMultipartUpload(b -> b.bucket(bucket()).key(objectKey).uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build()));
        } catch (SdkException e) {
            throw unavailable("complete a multipart upload", e);
        }
    }

    @Override
    public void abortMultipartUpload(String objectKey, String uploadId) {
        try {
            client().abortMultipartUpload(b -> b.bucket(bucket()).key(objectKey).uploadId(uploadId));
        } catch (SdkException e) {
            log.warn("Could not abort multipart upload {}: {}", objectKey, e.getMessage());
        }
    }

    @Override
    public Optional<StoredObject> getMetadata(String objectKey) {
        try {
            HeadObjectResponse head = client().headObject(b -> b.bucket(bucket()).key(objectKey));
            return Optional.of(new StoredObject(head.contentLength(), head.contentType(), head.eTag()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw unavailable("check an uploaded file", e);
        } catch (SdkException e) {
            throw unavailable("check an uploaded file", e);
        }
    }

    @Override
    public PresignedUrl getTemporaryUrl(String objectKey, String downloadName, Duration expiry) {
        try {
            var presigned = presigner().presignGetObject(b -> b.signatureDuration(expiry)
                .getObjectRequest(g -> {
                    g.bucket(bucket()).key(objectKey);
                    if (downloadName != null) {
                        g.responseContentDisposition("attachment; filename=\"" + downloadName.replace("\"", "") + "\"");
                    }
                }));
            return new PresignedUrl(presigned.url().toString(), presigned.expiration());
        } catch (SdkException e) {
            throw unavailable("create a download URL", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            client().deleteObject(b -> b.bucket(bucket()).key(objectKey));
        } catch (SdkException e) {
            throw unavailable("delete a file", e);
        }
    }

    private S3Client client() {
        requireConfigured();
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    var builder = S3Client.builder().region(Region.of(region())).credentialsProvider(credentials());
                    if (!settings.getEndpoint().isBlank()) {
                        builder.endpointOverride(URI.create(settings.getEndpoint()))
                            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
                    }
                    client = builder.build();
                }
            }
        }
        return client;
    }

    private S3Presigner presigner() {
        requireConfigured();
        if (presigner == null) {
            synchronized (this) {
                if (presigner == null) {
                    var builder = S3Presigner.builder().region(Region.of(region())).credentialsProvider(credentials());
                    if (!settings.getEndpoint().isBlank()) {
                        builder.endpointOverride(URI.create(settings.getEndpoint()))
                            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
                    }
                    presigner = builder.build();
                }
            }
        }
        return presigner;
    }

    private AwsCredentialsProvider credentials() {
        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        }
        return DefaultCredentialsProvider.create();
    }

    private void requireConfigured() {
        if (!configured()) {
            throw new KnowledgeException(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_NOT_CONFIGURED",
                "File storage is not configured yet. YouTube and external videos still work.");
        }
    }

    private KnowledgeException unavailable(String action, Exception e) {
        log.error("S3 failed to {}: {}", action, e.getMessage());
        return new KnowledgeException(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_UNAVAILABLE",
            "File storage is not available right now. Please try again later.");
    }

    @PreDestroy
    void close() {
        if (client != null) {
            client.close();
        }
        if (presigner != null) {
            presigner.close();
        }
    }
}
