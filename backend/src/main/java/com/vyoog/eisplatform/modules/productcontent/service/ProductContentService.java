package com.vyoog.eisplatform.modules.productcontent.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAccessService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeReader;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeViewService;
import com.vyoog.eisplatform.modules.knowledgebase.service.MediaStorageService;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.product.service.ProductDeleteListener;
import com.vyoog.eisplatform.modules.productcontent.dto.ProductContentDtos.*;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentItem;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentKind;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentStatus;
import com.vyoog.eisplatform.modules.productcontent.repository.ProductContentItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Product content (REQ-CAT-004, 02.04): datasheets, documentation links,
 * images, videos and case studies configured by a platform administrator on
 * an application and read by customers on the product page. Files live in the
 * private bucket of {@link MediaStorageService}; the browser uploads straight
 * to it with a presigned URL and this service only issues the URL, then
 * verifies the object (BR-PCON-003, -004) — files never pass through here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductContentService implements ProductDeleteListener {

    private static final String PREFIX = "product-content/";

    private final ProductContentItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeViewService viewService;
    private final KnowledgeAccessService accessService;
    private final MediaStorageService storage;
    private final ProductContentSettings settings;
    private final AuditService auditService;

    /** The administrator behind a call, from the caller's own token. */
    public record Actor(String keycloakSub, String email) {
    }

    // ---------------------------------------------------------------- admin

    public AdminContent adminContent(Long productId) {
        requireProduct(productId);
        List<ProductContentItem> items = itemRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
        Map<Long, KnowledgeArticle> articles = articlesOf(items);
        return new AdminContent(items.stream().map((i) -> toAdmin(i, articles.get(i.getKnowledgeContentId()))).toList(), storageInfo());
    }

    private StorageInfo storageInfo() {
        return new StorageInfo(storage.configured(), settings.getImageMaxSize(), settings.getDocumentMaxSize(),
            ProductContentRules.IMAGE.keySet().stream().sorted().toList(), ProductContentRules.PDF.keySet().stream().sorted().toList());
    }

    public UploadUrl uploadUrl(Long productId, UploadUrlRequest request) {
        requireProduct(productId);
        requireStorage();
        if (request == null || request.kind() == null || request.fileName() == null || request.fileName().isBlank()
            || request.contentType() == null || request.size() == null) {
            throw KnowledgeException.badRequest("INVALID_FILE_TYPE", "File name, type, size and kind are required.");
        }
        boolean logo = "logo".equalsIgnoreCase(request.purpose());
        FileKind fileKind = fileKindFor(request.kind(), logo);
        String fileName = ProductContentRules.cleanFileName(request.fileName());
        String extension = checkFile(fileKind, fileName, request.contentType(), request.size());
        // BR-PCON-003: the key is generated here, never the file name.
        String key = PREFIX + productId + "/" + request.kind().name().toLowerCase() + (logo ? "-logo" : "") + "/"
            + UUID.randomUUID() + "." + extension;
        MediaStorageService.PresignedUrl url = storage.generateUploadUrl(key,
            ProductContentRules.baseContentType(request.contentType()), settings.getUploadUrlExpiry());
        return new UploadUrl(key, url.url(), url.expiresAt(), maxSize(fileKind));
    }

    @Transactional
    public AdminItem create(Actor actor, Long productId, ItemRequest request) {
        requireProduct(productId);
        if (request == null || request.kind() == null) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The kind of content is required.");
        }
        ProductContentItem item = new ProductContentItem();
        item.setProductId(productId);
        item.setKind(request.kind());
        item.setCreatedBySub(actor.keycloakSub());
        item.setDisplayOrder(itemRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId).stream()
            .mapToInt(ProductContentItem::getDisplayOrder).max().orElse(0) + 1);
        apply(item, request, true);
        item.setUpdatedBySub(actor.keycloakSub());
        ProductContentItem saved = itemRepository.save(item);
        audit(actor, "PRODUCT_CONTENT_ADDED", saved, "Added " + saved.getKind() + " \"" + saved.getTitle() + "\"");
        return toAdmin(saved, articleOf(saved));
    }

    @Transactional
    public AdminItem update(Actor actor, Long productId, Long itemId, ItemRequest request) {
        ProductContentItem item = find(productId, itemId);
        List<String> oldKeys = new ArrayList<>();
        boolean changed = apply(item, request, false, oldKeys);
        if (changed) {
            item.setContentVersion(item.getContentVersion() + 1);
            item.setUpdatedAt(Instant.now());
            item.setUpdatedBySub(actor.keycloakSub());
        }
        ProductContentItem saved = itemRepository.save(item);
        // BR-PCON-008: the replaced file is deleted once the new one is attached.
        oldKeys.forEach(this::safeDelete);
        if (changed) {
            audit(actor, "PRODUCT_CONTENT_UPDATED", saved, "Updated " + saved.getKind() + " \"" + saved.getTitle()
                + "\", now version " + saved.getContentVersion());
        }
        return toAdmin(saved, articleOf(saved));
    }

    @Transactional
    public void delete(Actor actor, Long productId, Long itemId) {
        ProductContentItem item = find(productId, itemId);
        List<String> keys = keysOf(item);
        itemRepository.delete(item);
        keys.forEach(this::safeDelete);
        audit(actor, "PRODUCT_CONTENT_DELETED", item, "Deleted " + item.getKind() + " \"" + item.getTitle() + "\"");
    }

    @Transactional
    public AdminItem setStatus(Actor actor, Long productId, Long itemId, ProductContentStatus status) {
        ProductContentItem item = find(productId, itemId);
        if (item.getStatus() != status) {
            item.setStatus(status);
            item.setPublishedAt(status == ProductContentStatus.PUBLISHED ? Instant.now() : null);
            item.setUpdatedBySub(actor.keycloakSub());
            itemRepository.save(item);
            audit(actor, status == ProductContentStatus.PUBLISHED ? "PRODUCT_CONTENT_PUBLISHED" : "PRODUCT_CONTENT_UNPUBLISHED",
                item, (status == ProductContentStatus.PUBLISHED ? "Published " : "Unpublished ") + item.getKind() + " \"" + item.getTitle() + "\"");
        }
        return toAdmin(item, articleOf(item));
    }

    @Transactional
    public AdminContent reorder(Actor actor, Long productId, OrderRequest request) {
        requireProduct(productId);
        List<ProductContentItem> items = itemRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
        Set<Long> known = items.stream().map(ProductContentItem::getId).collect(Collectors.toSet());
        List<Long> ids = request == null || request.ids() == null ? List.of() : request.ids();
        if (!known.containsAll(ids) || ids.size() != new java.util.HashSet<>(ids).size()) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The order contains an unknown item.");
        }
        Map<Long, ProductContentItem> byId = items.stream().collect(Collectors.toMap(ProductContentItem::getId, Function.identity()));
        int position = 1;
        for (Long id : ids) {
            byId.get(id).setDisplayOrder(position++);
        }
        for (ProductContentItem item : items) {
            if (!ids.contains(item.getId())) {
                item.setDisplayOrder(position++);
            }
        }
        itemRepository.saveAll(items);
        auditService.recordSuccess("PRODUCT_CONTENT_REORDERED", actor.keycloakSub(), null, actor.email(), "Product",
            String.valueOf(productId), null, "Reordered the content of the product");
        return adminContent(productId);
    }

    /** Short-lived link for the administrator to look at a file, drafts included. */
    public TemporaryUrl previewUrl(Long productId, Long itemId, boolean logo) {
        ProductContentItem item = find(productId, itemId);
        return temporaryUrl(item, logo, false);
    }

    /** Knowledge articles that can be linked as documentation: live and visible to everyone. */
    public List<DocumentationOption> documentationOptions(Long productId) {
        requireProduct(productId);
        Map<Long, Long> catalog = accessService.catalogLinks();
        return viewService.visible(KnowledgeReader.anonymous(), null).stream()
            .map((live) -> new DocumentationOption(live.item().getId(), live.item().getTitle(),
                live.item().getContentType().name(), live.item().getProductId() != null
                && Objects.equals(catalog.get(live.item().getProductId()), productId)))
            .sorted((a, b) -> a.forThisProduct() == b.forThisProduct()
                ? a.title().compareToIgnoreCase(b.title()) : (a.forThisProduct() ? -1 : 1))
            .limit(300)
            .toList();
    }

    // ---------------------------------------------------------------- public

    /** BR-PCON-005: published items of an Active application only. */
    public PublicContent publicContent(Long productId) {
        requireActiveProduct(productId);
        List<ProductContentItem> items = itemRepository
            .findByProductIdAndStatusOrderByDisplayOrderAscIdAsc(productId, ProductContentStatus.PUBLISHED);
        Map<Long, KnowledgeViewService.Live> docs = liveDocs(items);
        Map<ProductContentKind, List<PublicItem>> by = new HashMap<>();
        for (ProductContentItem item : items) {
            if (item.getKind() == ProductContentKind.DOCUMENTATION && !docs.containsKey(item.getKnowledgeContentId())) {
                continue; // BR-PCON-005: no longer live or public
            }
            by.computeIfAbsent(item.getKind(), (k) -> new ArrayList<>()).add(toPublic(item, docs.get(item.getKnowledgeContentId())));
        }
        return new PublicContent(by.getOrDefault(ProductContentKind.DATASHEET, List.of()),
            by.getOrDefault(ProductContentKind.DOCUMENTATION, List.of()), by.getOrDefault(ProductContentKind.IMAGE, List.of()),
            by.getOrDefault(ProductContentKind.VIDEO, List.of()), by.getOrDefault(ProductContentKind.CASE_STUDY, List.of()));
    }

    /** A 5-minute link for a published datasheet or case-study PDF. */
    public TemporaryUrl download(Long productId, Long itemId) {
        requireActiveProduct(productId);
        ProductContentItem item = find(productId, itemId);
        if (item.getStatus() != ProductContentStatus.PUBLISHED || item.getObjectKey() == null
            || item.getKind() == ProductContentKind.IMAGE) {
            throw new ResourceNotFoundException("File not found");
        }
        return temporaryUrl(item, false, true);
    }

    // ---------------------------------------------------------------- delete hook

    /** REQ-CAT-004.12: the application's files go with it (rows go by ON DELETE CASCADE). */
    @Override
    @Transactional
    public void onProductDeleted(Long productId) {
        List<ProductContentItem> items = itemRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
        items.stream().flatMap((i) -> keysOf(i).stream()).forEach(this::safeDelete);
        itemRepository.deleteAll(items);
    }

    // ---------------------------------------------------------------- internals

    private enum FileKind { IMAGE, PDF }

    private static FileKind fileKindFor(ProductContentKind kind, boolean logo) {
        if (logo) {
            if (kind != ProductContentKind.CASE_STUDY) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Only a case study has a customer logo.");
            }
            return FileKind.IMAGE;
        }
        return switch (kind) {
            case IMAGE -> FileKind.IMAGE;
            case DATASHEET, CASE_STUDY -> FileKind.PDF;
            default -> throw KnowledgeException.badRequest("INVALID_CONTENT", "This kind of content has no file.");
        };
    }

    private long maxSize(FileKind fileKind) {
        return fileKind == FileKind.IMAGE ? settings.getImageMaxSize() : settings.getDocumentMaxSize();
    }

    /** BR-PCON-002: type by extension and declared content type, size from configuration. Returns the extension. */
    private String checkFile(FileKind fileKind, String fileName, String contentType, long size) {
        Map<String, Set<String>> allowed = fileKind == FileKind.IMAGE ? ProductContentRules.IMAGE : ProductContentRules.PDF;
        String extension = ProductContentRules.extension(fileName);
        String type = ProductContentRules.baseContentType(contentType);
        if (!allowed.containsKey(extension) || !allowed.get(extension).contains(type)) {
            throw KnowledgeException.badRequest("INVALID_FILE_TYPE", "This file type is not allowed here. Allowed: "
                + String.join(", ", allowed.keySet().stream().sorted().toList()) + ".");
        }
        if (size <= 0) {
            throw KnowledgeException.badRequest("UPLOAD_MISMATCH", "The file is empty.");
        }
        if (size > maxSize(fileKind)) {
            throw KnowledgeException.badRequest("FILE_TOO_LARGE", "The file is too large. The maximum is "
                + humanSize(maxSize(fileKind)) + ".");
        }
        return extension;
    }

    private boolean apply(ProductContentItem item, ItemRequest request, boolean creating) {
        return apply(item, request, creating, new ArrayList<>());
    }

    /** Validates the request for the item's kind and copies it onto the item; true if anything changed. */
    private boolean apply(ProductContentItem item, ItemRequest request, boolean creating, List<String> replacedKeys) {
        if (request == null) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The content is required.");
        }
        ProductContentKind kind = item.getKind();
        if (request.kind() != null && request.kind() != kind) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The kind of an item cannot be changed.");
        }
        boolean changed = creating;
        String title = trimmed(request.title());
        if (title == null || title.length() > 200) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "A title of up to 200 characters is required.");
        }
        String description = trimmed(request.description());
        if (description != null && description.length() > 1000) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The description can have up to 1,000 characters.");
        }
        changed |= !Objects.equals(item.getTitle(), title) | !Objects.equals(item.getDescription(), description);
        item.setTitle(title);
        item.setDescription(description);

        switch (kind) {
            case DATASHEET -> changed |= applyFile(item, request.file(), creating, true, replacedKeys);
            case IMAGE -> {
                String alt = trimmed(request.altText());
                if (alt == null || alt.length() > 250) {
                    throw KnowledgeException.badRequest("INVALID_CONTENT", "Alt text of up to 250 characters is required for an image.");
                }
                changed |= !Objects.equals(item.getAltText(), alt);
                item.setAltText(alt);
                changed |= applyFile(item, request.file(), creating, true, replacedKeys);
            }
            case VIDEO -> {
                ProductVideoLink link;
                try {
                    link = ProductVideoLink.parse(request.videoUrl());
                } catch (IllegalArgumentException e) {
                    throw KnowledgeException.badRequest("INVALID_VIDEO_URL", e.getMessage());
                }
                changed |= !Objects.equals(item.getVideoUrl(), link.url());
                item.setVideoProvider(link.provider());
                item.setVideoRef(link.ref());
                item.setVideoUrl(link.url());
                item.setThumbnailUrl(link.thumbnailUrl());
            }
            case CASE_STUDY -> {
                String customer = trimmed(request.customerName());
                String problem = trimmed(request.problem());
                String result = trimmed(request.result());
                if (customer == null || customer.length() > 200) {
                    throw KnowledgeException.badRequest("INVALID_CONTENT", "The customer name is required (up to 200 characters).");
                }
                if ((problem != null && problem.length() > 4000) || (result != null && result.length() > 4000)) {
                    throw KnowledgeException.badRequest("INVALID_CONTENT", "The problem and the result can have up to 4,000 characters.");
                }
                changed |= !Objects.equals(item.getCustomerName(), customer) | !Objects.equals(item.getProblem(), problem)
                    | !Objects.equals(item.getResult(), result);
                item.setCustomerName(customer);
                item.setProblem(problem);
                item.setResult(result);
                changed |= applyFile(item, request.file(), creating, false, replacedKeys);
                changed |= applyLogo(item, request.logo(), replacedKeys);
            }
            case DOCUMENTATION -> {
                Long articleId = request.articleId();
                if (articleId == null || liveDoc(articleId).isEmpty()) {
                    throw KnowledgeException.badRequest("INVALID_CONTENT",
                        "Choose a live knowledge article that is visible to everyone.");
                }
                changed |= !Objects.equals(item.getKnowledgeContentId(), articleId);
                item.setKnowledgeContentId(articleId);
            }
        }
        return changed;
    }

    /** The item's main file: required on create when {@code required}; replaced when a new upload is attached. */
    private boolean applyFile(ProductContentItem item, UploadedFile file, boolean creating, boolean required, List<String> replaced) {
        if (file == null) {
            if (creating && required) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Upload the file first.");
            }
            return false;
        }
        FileKind fileKind = fileKindFor(item.getKind(), false);
        Verified v = verify(item.getProductId(), item.getKind(), file, fileKind, false);
        if (item.getObjectKey() != null) {
            replaced.add(item.getObjectKey());
        }
        item.setObjectKey(v.key());
        item.setFileName(v.fileName());
        item.setFileSize(v.size());
        item.setMimeType(v.contentType());
        return true;
    }

    private boolean applyLogo(ProductContentItem item, UploadedFile logo, List<String> replaced) {
        if (logo == null) {
            return false;
        }
        Verified v = verify(item.getProductId(), item.getKind(), logo, FileKind.IMAGE, true);
        if (item.getLogoObjectKey() != null) {
            replaced.add(item.getLogoObjectKey());
        }
        item.setLogoObjectKey(v.key());
        item.setLogoFileName(v.fileName());
        item.setLogoFileSize(v.size());
        item.setLogoMimeType(v.contentType());
        return true;
    }

    private record Verified(String key, String fileName, long size, String contentType) {
    }

    /** BR-PCON-003, -004: own prefix, allowed type and size, and the stored object matches what was declared. */
    private Verified verify(Long productId, ProductContentKind kind, UploadedFile file, FileKind fileKind, boolean logo) {
        requireStorage();
        String key = file.uploadKey();
        String expectedPrefix = PREFIX + productId + "/" + kind.name().toLowerCase() + (logo ? "-logo" : "") + "/";
        if (key == null || !key.startsWith(expectedPrefix) || key.contains("..")) {
            throw KnowledgeException.badRequest("UPLOAD_MISMATCH", "The upload does not belong to this item. Please upload the file again.");
        }
        if (file.fileName() == null || file.contentType() == null || file.size() == null) {
            throw KnowledgeException.badRequest("UPLOAD_MISMATCH", "The uploaded file is missing its details.");
        }
        String fileName = ProductContentRules.cleanFileName(file.fileName());
        checkFile(fileKind, fileName, file.contentType(), file.size());
        if (itemRepository.existsByObjectKeyOrLogoObjectKey(key, key)) {
            throw new KnowledgeException(HttpStatus.CONFLICT, "DUPLICATE_UPLOAD", "This upload is already used by an item.");
        }
        Optional<MediaStorageService.StoredObject> stored = storage.getMetadata(key);
        if (stored.isEmpty()) {
            throw KnowledgeException.badRequest("UPLOAD_NOT_FOUND", "The upload was not found. Please upload the file again.");
        }
        String storedType = ProductContentRules.baseContentType(stored.get().contentType());
        String declaredType = ProductContentRules.baseContentType(file.contentType());
        if (stored.get().size() != file.size() || !storedType.equals(declaredType)) {
            safeDelete(key);
            throw KnowledgeException.badRequest("UPLOAD_MISMATCH",
                "The uploaded file does not match what was declared. Please upload it again.");
        }
        return new Verified(key, fileName, file.size(), declaredType);
    }

    private TemporaryUrl temporaryUrl(ProductContentItem item, boolean logo, boolean download) {
        requireStorage();
        String key = logo ? item.getLogoObjectKey() : item.getObjectKey();
        String name = logo ? item.getLogoFileName() : item.getFileName();
        Long size = logo ? item.getLogoFileSize() : item.getFileSize();
        if (key == null) {
            throw new ResourceNotFoundException("File not found");
        }
        MediaStorageService.PresignedUrl url = storage.getTemporaryUrl(key, download ? name : null,
            download ? settings.getDownloadUrlExpiry() : settings.getViewUrlExpiry());
        return new TemporaryUrl(url.url(), url.expiresAt(), name, size);
    }

    private String viewUrl(String key) {
        if (key == null || !storage.configured()) {
            return null;
        }
        return storage.getTemporaryUrl(key, null, settings.getViewUrlExpiry()).url();
    }

    private AdminItem toAdmin(ProductContentItem i, KnowledgeArticle article) {
        boolean available = i.getKind() == ProductContentKind.DOCUMENTATION && article != null
            && liveDoc(article.getId()).isPresent();
        return new AdminItem(i.getId(), i.getKind(), i.getTitle(), i.getDescription(), i.getStatus(), i.getDisplayOrder(),
            i.getContentVersion(), i.getUpdatedAt(), i.getPublishedAt(), fileInfo(i.getFileName(), i.getFileSize(), i.getMimeType()),
            i.getKind() == ProductContentKind.IMAGE ? viewUrl(i.getObjectKey()) : null, i.getAltText(),
            fileInfo(i.getLogoFileName(), i.getLogoFileSize(), i.getLogoMimeType()), viewUrl(i.getLogoObjectKey()),
            i.getVideoProvider(), i.getVideoUrl(), ProductVideoLink.embedUrl(i.getVideoProvider(), i.getVideoRef()),
            i.getThumbnailUrl(), i.getCustomerName(), i.getProblem(), i.getResult(), i.getKnowledgeContentId(),
            article == null ? null : article.getTitle(), available);
    }

    private PublicItem toPublic(ProductContentItem i, KnowledgeViewService.Live doc) {
        String ref = doc == null ? null : (doc.item().getSlug() != null ? doc.item().getSlug() : String.valueOf(doc.item().getId()));
        return new PublicItem(i.getId(), i.getKind(), i.getTitle(), i.getDescription(), i.getContentVersion(), i.getUpdatedAt(),
            fileInfo(i.getFileName(), i.getFileSize(), i.getMimeType()),
            i.getKind() == ProductContentKind.IMAGE ? viewUrl(i.getObjectKey()) : null, i.getAltText(), viewUrl(i.getLogoObjectKey()),
            i.getVideoProvider(), i.getVideoUrl(), ProductVideoLink.embedUrl(i.getVideoProvider(), i.getVideoRef()), i.getThumbnailUrl(),
            i.getCustomerName(), i.getProblem(), i.getResult(), ref, doc == null ? null : doc.item().getContentType().name());
    }

    private static FileInfo fileInfo(String name, Long size, String type) {
        return name == null || size == null ? null : new FileInfo(name, size, type);
    }

    private Map<Long, KnowledgeArticle> articlesOf(List<ProductContentItem> items) {
        List<Long> ids = items.stream().map(ProductContentItem::getKnowledgeContentId).filter(Objects::nonNull).distinct().toList();
        // A HashMap on purpose: callers look up a null id for items without an article.
        return ids.isEmpty() ? new HashMap<>() : articleRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(KnowledgeArticle::getId, Function.identity()));
    }

    private KnowledgeArticle articleOf(ProductContentItem item) {
        return item.getKnowledgeContentId() == null ? null : articleRepository.findById(item.getKnowledgeContentId()).orElse(null);
    }

    private Optional<KnowledgeViewService.Live> liveDoc(Long articleId) {
        return articleRepository.findById(articleId).flatMap((a) -> viewService.visibleItem(a, KnowledgeReader.anonymous()));
    }

    /** Knowledge articles of these items that a visitor may see. */
    private Map<Long, KnowledgeViewService.Live> liveDocs(List<ProductContentItem> items) {
        List<KnowledgeArticle> articles = new ArrayList<>(articlesOf(items.stream()
            .filter((i) -> i.getKind() == ProductContentKind.DOCUMENTATION).toList()).values());
        return viewService.filterVisible(articles, KnowledgeReader.anonymous()).stream()
            .collect(Collectors.toMap((l) -> l.item().getId(), Function.identity()));
    }

    private ProductContentItem find(Long productId, Long itemId) {
        ProductContentItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + itemId));
        if (!item.getProductId().equals(productId)) {
            throw new ResourceNotFoundException("Content not found: " + itemId);
        }
        return item;
    }

    private void requireProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
    }

    private void requireActiveProduct(Long productId) {
        if (productRepository.findById(productId).filter((p) -> p.getStatus() == ProductStatus.ACTIVE).isEmpty()) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
    }

    private void requireStorage() {
        if (!storage.configured()) {
            throw new KnowledgeException(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_NOT_CONFIGURED",
                "File storage is not configured yet. Links and case studies without files still work.");
        }
    }

    private static List<String> keysOf(ProductContentItem item) {
        List<String> keys = new ArrayList<>();
        if (item.getObjectKey() != null) {
            keys.add(item.getObjectKey());
        }
        if (item.getLogoObjectKey() != null) {
            keys.add(item.getLogoObjectKey());
        }
        return keys;
    }

    private void safeDelete(String key) {
        try {
            if (storage.configured()) {
                storage.delete(key);
            }
        } catch (RuntimeException e) {
            log.warn("Could not delete product content object {}: {}", key, e.getMessage());
        }
    }

    private void audit(Actor actor, String action, ProductContentItem item, String detail) {
        auditService.recordSuccess(action, actor.keycloakSub(), null, actor.email(), "ProductContent",
            String.valueOf(item.getId()), null, detail + " (product " + item.getProductId() + ")");
    }

    private static String trimmed(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static String humanSize(long bytes) {
        return bytes >= 1024 * 1024 ? (bytes / (1024 * 1024)) + " MB" : (bytes / 1024) + " KB";
    }
}
