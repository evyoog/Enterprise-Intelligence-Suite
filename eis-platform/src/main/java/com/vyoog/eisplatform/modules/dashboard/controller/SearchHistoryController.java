package com.vyoog.eisplatform.modules.dashboard.controller;

import com.vyoog.eisplatform.modules.dashboard.dto.RecordSearchRequest;
import com.vyoog.eisplatform.modules.dashboard.dto.SearchHistoryEntryDto;
import com.vyoog.eisplatform.modules.dashboard.service.SearchHistoryService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Covered by SecurityConfig's existing {@code /me/**} authenticated() rule —
 * same as DashboardController, no new rule needed. */
@RestController
@RequestMapping("/me/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public List<SearchHistoryEntryDto> listRecent(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return searchHistoryService.listRecent(customer.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordSearch(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RecordSearchRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        searchHistoryService.recordSearch(customer.getId(), request.query());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearHistory(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        searchHistoryService.clearHistory(customer.getId());
    }
}
