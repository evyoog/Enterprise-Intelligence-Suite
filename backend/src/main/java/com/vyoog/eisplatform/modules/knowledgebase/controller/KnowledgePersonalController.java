package com.vyoog.eisplatform.modules.knowledgebase.controller;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeSummaryDto;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAccessService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgePersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Personal learning panel (REQ-KNW-005.8) — the caller's own data only (BR-KCEN-004). */
@RestController
@RequestMapping("/me/knowledge")
@RequiredArgsConstructor
public class KnowledgePersonalController {

    private final KnowledgeAccessService accessService;
    private final KnowledgePersonalService personalService;

    @GetMapping
    public KnowledgeReaderDtos.Personal personal(Authentication auth) {
        return personalService.personal(accessService.reader(auth));
    }

    @GetMapping("/bookmarks")
    public List<KnowledgeSummaryDto> bookmarks(Authentication auth) {
        return personalService.bookmarks(accessService.reader(auth));
    }

    @PutMapping("/bookmarks/{contentId}")
    public ResponseEntity<Void> bookmark(@PathVariable("contentId") Long contentId, Authentication auth) {
        personalService.addBookmark(accessService.reader(auth), contentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bookmarks/{contentId}")
    public ResponseEntity<Void> unbookmark(@PathVariable("contentId") Long contentId, Authentication auth) {
        personalService.removeBookmark(accessService.reader(auth), contentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/progress/{contentId}")
    public ResponseEntity<Void> progress(@PathVariable("contentId") Long contentId,
                                         @RequestBody KnowledgeReaderDtos.ProgressRequest body, Authentication auth) {
        personalService.saveProgress(accessService.reader(auth), contentId, body);
        return ResponseEntity.noContent().build();
    }
}
