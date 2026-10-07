package com.vyoog.eisplatform.modules.productcontent.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One piece of content of one catalog product (REQ-CAT-004, 02.04). Files
 * are only referenced by their private S3 object key — never stored here and
 * never exposed as a URL (BR-PCON-006). Columns by kind: file (datasheet,
 * image, case-study PDF), logo (case study), video_* (video), article_id
 * (documentation), customer/problem/result (case study).
 */
@Entity
@Table(name = "product_content_item")
@Getter
@Setter
public class ProductContentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductContentKind kind;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductContentStatus status = ProductContentStatus.DRAFT;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    /** Latest version only (REQ-CAT-004.4): +1 on every edit or file replacement. */
    @Column(name = "content_version", nullable = false)
    private int contentVersion = 1;

    // ---- file (datasheet, image, case-study PDF) ----
    @Column(name = "object_key", length = 500, unique = true)
    private String objectKey;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "alt_text", length = 250)
    private String altText;

    // ---- case-study logo ----
    @Column(name = "logo_object_key", length = 500, unique = true)
    private String logoObjectKey;

    @Column(name = "logo_file_name", length = 255)
    private String logoFileName;

    @Column(name = "logo_file_size")
    private Long logoFileSize;

    @Column(name = "logo_mime_type", length = 100)
    private String logoMimeType;

    // ---- video link ----
    @Column(name = "video_provider", length = 20)
    private String videoProvider;

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "video_ref", length = 50)
    private String videoRef;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    // ---- case study ----
    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(columnDefinition = "TEXT")
    private String problem;

    @Column(name = "result_text", columnDefinition = "TEXT")
    private String result;

    // ---- documentation: a knowledge article ----
    @Column(name = "knowledge_content_id")
    private Long knowledgeContentId;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_by_sub", length = 100)
    private String createdBySub;

    @Column(name = "updated_by_sub", length = 100)
    private String updatedBySub;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
