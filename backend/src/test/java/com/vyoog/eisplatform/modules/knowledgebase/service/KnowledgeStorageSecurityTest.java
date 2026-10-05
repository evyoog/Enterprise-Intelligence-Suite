package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BR-MED-001 / prompt section 9.11: presigned URLs are short-lived, for one
 * object and one method, and never contain the secret key. Presigning works
 * offline, so this runs the real AWS SDK signer with fake keys.
 */
class KnowledgeStorageSecurityTest {

    private static final String FAKE_ACCESS = "AKIAFAKEACCESSKEY123";
    private static final String FAKE_SECRET = "fakeSecretKeyThatMustNeverLeak0123456789";

    private static S3MediaStorageService storage(String provider) {
        KnowledgeSettings settings = new KnowledgeSettings();
        ReflectionTestUtils.setField(settings, "storageProvider", provider);
        ReflectionTestUtils.setField(settings, "bucket", "eis-knowledge-test");
        ReflectionTestUtils.setField(settings, "region", "ap-south-1");
        ReflectionTestUtils.setField(settings, "endpoint", "");
        return new S3MediaStorageService(settings, FAKE_ACCESS, FAKE_SECRET);
    }

    @Test
    void presignedUrlsExpireAndNeverCarryTheSecret() {
        S3MediaStorageService s3 = storage("s3");
        var put = s3.generateUploadUrl("videos/tutorials/valam/inventory/abc.mp4", "video/mp4", Duration.ofMinutes(15));
        assertThat(put.url()).contains("X-Amz-Expires=900").contains("X-Amz-Signature=")
            .contains("videos/tutorials/valam/inventory/abc.mp4").doesNotContain(FAKE_SECRET);
        var get = s3.getTemporaryUrl("documents/guides/x/y/abc.pdf", "guide.pdf", Duration.ofMinutes(5));
        assertThat(get.url()).contains("X-Amz-Expires=300").contains("response-content-disposition")
            .doesNotContain(FAKE_SECRET);
        assertThat(get.expiresAt()).isBefore(java.time.Instant.now().plus(Duration.ofMinutes(6)));
    }

    @Test
    void withoutConfigurationNothingIsSignedOrReturned() {
        S3MediaStorageService none = storage("none");
        assertThat(none.configured()).isFalse();
        assertThatThrownBy(() -> none.generateUploadUrl("k", "video/mp4", Duration.ofMinutes(1)))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("STORAGE_NOT_CONFIGURED");
    }
}
