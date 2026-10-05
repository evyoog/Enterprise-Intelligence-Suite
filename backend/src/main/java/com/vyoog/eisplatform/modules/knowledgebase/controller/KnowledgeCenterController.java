package com.vyoog.eisplatform.modules.knowledgebase.controller;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeItemDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePageDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeSummaryDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAccessService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAnalyticsService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAssistantService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeReader;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeReaderService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeVideoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Knowledge Center reader API (REQ-KNW-005). Public at the URL level; the
 * caller's own token (if any) decides what they may see (BR-KVS-001).
 * Hidden content answers 404 exactly like missing content.
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeCenterController {

    private final KnowledgeAccessService accessService;
    private final KnowledgeReaderService readerService;
    private final KnowledgeVideoService videoService;
    private final KnowledgeAnalyticsService analyticsService;
    private final KnowledgeAssistantService assistantService;

    @GetMapping("/home")
    public KnowledgeReaderDtos.Home home(Authentication auth) {
        return readerService.home(reader(auth));
    }

    @GetMapping("/content")
    public KnowledgePageDto<KnowledgeSummaryDto> list(@RequestParam(value = "type", required = false) List<KnowledgeContentType> types,
                                                      @RequestParam(value = "product", required = false) String product,
                                                      @RequestParam(value = "module", required = false) Long module,
                                                      @RequestParam(value = "category", required = false) Long category,
                                                      @RequestParam(value = "tag", required = false) String tag,
                                                      @RequestParam(value = "q", required = false) String q,
                                                      @RequestParam(value = "sort", required = false) String sort,
                                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                                      @RequestParam(value = "size", defaultValue = "24") int size,
                                                      Authentication auth) {
        return readerService.list(reader(auth), types, product, module, category, tag, q, sort, page, size);
    }

    @GetMapping("/content/{idOrSlug}")
    public KnowledgeItemDto get(@PathVariable("idOrSlug") String idOrSlug, Authentication auth, HttpServletRequest request) {
        KnowledgeReader reader = reader(auth);
        return readerService.get(reader, idOrSlug, viewerKey(reader, request));
    }

    @GetMapping("/products")
    public List<KnowledgeReaderDtos.ProductCard> products(Authentication auth) {
        return readerService.products(reader(auth));
    }

    @GetMapping("/products/{slug}")
    public KnowledgeReaderDtos.ProductHub hub(@PathVariable("slug") String slug, Authentication auth) {
        return readerService.hub(reader(auth), slug);
    }

    @GetMapping("/workflows")
    public List<KnowledgeItemDto> workflows(Authentication auth) {
        return readerService.workflows(reader(auth));
    }

    @GetMapping("/faqs")
    public List<KnowledgeItemDto> faqs(@RequestParam(value = "product", required = false) String product,
                                       @RequestParam(value = "module", required = false) Long module, Authentication auth) {
        return readerService.faqs(reader(auth), product, module);
    }

    @GetMapping("/error-codes/{code}")
    public KnowledgeItemDto errorCode(@PathVariable("code") String code, Authentication auth, HttpServletRequest request) {
        KnowledgeReader reader = reader(auth);
        return readerService.errorCode(reader, code, viewerKey(reader, request));
    }

    @GetMapping("/glossary")
    public List<KnowledgeReaderDtos.GlossaryTerm> glossary(Authentication auth) {
        return readerService.glossary(reader(auth));
    }

    @GetMapping("/release-notes")
    public List<KnowledgeItemDto> releaseNotes(@RequestParam(value = "product", required = false) String product,
                                               Authentication auth) {
        return readerService.releaseNotes(reader(auth), product);
    }

    @GetMapping("/search")
    public KnowledgeReaderDtos.SearchResult search(@RequestParam(value = "q", required = false) String q,
                                                   @RequestParam(value = "type", required = false) KnowledgeContentType type,
                                                   Authentication auth) {
        return readerService.search(reader(auth), q, type);
    }

    /** BR-KVID-003: authenticate (optional) → authorize → temporary URL. */
    @GetMapping("/videos/{id}/play-url")
    public KnowledgeVideoDtos.PlayInfo playUrl(@PathVariable("id") Long id, Authentication auth) {
        return videoService.playInfo(reader(auth), id);
    }

    @GetMapping("/media/{id}/download-url")
    public KnowledgeMediaDtos.TemporaryUrl downloadUrl(@PathVariable("id") Long id, Authentication auth,
                                                       HttpServletRequest request) {
        KnowledgeReader reader = reader(auth);
        return readerService.downloadUrl(reader, id, viewerKey(reader, request));
    }

    @PostMapping("/content/{id}/feedback")
    public ResponseEntity<Void> feedback(@PathVariable("id") Long id, @RequestBody KnowledgeReaderDtos.FeedbackRequest body,
                                         Authentication auth) {
        analyticsService.feedback(reader(auth), id, body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events")
    public ResponseEntity<Void> event(@RequestBody KnowledgeReaderDtos.EventRequest body, Authentication auth,
                                      HttpServletRequest request) {
        KnowledgeReader reader = reader(auth);
        analyticsService.recordClientEvent(reader, body, viewerKey(reader, request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assistant/status")
    public KnowledgeReaderDtos.AssistantStatus assistantStatus() {
        return assistantService.status();
    }

    @PostMapping("/assistant/ask")
    public void ask(@RequestBody KnowledgeReaderDtos.AssistantRequest body) {
        assistantService.ask(body);
    }

    private KnowledgeReader reader(Authentication auth) {
        return accessService.reader(auth);
    }

    /** Signed in: the token subject; signed out: address + browser (hashed before storing). */
    static String viewerKey(KnowledgeReader reader, HttpServletRequest request) {
        if (reader.keycloakSub() != null) {
            return "sub:" + reader.keycloakSub();
        }
        String agent = request.getHeader("User-Agent");
        return "anon:" + request.getRemoteAddr() + "|" + (agent == null ? "" : agent);
    }
}
