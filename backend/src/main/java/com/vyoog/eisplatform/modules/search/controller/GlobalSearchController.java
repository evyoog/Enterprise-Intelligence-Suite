package com.vyoog.eisplatform.modules.search.controller;

import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.search.dto.GlobalSearchResultDto;
import com.vyoog.eisplatform.modules.search.service.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 01.03.01 Unified Search (sprint 2027.1.3) — public for products/knowledge
 * articles; ticket results only appear when the caller has a linked
 * Customer row (see CurrentCustomerResolver's own javadoc on why that's an
 * expected, non-error case here, unlike a real /me/** endpoint). */
@RestController
@RequiredArgsConstructor
public class GlobalSearchController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final GlobalSearchService globalSearchService;

    @GetMapping("/search")
    public GlobalSearchResultDto search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "type", required = false) String type,
            @AuthenticationPrincipal Jwt jwt) {
        Long customerId = jwt == null ? null
            : currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null);
        return globalSearchService.search(query, type, customerId);
    }
}
