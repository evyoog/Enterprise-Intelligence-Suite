package com.vyoog.eisplatform.modules.search.controller;

import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.search.dto.GlobalSearchResultDto;
import com.vyoog.eisplatform.modules.search.dto.SearchSuggestionDto;
import com.vyoog.eisplatform.modules.search.service.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 01.03.01 Unified Search (REQ-PRT-002/003, C70) — public for products and
 * knowledge articles; ticket results only appear when the caller has a
 * linked Customer row (see CurrentCustomerResolver's own javadoc on why
 * that's an expected, non-error case here, unlike a real /me/** endpoint). */
@RestController
@RequiredArgsConstructor
public class GlobalSearchController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final GlobalSearchService globalSearchService;

    /**
     * @param mode  "hybrid" (default: keyword + semantic) or "keyword"
     * @param limit maximum results (1–100; default from settings)
     * @param track true when the search was run from the results page; it is
     *              then counted in search insights
     */
    @GetMapping("/search")
    public GlobalSearchResultDto search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "track", defaultValue = "false") boolean track,
            @AuthenticationPrincipal Jwt jwt) {
        GlobalSearchService.Mode searchMode = "keyword".equalsIgnoreCase(mode)
            ? GlobalSearchService.Mode.KEYWORD : GlobalSearchService.Mode.HYBRID;
        return globalSearchService.search(query, type, customerId(jwt), searchMode, limit, track);
    }

    @GetMapping("/search/suggest")
    public List<SearchSuggestionDto> suggest(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "type", required = false) String type,
            @AuthenticationPrincipal Jwt jwt) {
        return globalSearchService.suggest(query, type, customerId(jwt));
    }

    private Long customerId(Jwt jwt) {
        return jwt == null ? null
            : currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null);
    }
}
