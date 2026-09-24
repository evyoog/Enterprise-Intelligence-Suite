package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.dashboard.dto.SearchHistoryEntryDto;
import com.vyoog.eisplatform.modules.dashboard.model.SearchHistoryEntry;
import com.vyoog.eisplatform.modules.dashboard.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Phase 17: recorded explicitly by the frontend after a real search (see
 * ProductController#searchProducts, which stays customer-unaware — search
 * itself is a public, stateless catalog query; only this side-effect needs
 * an identity, kept here rather than in the product module the same way
 * favorites/usage already live here rather than in modules.product).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    public List<SearchHistoryEntryDto> listRecent(Long customerId) {
        return searchHistoryRepository.findTop10ByCustomerIdOrderBySearchedAtDesc(customerId).stream()
            .map(e -> new SearchHistoryEntryDto(e.getQuery(), e.getSearchedAt()))
            .toList();
    }

    /** Re-searching the same term moves it back to the top rather than
     * appearing twice — the existing row (case-insensitive) is replaced, not
     * duplicated. */
    @Transactional
    public void recordSearch(Long customerId, String query) {
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        searchHistoryRepository.deleteByCustomerIdAndQueryIgnoreCase(customerId, trimmed);

        SearchHistoryEntry entry = new SearchHistoryEntry();
        entry.setCustomerId(customerId);
        entry.setQuery(trimmed);
        searchHistoryRepository.save(entry);
    }

    @Transactional
    public void clearHistory(Long customerId) {
        searchHistoryRepository.deleteByCustomerId(customerId);
    }
}
