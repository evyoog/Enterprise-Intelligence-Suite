package com.vyoog.eisplatform.modules.search.controller;

import com.vyoog.eisplatform.modules.search.dto.SearchIndexStatusDto;
import com.vyoog.eisplatform.modules.search.dto.SearchInsightsDto;
import com.vyoog.eisplatform.modules.search.dto.SynonymDto;
import com.vyoog.eisplatform.modules.search.dto.SynonymRequest;
import com.vyoog.eisplatform.modules.search.service.SearchAdminService;
import com.vyoog.eisplatform.modules.search.service.SearchInsightsService;
import com.vyoog.eisplatform.modules.search.service.SynonymService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Search administration (C70): index status and rebuild, synonyms, insights.
 * {@code MANAGE_SEARCH}-gated in SecurityConfig. */
@RestController
@RequestMapping("/admin/search")
@RequiredArgsConstructor
public class AdminSearchController {

    private final SearchAdminService adminService;
    private final SynonymService synonymService;
    private final SearchInsightsService insightsService;

    @GetMapping("/index")
    public SearchIndexStatusDto status() {
        return adminService.status();
    }

    /** Rebuilds the whole index in the background; {@code reembed=true} also
     * embeds every passage again. Answers 409 if a rebuild is already running. */
    @PostMapping("/index/rebuild")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void rebuild(@RequestParam(value = "reembed", defaultValue = "false") boolean reembed) {
        adminService.startRebuild(reembed);
    }

    @GetMapping("/synonyms")
    public List<SynonymDto> synonyms() {
        return synonymService.list();
    }

    @PostMapping("/synonyms")
    @ResponseStatus(HttpStatus.CREATED)
    public SynonymDto createSynonym(@Valid @RequestBody SynonymRequest request) {
        return synonymService.create(request.terms());
    }

    @DeleteMapping("/synonyms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSynonym(@PathVariable Long id) {
        synonymService.delete(id);
    }

    @GetMapping("/insights")
    public SearchInsightsDto insights(@RequestParam(value = "days", defaultValue = "30") int days) {
        return insightsService.insights(days);
    }
}
