package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePublishRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeCategoryRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import com.vyoog.eisplatform.modules.knowledgebase.support.InMemoryMediaStorage;
import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.ANONYMOUS;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.CONTRIBUTOR;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.PUBLISHER;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.json;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.member;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-KNW-003 media library and REQ-KNW-004 videos, with S3 replaced by an in-memory store. */
@SpringBootTest
@ActiveProfiles("test")
@Import(KnowledgeTestConfig.class)
class KnowledgeMediaAndVideoServiceTest {

    @Autowired
    private KnowledgeMediaService mediaService;
    @Autowired
    private KnowledgeVideoService videoService;
    @Autowired
    private KnowledgeContentService contentService;
    @Autowired
    private KnowledgeReaderService readerService;
    @Autowired
    private InMemoryMediaStorage storage;
    @Autowired
    private KnowledgeSettings settings;
    @Autowired
    private KnowledgeProductRepository productRepository;
    @Autowired
    private KnowledgeModuleRepository moduleRepository;
    @Autowired
    private KnowledgeCategoryRepository categoryRepository;

    private Long productId;
    private Long moduleId;
    private Long categoryId;

    @BeforeEach
    void taxonomy() {
        var product = productRepository.findBySlug("valam").orElseThrow();
        productId = product.getId();
        moduleId = moduleRepository.findByProductIdOrderByDisplayOrderAscNameAsc(productId).stream()
            .filter(m -> m.getName().equals("Inventory")).findFirst().orElseThrow().getId();
        categoryId = categoryRepository.findAll().get(0).getId();
    }

    private KnowledgeMediaDtos.Media upload(String name, String type, long size, KnowledgeMediaKind kind) {
        KnowledgeMediaDtos.UploadTicket ticket = mediaService.requestUpload(CONTRIBUTOR,
            new KnowledgeMediaDtos.UploadRequest(name, type, size, kind, productId, moduleId, null));
        storage.put(ticket.objectKey(), size, type);
        return mediaService.completeUpload(CONTRIBUTOR, ticket.mediaId(), null);
    }

    @Test
    void uploadUrlUsesAGeneratedKeyAndShortExpiryAndChecksTypeAndSize() {
        KnowledgeMediaDtos.UploadTicket ticket = mediaService.requestUpload(CONTRIBUTOR,
            new KnowledgeMediaDtos.UploadRequest("../../Q3 manual.pdf", "application/pdf", 2048L, KnowledgeMediaKind.DOCUMENT,
                productId, moduleId, "manuals"));
        assertThat(ticket.objectKey()).startsWith("documents/manuals/valam/inventory/").endsWith(".pdf")
            .doesNotContain("Q3").doesNotContain("..");
        assertThat(ticket.uploadUrl()).contains("X-Amz-Expires=" + settings.getUploadUrlExpiry().toSeconds());
        assertThat(settings.getUploadUrlExpiry()).isLessThanOrEqualTo(Duration.ofMinutes(15));

        assertThatThrownBy(() -> mediaService.requestUpload(CONTRIBUTOR, new KnowledgeMediaDtos.UploadRequest(
                "run.exe", "application/octet-stream", 10L, KnowledgeMediaKind.DOCUMENT, productId, moduleId, null)))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("INVALID_FILE_TYPE");
        assertThatThrownBy(() -> mediaService.requestUpload(CONTRIBUTOR, new KnowledgeMediaDtos.UploadRequest(
                "logo.svg", "image/svg+xml", 10L, KnowledgeMediaKind.IMAGE, productId, moduleId, null)))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("INVALID_FILE_TYPE");
        assertThatThrownBy(() -> mediaService.requestUpload(CONTRIBUTOR, new KnowledgeMediaDtos.UploadRequest(
                "huge.mp4", "video/mp4", settings.getVideoMaxSize() + 1, KnowledgeMediaKind.VIDEO_FILE, productId, moduleId, null)))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("FILE_TOO_LARGE");
        Long otherModule = moduleRepository.findAll().stream().filter(m -> !m.getProductId().equals(productId))
            .findFirst().orElseThrow().getId();
        assertThatThrownBy(() -> mediaService.requestUpload(CONTRIBUTOR, new KnowledgeMediaDtos.UploadRequest(
                "a.pdf", "application/pdf", 10L, KnowledgeMediaKind.DOCUMENT, productId, otherModule, null)))
            .isInstanceOf(KnowledgeException.class);
    }

    @Test
    void completeUploadVerifiesTheStoredObject() {
        KnowledgeMediaDtos.UploadTicket missing = mediaService.requestUpload(CONTRIBUTOR,
            new KnowledgeMediaDtos.UploadRequest("a.png", "image/png", 100L, KnowledgeMediaKind.IMAGE, productId, moduleId, null));
        assertThatThrownBy(() -> mediaService.completeUpload(CONTRIBUTOR, missing.mediaId(), null))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("UPLOAD_NOT_FOUND");

        KnowledgeMediaDtos.UploadTicket wrong = mediaService.requestUpload(CONTRIBUTOR,
            new KnowledgeMediaDtos.UploadRequest("b.png", "image/png", 100L, KnowledgeMediaKind.IMAGE, productId, moduleId, null));
        storage.put(wrong.objectKey(), 999L, "image/png");
        assertThatThrownBy(() -> mediaService.completeUpload(CONTRIBUTOR, wrong.mediaId(), null))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("UPLOAD_MISMATCH");
        assertThat(storage.objects).doesNotContainKey(wrong.objectKey());

        KnowledgeMediaDtos.Media ok = upload("c.png", "image/png", 100L, KnowledgeMediaKind.IMAGE);
        assertThat(ok.status()).isEqualTo(KnowledgeMediaStatus.READY);
        assertThatThrownBy(() -> mediaService.completeUpload(CONTRIBUTOR, ok.id(), null))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("DUPLICATE_UPLOAD");
    }

    @Test
    void largeFilesUseMultipartAndCancelRemovesTheUpload() {
        long size = settings.getMultipartThreshold() + 1;
        KnowledgeMediaDtos.UploadTicket ticket = mediaService.requestUpload(CONTRIBUTOR,
            new KnowledgeMediaDtos.UploadRequest("webinar.mp4", "video/mp4", size, KnowledgeMediaKind.VIDEO_FILE,
                productId, moduleId, "webinars"));
        assertThat(ticket.uploadUrl()).isNull();
        assertThat(ticket.multipart().partUrls()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(ticket.objectKey()).startsWith("videos/webinars/");
        mediaService.abort(CONTRIBUTOR, ticket.mediaId());
        assertThat(mediaService.get(ticket.mediaId(), true).status()).isEqualTo(KnowledgeMediaStatus.CANCELLED);
    }

    @Test
    void downloadIsOnlyForReadersWhoMaySeeContentUsingTheFile() {
        KnowledgeMediaDtos.Media pdf = upload("guide.pdf", "application/pdf", 4096L, KnowledgeMediaKind.DOCUMENT);
        String blocks = "[{\"type\":\"pdf\",\"mediaId\":" + pdf.id() + ",\"label\":\"Guide\"}]";
        KnowledgeContentRequest r = new KnowledgeContentRequest(KnowledgeContentType.DOCUMENT, "Inventory guide", null, null,
            productId, moduleId, null, null, null, null, KnowledgeAudience.ORGANIZATION, List.of(701L), null, null, null,
            null, null, null, null, null, null, null, null, json(blocks), null);
        KnowledgeContentDto doc = contentService.create(CONTRIBUTOR, r);
        // Not published yet: nobody may download.
        assertThatThrownBy(() -> readerService.downloadUrl(member(1, 701), pdf.id(), "x"))
            .isInstanceOf(ResourceNotFoundException.class);
        contentService.submit(CONTRIBUTOR, doc.id());
        contentService.approve(PUBLISHER, doc.id());
        contentService.publish(PUBLISHER, doc.id(), new KnowledgePublishRequest(null, null));

        KnowledgeMediaDtos.TemporaryUrl url = readerService.downloadUrl(member(1, 701), pdf.id(), "x");
        assertThat(url.url()).contains("X-Amz-Expires=" + settings.getDownloadUrlExpiry().toSeconds());
        assertThat(url.fileName()).isEqualTo("guide.pdf");
        assertThatThrownBy(() -> readerService.downloadUrl(member(2, 702), pdf.id(), "y"))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> readerService.downloadUrl(ANONYMOUS, pdf.id(), "z"))
            .isInstanceOf(ResourceNotFoundException.class);

        // Deleting a file in use needs confirmation (BR-KMED-005).
        assertThatThrownBy(() -> mediaService.delete(PUBLISHER, pdf.id(), false))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("IN_USE");
        mediaService.delete(PUBLISHER, pdf.id(), true);
        assertThat(storage.deleted).anyMatch(k -> k.endsWith(".pdf"));
    }

    @Test
    void organizationACannotPlayOrganizationBsRestrictedVideo() {
        KnowledgeMediaDtos.Media file = upload("setup.mp4", "video/mp4", 50_000L, KnowledgeMediaKind.VIDEO_FILE);
        KnowledgeContentRequest content = new KnowledgeContentRequest(KnowledgeContentType.VIDEO, "Inventory setup", null,
            null, productId, moduleId, categoryId, null, null, null, KnowledgeAudience.ORGANIZATION, List.of(801L), null,
            null, null, null, null, null, null, null, null, null, null, null, null);
        KnowledgeContentDto video = videoService.create(CONTRIBUTOR, new KnowledgeVideoDtos.VideoRequest(content,
            VideoSourceType.AWS_S3, null, null, file.id(), 135, null, null, null, "WEBVTT\n\n1\n00:00.000 --> 00:02.000\nWelcome",
            "00:00 Introduction\n02:15 Inventory setup"));
        assertThat(video.video().sourceType()).isEqualTo(VideoSourceType.AWS_S3);
        assertThat(video.video().transcript()).isEqualTo("Welcome");
        assertThat(video.video().chapters()).hasSize(2);
        contentService.submit(CONTRIBUTOR, video.id());
        contentService.approve(PUBLISHER, video.id());
        contentService.publish(PUBLISHER, video.id(), null);

        KnowledgeVideoDtos.PlayInfo play = videoService.playInfo(member(9, 801), video.id());
        assertThat(play.url()).contains("X-Amz-Expires=" + settings.getPlaybackUrlExpiry().toSeconds());
        assertThatThrownBy(() -> videoService.playInfo(member(10, 802), video.id()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> videoService.playInfo(ANONYMOUS, video.id()))
            .isInstanceOf(ResourceNotFoundException.class);
        // The reader view never carries the storage key or a media id.
        assertThat(readerService.get(member(9, 801), String.valueOf(video.id()), "v").video().mediaId()).isNull();

        // Deleting the video deletes its storage object (REQ-KNW-004.8).
        String key = mediaService.get(file.id(), true).storage().objectKey();
        contentService.delete(PUBLISHER, video.id());
        assertThat(storage.deleted).contains(key);
    }

    @Test
    void youTubeAndExternalVideosNeedNoStorage() {
        KnowledgeContentRequest content = new KnowledgeContentRequest(KnowledgeContentType.VIDEO, "Getting started tour",
            null, null, productId, moduleId, categoryId, null, null, null, KnowledgeAudience.PUBLIC, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null);
        KnowledgeContentDto yt = videoService.create(CONTRIBUTOR, new KnowledgeVideoDtos.VideoRequest(content,
            VideoSourceType.YOUTUBE, "https://youtu.be/dQw4w9WgXcQ?t=10", null, null, 212, "eVyoog", null, null, null, null));
        assertThat(yt.video().youtubeId()).isEqualTo("dQw4w9WgXcQ");
        assertThat(yt.video().thumbnailUrl()).startsWith("https://i.ytimg.com/");
        contentService.submit(CONTRIBUTOR, yt.id());
        contentService.approve(PUBLISHER, yt.id());
        contentService.publish(PUBLISHER, yt.id(), null);
        assertThat(videoService.playInfo(ANONYMOUS, yt.id()).embedId()).isEqualTo("dQw4w9WgXcQ");

        assertThatThrownBy(() -> videoService.create(CONTRIBUTOR, new KnowledgeVideoDtos.VideoRequest(content,
                VideoSourceType.EXTERNAL_URL, null, "http://insecure.example/video.mp4", null, null, null, null, null, null, null)))
            .isInstanceOf(KnowledgeException.class);
        assertThatThrownBy(() -> videoService.create(CONTRIBUTOR, new KnowledgeVideoDtos.VideoRequest(content,
                VideoSourceType.YOUTUBE, "https://example.com/watch?v=dQw4w9WgXcQ", null, null, null, null, null, null, null, null)))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("YOUTUBE_NOT_FOUND");
        KnowledgeContentRequest noModule = new KnowledgeContentRequest(KnowledgeContentType.VIDEO, "No module", null, null,
            productId, null, categoryId, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null);
        assertThatThrownBy(() -> videoService.create(CONTRIBUTOR, new KnowledgeVideoDtos.VideoRequest(noModule,
                VideoSourceType.YOUTUBE, "dQw4w9WgXcQ", null, null, null, null, null, null, null, null)))
            .isInstanceOf(KnowledgeException.class);
        assertThat(request(KnowledgeContentType.VIDEO, "x", "y")).isNotNull();
    }

    @Test
    void uploadsNeverCompletedAreRemovedByTheCleanupJob() {
        KnowledgeMediaDtos.UploadTicket ticket = mediaService.requestUpload(CONTRIBUTOR,
            new KnowledgeMediaDtos.UploadRequest("orphan.png", "image/png", 10L, KnowledgeMediaKind.IMAGE, productId, moduleId, null));
        storage.put(ticket.objectKey(), 10L, "image/png");
        assertThat(mediaService.cleanupOrphans(java.time.Instant.now())).isZero();
        assertThat(mediaService.cleanupOrphans(java.time.Instant.now().plus(settings.getOrphanAfter()).plusSeconds(60)))
            .isGreaterThanOrEqualTo(1);
        assertThat(storage.objects).doesNotContainKey(ticket.objectKey());
        assertThat(mediaService.get(ticket.mediaId(), true).status()).isEqualTo(KnowledgeMediaStatus.FAILED);
    }

    @Test
    void chaptersAndYouTubeLinksAreParsedStrictly() {
        assertThat(YouTubeVideoService.parseVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=x")).isEqualTo("dQw4w9WgXcQ");
        assertThat(YouTubeVideoService.parseVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ")).isEqualTo("dQw4w9WgXcQ");
        assertThat(YouTubeVideoService.parseVideoId("https://www.youtube.com/embed/dQw4w9WgXcQ")).isEqualTo("dQw4w9WgXcQ");
        assertThat(YouTubeVideoService.parseVideoId("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ")).isEqualTo("dQw4w9WgXcQ");
        assertThat(YouTubeVideoService.parseVideoId("https://vimeo.com/123")).isNull();
        assertThat(YouTubeVideoService.parseIsoDuration("PT1H2M3S")).isEqualTo(3723);
        assertThat(KnowledgeVideoService.parseChapters("00:00 Intro\n1:02:03 Deep dive", null)).contains("3723");
        assertThatThrownBy(() -> KnowledgeVideoService.parseChapters("02:00 B\n01:00 A", null)).isInstanceOf(KnowledgeException.class);
        assertThatThrownBy(() -> KnowledgeVideoService.parseChapters("05:00 Late", 120)).isInstanceOf(KnowledgeException.class);
        assertThatThrownBy(() -> KnowledgeVideoService.parseChapters("not a chapter", null)).isInstanceOf(KnowledgeException.class);
    }
}
