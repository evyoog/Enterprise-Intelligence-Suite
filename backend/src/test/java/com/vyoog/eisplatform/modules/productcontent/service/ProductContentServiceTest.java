package com.vyoog.eisplatform.modules.productcontent.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePublishRequest;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeContentService;
import com.vyoog.eisplatform.modules.knowledgebase.support.InMemoryMediaStorage;
import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import com.vyoog.eisplatform.modules.productcontent.dto.ProductContentDtos.*;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentKind;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentStatus;
import com.vyoog.eisplatform.modules.productcontent.repository.ProductContentItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.CONTRIBUTOR;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.PUBLISHER;
import static com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeFixtures.request;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-CAT-004 product content (02.04): admin configuration, upload checks, publishing, versions, public view. */
@SpringBootTest
@ActiveProfiles("test")
@Import(KnowledgeTestConfig.class)
class ProductContentServiceTest {

    private static final ProductContentService.Actor ADMIN = new ProductContentService.Actor("sub-admin", "admin@test");

    @Autowired
    private ProductContentService service;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductContentItemRepository itemRepository;
    @Autowired
    private InMemoryMediaStorage storage;
    @Autowired
    private KnowledgeContentService knowledgeContent;

    private Long productId;

    @BeforeEach
    void product() {
        storage.configured = true;
        productId = newProduct(ProductStatus.ACTIVE);
    }

    @AfterEach
    void storageBack() {
        storage.configured = true;
    }

    private Long newProduct(ProductStatus status) {
        Product product = new Product();
        product.setName("Content test " + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(status);
        return productRepository.save(product).getId();
    }

    /** The browser's two steps: ask for an upload URL, then PUT the file straight to storage. */
    private UploadedFile upload(Long product, ProductContentKind kind, String purpose, String fileName, String type, long size) {
        UploadUrl url = service.uploadUrl(product, new UploadUrlRequest(kind, purpose, fileName, type, size));
        storage.put(url.uploadKey(), size, type);
        return new UploadedFile(url.uploadKey(), fileName, type, size);
    }

    private ItemRequest datasheet(String title, UploadedFile file) {
        return new ItemRequest(ProductContentKind.DATASHEET, title, null, null, null, null, null, null, null, file, null);
    }

    private ItemRequest image(String title, String alt, UploadedFile file) {
        return new ItemRequest(ProductContentKind.IMAGE, title, null, alt, null, null, null, null, null, file, null);
    }

    private ItemRequest video(String title, String url) {
        return new ItemRequest(ProductContentKind.VIDEO, title, null, null, url, null, null, null, null, null, null);
    }

    private ItemRequest caseStudy(String customer, UploadedFile pdf, UploadedFile logo) {
        return new ItemRequest(ProductContentKind.CASE_STUDY, "How " + customer + " saved time", null, null, null, customer,
            "Slow month-end", "Closed 3 days faster", null, pdf, logo);
    }

    // ---- upload URL (REQ-CAT-004.2, .5; BR-PCON-002, -003) ----

    @Test
    void uploadUrlIsForANewKeyUnderTheProductWithAShortExpiry() {
        UploadUrl url = service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null,
            "../../Q3 datasheet.pdf", "application/pdf", 1_000_000L));
        assertThat(url.uploadKey()).startsWith("product-content/" + productId + "/datasheet/").endsWith(".pdf")
            .doesNotContain("Q3").doesNotContain("..");
        assertThat(url.uploadUrl()).contains("method=PUT");
        assertThat(storage.issuedExpiries).contains(Duration.ofMinutes(15));
        assertThat(url.maxSize()).isEqualTo(20L * 1024 * 1024);
    }

    @Test
    void refusesWrongTypesAndTooLargeFilesWithAFriendlyCode() {
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.IMAGE, null, "logo.svg", "image/svg+xml", 100L)), "INVALID_FILE_TYPE");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.IMAGE, null, "anim.gif", "image/gif", 100L)), "INVALID_FILE_TYPE");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null, "run.exe", "application/octet-stream", 100L)), "INVALID_FILE_TYPE");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null, "sheet.pdf", "image/png", 100L)), "INVALID_FILE_TYPE");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.IMAGE, null, "a.pdf", "application/pdf", 100L)), "INVALID_FILE_TYPE");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.IMAGE, null, "big.png", "image/png", 5L * 1024 * 1024 + 1)), "FILE_TOO_LARGE");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null, "big.pdf", "application/pdf", 20L * 1024 * 1024 + 1)), "FILE_TOO_LARGE");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.VIDEO, null, "v.mp4", "video/mp4", 100L)), "INVALID_CONTENT");
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.IMAGE, "logo", "l.png", "image/png", 100L)), "INVALID_CONTENT");
        assertThatThrownBy(() -> service.uploadUrl(-5L, new UploadUrlRequest(ProductContentKind.IMAGE, null, "a.png", "image/png", 1L)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void withoutStorageFilesAreRefusedButLinksStillWork() {
        storage.configured = false;
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null, "a.pdf", "application/pdf", 10L)),
            "STORAGE_NOT_CONFIGURED");
        AdminItem item = service.create(ADMIN, productId, video("Overview", "https://youtu.be/dQw4w9WgXcQ"));
        assertThat(item.status()).isEqualTo(ProductContentStatus.DRAFT);
        assertThat(service.adminContent(productId).storage().configured()).isFalse();
    }

    // ---- saving an item verifies the upload (BR-PCON-003, -004) ----

    @Test
    void savingACompletedUploadCreatesADraftVersionOne() {
        UploadedFile file = upload(productId, ProductContentKind.DATASHEET, null, "Sheet.pdf", "application/pdf", 2048);
        AdminItem item = service.create(ADMIN, productId, datasheet("Platform datasheet", file));
        assertThat(item.status()).isEqualTo(ProductContentStatus.DRAFT);
        assertThat(item.version()).isEqualTo(1);
        assertThat(item.file().fileName()).isEqualTo("Sheet.pdf");
        assertThat(item.file().size()).isEqualTo(2048);
    }

    @Test
    void anUploadThatDoesNotMatchWhatWasDeclaredIsRefusedAndDeleted() {
        UploadUrl url = service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null, "a.pdf", "application/pdf", 5000L));
        storage.put(url.uploadKey(), 4999, "application/pdf");
        assertCode(() -> service.create(ADMIN, productId, datasheet("Mismatch", new UploadedFile(url.uploadKey(), "a.pdf", "application/pdf", 5000L))),
            "UPLOAD_MISMATCH");
        assertThat(storage.deleted).contains(url.uploadKey());
        assertThat(itemRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId)).isEmpty();

        UploadUrl typed = service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null, "b.pdf", "application/pdf", 100L));
        storage.put(typed.uploadKey(), 100, "text/html");
        assertCode(() -> service.create(ADMIN, productId, datasheet("Wrong type", new UploadedFile(typed.uploadKey(), "b.pdf", "application/pdf", 100L))),
            "UPLOAD_MISMATCH");
    }

    @Test
    void aMissingObjectAForeignKeyAndAReusedKeyAreRefused() {
        UploadUrl url = service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.DATASHEET, null, "a.pdf", "application/pdf", 10L));
        assertCode(() -> service.create(ADMIN, productId, datasheet("Not uploaded", new UploadedFile(url.uploadKey(), "a.pdf", "application/pdf", 10L))),
            "UPLOAD_NOT_FOUND");

        Long other = newProduct(ProductStatus.ACTIVE);
        UploadedFile foreign = upload(other, ProductContentKind.DATASHEET, null, "x.pdf", "application/pdf", 10);
        assertCode(() -> service.create(ADMIN, productId, datasheet("Another product's file", foreign)), "UPLOAD_MISMATCH");
        UploadedFile traversal = new UploadedFile("product-content/" + productId + "/datasheet/../../secret.pdf", "x.pdf", "application/pdf", 10L);
        assertCode(() -> service.create(ADMIN, productId, datasheet("Traversal", traversal)), "UPLOAD_MISMATCH");
        assertCode(() -> service.create(ADMIN, productId, datasheet("No file", null)), "INVALID_CONTENT");

        UploadedFile ok = upload(productId, ProductContentKind.DATASHEET, null, "ok.pdf", "application/pdf", 10);
        service.create(ADMIN, productId, datasheet("First", ok));
        assertCode(() -> service.create(ADMIN, productId, datasheet("Second with the same file", ok)), "DUPLICATE_UPLOAD");
    }

    // ---- kinds ----

    @Test
    void anImageNeedsAltText() {
        UploadedFile file = upload(productId, ProductContentKind.IMAGE, null, "shot.png", "image/png", 4000);
        assertCode(() -> service.create(ADMIN, productId, image("Dashboard", " ", file)), "INVALID_CONTENT");
        AdminItem item = service.create(ADMIN, productId, image("Dashboard", "The dashboard with three charts", file));
        assertThat(item.altText()).isEqualTo("The dashboard with three charts");
        assertThat(item.fileUrl()).contains("method=GET");
    }

    @Test
    void videoLinksAreParsedAndNonHttpsLinksRefused() {
        AdminItem yt = service.create(ADMIN, productId, video("Demo", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertThat(yt.videoProvider()).isEqualTo("YOUTUBE");
        assertThat(yt.embedUrl()).isEqualTo("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ");
        assertThat(yt.thumbnailUrl()).contains("dQw4w9WgXcQ");
        AdminItem other = service.create(ADMIN, productId, video("Webinar", "https://videos.example.com/w.mp4"));
        assertThat(other.videoProvider()).isEqualTo("EXTERNAL");
        assertThat(other.embedUrl()).isNull();
        assertCode(() -> service.create(ADMIN, productId, video("Insecure", "http://youtu.be/dQw4w9WgXcQ")), "INVALID_VIDEO_URL");
        assertCode(() -> service.create(ADMIN, productId, video("Script", "javascript:alert(1)")), "INVALID_VIDEO_URL");
    }

    @Test
    void aCaseStudyNeedsACustomerAndMayHaveAPdfAndALogo() {
        assertCode(() -> service.create(ADMIN, productId, caseStudy(" ", null, null)), "INVALID_CONTENT");
        UploadedFile pdf = upload(productId, ProductContentKind.CASE_STUDY, null, "acme.pdf", "application/pdf", 9000);
        UploadedFile logo = upload(productId, ProductContentKind.CASE_STUDY, "logo", "acme.png", "image/png", 800);
        AdminItem item = service.create(ADMIN, productId, caseStudy("Acme", pdf, logo));
        assertThat(item.customerName()).isEqualTo("Acme");
        assertThat(item.file().fileName()).isEqualTo("acme.pdf");
        assertThat(item.logo().fileName()).isEqualTo("acme.png");
        assertThat(item.logoUrl()).contains("method=GET");
        assertThat(service.create(ADMIN, productId, caseStudy("No files", null, null)).file()).isNull();
        // a logo must be an image
        assertCode(() -> service.uploadUrl(productId, new UploadUrlRequest(ProductContentKind.CASE_STUDY, "logo", "x.pdf", "application/pdf", 10L)), "INVALID_FILE_TYPE");
    }

    // ---- publishing and what a visitor sees (REQ-CAT-004.9, BR-PCON-005, -006) ----

    @Test
    void visitorsSeeOnlyPublishedItemsOfAnActiveProduct() {
        UploadedFile sheet = upload(productId, ProductContentKind.DATASHEET, null, "s.pdf", "application/pdf", 100);
        AdminItem draft = service.create(ADMIN, productId, datasheet("Datasheet", sheet));
        AdminItem vid = service.create(ADMIN, productId, video("Demo", "https://vimeo.com/76979871"));
        assertThat(service.publicContent(productId).datasheets()).isEmpty();
        assertThat(service.publicContent(productId).videos()).isEmpty();

        service.setStatus(ADMIN, productId, draft.id(), ProductContentStatus.PUBLISHED);
        service.setStatus(ADMIN, productId, vid.id(), ProductContentStatus.PUBLISHED);
        PublicContent visible = service.publicContent(productId);
        assertThat(visible.datasheets()).extracting(PublicItem::title).containsExactly("Datasheet");
        assertThat(visible.videos().get(0).embedUrl()).isEqualTo("https://player.vimeo.com/video/76979871?dnt=1");

        service.setStatus(ADMIN, productId, draft.id(), ProductContentStatus.DRAFT);
        assertThat(service.publicContent(productId).datasheets()).isEmpty();
    }

    @Test
    void contentIsHiddenWhileTheProductIsNotActive() {
        Long retired = newProduct(ProductStatus.RETIRED);
        AdminItem item = service.create(ADMIN, retired, video("Demo", "https://youtu.be/dQw4w9WgXcQ"));
        service.setStatus(ADMIN, retired, item.id(), ProductContentStatus.PUBLISHED);
        assertThatThrownBy(() -> service.publicContent(retired)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.download(retired, item.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadsAreShortLivedAndNeverForDraftsOrWrongProducts() {
        UploadedFile sheet = upload(productId, ProductContentKind.DATASHEET, null, "s.pdf", "application/pdf", 100);
        AdminItem item = service.create(ADMIN, productId, datasheet("Datasheet", sheet));
        assertThatThrownBy(() -> service.download(productId, item.id())).isInstanceOf(ResourceNotFoundException.class);
        service.setStatus(ADMIN, productId, item.id(), ProductContentStatus.PUBLISHED);
        storage.issuedExpiries.clear();
        TemporaryUrl link = service.download(productId, item.id());
        assertThat(link.url()).contains("X-Amz-Expires=300");
        assertThat(link.fileName()).isEqualTo("s.pdf");
        assertThat(storage.issuedExpiries).containsExactly(Duration.ofMinutes(5));
        Long other = newProduct(ProductStatus.ACTIVE);
        assertThatThrownBy(() -> service.download(other, item.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void imagesUseAOneHourLinkAndNoResponseHoldsACredentialOrABucketAddress() {
        UploadedFile file = upload(productId, ProductContentKind.IMAGE, null, "a.webp", "image/webp", 100);
        AdminItem item = service.create(ADMIN, productId, image("Hero", "Hero shot", file));
        service.setStatus(ADMIN, productId, item.id(), ProductContentStatus.PUBLISHED);
        storage.issuedExpiries.clear();
        PublicContent content = service.publicContent(productId);
        assertThat(storage.issuedExpiries).containsOnly(Duration.ofHours(1));
        String everything = content.toString() + service.adminContent(productId);
        assertThat(everything).doesNotContain("AKIA").doesNotContain("secret").doesNotContain("eis-knowledge-test");
    }

    // ---- versions (REQ-CAT-004.4, BR-PCON-008) ----

    @Test
    void editingOrReplacingAFileRaisesTheVersionAndDeletesTheOldFile() {
        UploadedFile first = upload(productId, ProductContentKind.DATASHEET, null, "v1.pdf", "application/pdf", 100);
        AdminItem item = service.create(ADMIN, productId, datasheet("Datasheet", first));

        // saving the same values changes nothing
        AdminItem same = service.update(ADMIN, productId, item.id(), datasheet("Datasheet", null));
        assertThat(same.version()).isEqualTo(1);

        AdminItem renamed = service.update(ADMIN, productId, item.id(), datasheet("Datasheet 2026", null));
        assertThat(renamed.version()).isEqualTo(2);

        UploadedFile second = upload(productId, ProductContentKind.DATASHEET, null, "v2.pdf", "application/pdf", 200);
        AdminItem replaced = service.update(ADMIN, productId, item.id(), datasheet("Datasheet 2026", second));
        assertThat(replaced.version()).isEqualTo(3);
        assertThat(replaced.file().fileName()).isEqualTo("v2.pdf");
        assertThat(storage.deleted).contains(first.uploadKey());
        assertThat(storage.objects).containsKey(second.uploadKey()).doesNotContainKey(first.uploadKey());
        assertThat(replaced.updatedAt()).isAfterOrEqualTo(item.updatedAt());
    }

    @Test
    void theKindOfAnItemCannotChange() {
        AdminItem item = service.create(ADMIN, productId, video("Demo", "https://youtu.be/dQw4w9WgXcQ"));
        assertCode(() -> service.update(ADMIN, productId, item.id(),
            new ItemRequest(ProductContentKind.CASE_STUDY, "x", null, null, null, "c", null, null, null, null, null)), "INVALID_CONTENT");
    }

    // ---- documentation (REQ-CAT-004.3, BR-PCON-005) ----

    @Test
    void documentationLinksOnlyLivePublicArticlesAndDisappearsWhenTheArticleDoes() {
        KnowledgeContentDto created = knowledgeContent.create(CONTRIBUTOR,
            request(KnowledgeContentType.PRODUCT_GUIDE, "Getting started with " + System.nanoTime(), "Open the app."));
        ItemRequest doc = new ItemRequest(ProductContentKind.DOCUMENTATION, "Getting started", null, null, null, null, null, null,
            created.id(), null, null);
        assertCode(() -> service.create(ADMIN, productId, doc), "INVALID_CONTENT"); // still a draft

        knowledgeContent.submit(CONTRIBUTOR, created.id());
        knowledgeContent.approve(PUBLISHER, created.id());
        knowledgeContent.publish(PUBLISHER, created.id(), new KnowledgePublishRequest("MINOR", null));
        assertThat(service.documentationOptions(productId)).extracting(DocumentationOption::id).contains(created.id());

        AdminItem item = service.create(ADMIN, productId, doc);
        assertThat(item.articleAvailable()).isTrue();
        service.setStatus(ADMIN, productId, item.id(), ProductContentStatus.PUBLISHED);
        PublicItem shown = service.publicContent(productId).documentation().get(0);
        assertThat(shown.articleType()).isEqualTo("PRODUCT_GUIDE");
        assertThat(shown.articleRef()).isNotBlank();

        knowledgeContent.unpublish(PUBLISHER, created.id());
        assertThat(service.publicContent(productId).documentation()).isEmpty();
        assertThat(service.adminContent(productId).items().get(0).articleAvailable()).isFalse();
    }

    // ---- ordering, delete (REQ-CAT-004.8, .12) ----

    @Test
    void itemsAreReorderedAndNewOnesGoLast() {
        AdminItem a = service.create(ADMIN, productId, video("A", "https://youtu.be/dQw4w9WgXcQ"));
        AdminItem b = service.create(ADMIN, productId, video("B", "https://youtu.be/dQw4w9WgXcR"));
        AdminItem c = service.create(ADMIN, productId, video("C", "https://youtu.be/dQw4w9WgXcS"));
        assertThat(service.adminContent(productId).items()).extracting(AdminItem::title).containsExactly("A", "B", "C");
        AdminContent reordered = service.reorder(ADMIN, productId, new OrderRequest(List.of(c.id(), a.id(), b.id())));
        assertThat(reordered.items()).extracting(AdminItem::title).containsExactly("C", "A", "B");
        assertCode(() -> service.reorder(ADMIN, productId, new OrderRequest(List.of(c.id(), 999_999L))), "INVALID_CONTENT");
    }

    @Test
    void deletingAnItemDeletesItsFilesAndDeletingTheProductDeletesEverything() {
        UploadedFile pdf = upload(productId, ProductContentKind.CASE_STUDY, null, "c.pdf", "application/pdf", 100);
        UploadedFile logo = upload(productId, ProductContentKind.CASE_STUDY, "logo", "c.png", "image/png", 100);
        AdminItem item = service.create(ADMIN, productId, caseStudy("Globex", pdf, logo));
        service.delete(ADMIN, productId, item.id());
        assertThat(storage.deleted).contains(pdf.uploadKey(), logo.uploadKey());
        assertThat(itemRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId)).isEmpty();

        UploadedFile img = upload(productId, ProductContentKind.IMAGE, null, "i.png", "image/png", 100);
        service.create(ADMIN, productId, image("Shot", "A shot", img));
        productService.deleteProduct(productId);
        assertThat(storage.deleted).contains(img.uploadKey());
        assertThat(itemRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId)).isEmpty();
    }

    private static void assertCode(Runnable call, String code) {
        assertThatThrownBy(call::run).isInstanceOf(KnowledgeException.class)
            .satisfies((e) -> assertThat(((KnowledgeException) e).getCode()).isEqualTo(code));
    }
}
