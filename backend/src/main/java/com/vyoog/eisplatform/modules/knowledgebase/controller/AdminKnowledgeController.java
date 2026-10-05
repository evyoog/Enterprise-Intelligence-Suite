package com.vyoog.eisplatform.modules.knowledgebase.controller;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeAnalyticsDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeCommentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeCompareDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRowDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeItemDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePageDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePublishRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeTaxonomyDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVersionDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAccessService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeActor;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAnalyticsService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeContentService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeSearchIndexService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeTaxonomyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Knowledge Management (REQ-KNW-002, -006, -008). SecurityConfig lets in
 * contributors and publishers only; publisher-only actions are checked here
 * again (BR-KPRM-002) before the service runs.
 */
@RestController
@RequestMapping("/admin/knowledge")
@RequiredArgsConstructor
public class AdminKnowledgeController {

    private final KnowledgeAccessService accessService;
    private final KnowledgeContentService contentService;
    private final KnowledgeTaxonomyService taxonomyService;
    private final KnowledgeAnalyticsService analyticsService;
    private final KnowledgeSearchIndexService searchIndexService;

    /** What the caller may do — the UI hides the rest (BR-KPRM-004). */
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        KnowledgeActor actor = accessService.actor(auth);
        return Map.of("contributor", actor.contributor(), "publisher", actor.publisher(),
            "contentTypes", KnowledgeContentService.EDITABLE_TYPES.stream().map(Enum::name).sorted().toList());
    }

    @GetMapping("/content")
    public KnowledgePageDto<KnowledgeContentRowDto> list(@RequestParam(value = "type", required = false) KnowledgeContentType type,
                                                         @RequestParam(value = "status", required = false) String status,
                                                         @RequestParam(value = "product", required = false) Long product,
                                                         @RequestParam(value = "q", required = false) String q,
                                                         @RequestParam(value = "source", required = false) String source,
                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "25") int size,
                                                         Authentication auth) {
        accessService.actor(auth);
        return contentService.list(type, status, product, q, source, page, size);
    }

    @PostMapping("/content")
    public KnowledgeContentDto create(@Valid @RequestBody KnowledgeContentRequest request, Authentication auth) {
        return contentService.create(accessService.actor(auth), request);
    }

    @GetMapping("/content/{id}")
    public KnowledgeContentDto get(@PathVariable("id") Long id, Authentication auth) {
        accessService.actor(auth);
        return contentService.get(id);
    }

    @PutMapping("/content/{id}")
    public KnowledgeContentDto update(@PathVariable("id") Long id, @Valid @RequestBody KnowledgeContentRequest request,
                                      Authentication auth) {
        return contentService.update(accessService.actor(auth), id, request);
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, Authentication auth) {
        contentService.delete(publisher(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/content/{id}/submit")
    public KnowledgeContentDto submit(@PathVariable("id") Long id, Authentication auth) {
        return contentService.submit(accessService.actor(auth), id);
    }

    @PostMapping("/content/{id}/approve")
    public KnowledgeContentDto approve(@PathVariable("id") Long id, Authentication auth) {
        return contentService.approve(publisher(auth), id);
    }

    @PostMapping("/content/{id}/return")
    public KnowledgeContentDto returnToDraft(@PathVariable("id") Long id,
                                            @Valid @RequestBody(required = false) KnowledgeCommentRequest body,
                                            Authentication auth) {
        return contentService.returnToDraft(publisher(auth), id, body == null ? null : body.comment());
    }

    @PostMapping("/content/{id}/publish")
    public KnowledgeContentDto publish(@PathVariable("id") Long id,
                                       @RequestBody(required = false) KnowledgePublishRequest body, Authentication auth) {
        return contentService.publish(publisher(auth), id, body);
    }

    @PostMapping("/content/{id}/unschedule")
    public KnowledgeContentDto unschedule(@PathVariable("id") Long id, Authentication auth) {
        return contentService.unschedule(publisher(auth), id);
    }

    @PostMapping("/content/{id}/unpublish")
    public KnowledgeContentDto unpublish(@PathVariable("id") Long id, Authentication auth) {
        return contentService.unpublish(publisher(auth), id);
    }

    @PostMapping("/content/{id}/deprecate")
    public KnowledgeContentDto deprecate(@PathVariable("id") Long id, Authentication auth) {
        return contentService.deprecate(publisher(auth), id);
    }

    @PostMapping("/content/{id}/archive")
    public KnowledgeContentDto archive(@PathVariable("id") Long id, Authentication auth) {
        return contentService.archive(publisher(auth), id);
    }

    @PostMapping("/content/{id}/restore")
    public KnowledgeContentDto restore(@PathVariable("id") Long id, Authentication auth) {
        return contentService.restore(publisher(auth), id);
    }

    @GetMapping("/content/{id}/preview")
    public KnowledgeItemDto preview(@PathVariable("id") Long id, Authentication auth) {
        accessService.actor(auth);
        return contentService.preview(id);
    }

    @GetMapping("/content/{id}/versions")
    public List<KnowledgeVersionDto> versions(@PathVariable("id") Long id, Authentication auth) {
        accessService.actor(auth);
        return contentService.versions(id);
    }

    @GetMapping("/content/{id}/versions/compare")
    public KnowledgeCompareDto compare(@PathVariable("id") Long id, @RequestParam("from") String from,
                                       @RequestParam("to") String to, Authentication auth) {
        accessService.actor(auth);
        return contentService.compare(id, from, to);
    }

    @GetMapping("/content/{id}/versions/{label}")
    public KnowledgeVersionDto version(@PathVariable("id") Long id, @PathVariable("label") String label,
                                       Authentication auth) {
        accessService.actor(auth);
        return contentService.version(id, label);
    }

    @PostMapping("/content/{id}/versions/{label}/restore")
    public KnowledgeContentDto restoreVersion(@PathVariable("id") Long id, @PathVariable("label") String label,
                                              Authentication auth) {
        return contentService.restoreVersion(publisher(auth), id, label);
    }

    // ---- taxonomy (read: contributors; write: publishers) ---------------------

    @GetMapping("/taxonomy")
    public KnowledgeTaxonomyDto.Taxonomy taxonomy(Authentication auth) {
        accessService.actor(auth);
        return taxonomyService.taxonomy();
    }

    @PostMapping("/taxonomy/products")
    public KnowledgeTaxonomyDto.Product createProduct(@RequestBody KnowledgeTaxonomyDto.ProductRequest body, Authentication auth) {
        return taxonomyService.saveProduct(publisher(auth), null, body);
    }

    @PutMapping("/taxonomy/products/{id}")
    public KnowledgeTaxonomyDto.Product updateProduct(@PathVariable("id") Long id,
                                                      @RequestBody KnowledgeTaxonomyDto.ProductRequest body, Authentication auth) {
        return taxonomyService.saveProduct(publisher(auth), id, body);
    }

    @DeleteMapping("/taxonomy/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id, Authentication auth) {
        taxonomyService.deleteProduct(publisher(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/taxonomy/modules")
    public KnowledgeTaxonomyDto.Module createModule(@RequestBody KnowledgeTaxonomyDto.ModuleRequest body, Authentication auth) {
        return taxonomyService.saveModule(publisher(auth), null, body);
    }

    @PutMapping("/taxonomy/modules/{id}")
    public KnowledgeTaxonomyDto.Module updateModule(@PathVariable("id") Long id,
                                                    @RequestBody KnowledgeTaxonomyDto.ModuleRequest body, Authentication auth) {
        return taxonomyService.saveModule(publisher(auth), id, body);
    }

    @DeleteMapping("/taxonomy/modules/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable("id") Long id, Authentication auth) {
        taxonomyService.deleteModule(publisher(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/taxonomy/categories")
    public KnowledgeTaxonomyDto.Category createCategory(@RequestBody KnowledgeTaxonomyDto.CategoryRequest body, Authentication auth) {
        return taxonomyService.saveCategory(publisher(auth), null, body);
    }

    @PutMapping("/taxonomy/categories/{id}")
    public KnowledgeTaxonomyDto.Category updateCategory(@PathVariable("id") Long id,
                                                        @RequestBody KnowledgeTaxonomyDto.CategoryRequest body, Authentication auth) {
        return taxonomyService.saveCategory(publisher(auth), id, body);
    }

    @DeleteMapping("/taxonomy/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id, Authentication auth) {
        taxonomyService.deleteCategory(publisher(auth), id);
        return ResponseEntity.noContent().build();
    }

    // ---- dashboard, analytics, search index ----------------------------------

    @GetMapping("/dashboard")
    public KnowledgeAnalyticsDto.Dashboard dashboard(Authentication auth) {
        accessService.actor(auth);
        return analyticsService.dashboard();
    }

    @GetMapping("/analytics")
    public KnowledgeAnalyticsDto.Analytics analytics(@RequestParam(value = "days", defaultValue = "30") int days,
                                                     Authentication auth) {
        accessService.actor(auth);
        return analyticsService.analytics(days);
    }

    @GetMapping("/search-index")
    public KnowledgeSearchIndexService.Status searchIndex(Authentication auth) {
        publisher(auth);
        return searchIndexService.status();
    }

    @PostMapping("/search-index/reindex")
    public Map<String, Integer> reindexAll(Authentication auth) {
        return Map.of("items", searchIndexService.reindexAll(publisher(auth)));
    }

    @PostMapping("/search-index/reindex/{contentId}")
    public ResponseEntity<Void> reindexOne(@PathVariable("contentId") Long contentId, Authentication auth) {
        searchIndexService.reindexOne(publisher(auth), contentId);
        return ResponseEntity.noContent().build();
    }

    private KnowledgeActor publisher(Authentication auth) {
        KnowledgeActor actor = accessService.actor(auth);
        accessService.requirePublisher(actor);
        return actor;
    }
}
