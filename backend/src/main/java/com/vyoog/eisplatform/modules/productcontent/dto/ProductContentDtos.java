package com.vyoog.eisplatform.modules.productcontent.dto;

import com.vyoog.eisplatform.modules.productcontent.model.ProductContentKind;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentStatus;

import java.time.Instant;
import java.util.List;

/** Product content payloads (REQ-CAT-004). No credentials or permanent file URLs, ever (BR-PCON-006). */
public final class ProductContentDtos {

    private ProductContentDtos() {
    }

    public record FileInfo(String fileName, long size, String mimeType) {
    }

    /** What the administrator sees for one item (drafts included). */
    public record AdminItem(Long id, ProductContentKind kind, String title, String description,
                            ProductContentStatus status, int displayOrder, int version, Instant updatedAt,
                            Instant publishedAt, FileInfo file, String fileUrl, String altText, FileInfo logo,
                            String logoUrl, String videoProvider, String videoUrl, String embedUrl,
                            String thumbnailUrl, String customerName, String problem, String result,
                            Long articleId, String articleTitle, boolean articleAvailable) {
    }

    public record StorageInfo(boolean configured, long imageMaxSize, long documentMaxSize,
                              List<String> imageTypes, List<String> documentTypes) {
    }

    public record AdminContent(List<AdminItem> items, StorageInfo storage) {
    }

    /** A file the browser already uploaded: the key from the upload URL step, plus what it declared. */
    public record UploadedFile(String uploadKey, String fileName, String contentType, Long size) {
    }

    /** Create or edit one item; a file or logo is set only when a new upload is attached. */
    public record ItemRequest(ProductContentKind kind, String title, String description, String altText,
                              String videoUrl, String customerName, String problem, String result,
                              Long articleId, UploadedFile file, UploadedFile logo) {
    }

    /** purpose is "file" (default) or "logo" (case-study customer logo). */
    public record UploadUrlRequest(ProductContentKind kind, String purpose, String fileName, String contentType, Long size) {
    }

    public record UploadUrl(String uploadKey, String uploadUrl, Instant expiresAt, long maxSize) {
    }

    public record OrderRequest(List<Long> ids) {
    }

    public record TemporaryUrl(String url, Instant expiresAt, String fileName, Long size) {
    }

    public record DocumentationOption(Long id, String title, String type, boolean forThisProduct) {
    }

    /** What a visitor sees for one published item. */
    public record PublicItem(Long id, ProductContentKind kind, String title, String description, int version,
                             Instant updatedAt, FileInfo file, String imageUrl, String altText, String logoUrl,
                             String videoProvider, String videoUrl, String embedUrl, String thumbnailUrl,
                             String customerName, String problem, String result, String articleRef,
                             String articleType) {
    }

    public record PublicContent(List<PublicItem> datasheets, List<PublicItem> documentation, List<PublicItem> images,
                                List<PublicItem> videos, List<PublicItem> caseStudies) {
    }
}
