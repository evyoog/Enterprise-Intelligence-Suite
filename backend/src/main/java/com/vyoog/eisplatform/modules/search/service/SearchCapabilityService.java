package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.repository.SearchCapabilities;
import com.vyoog.eisplatform.modules.search.repository.SearchSqlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Whether the PostgreSQL search index is installed (C70). Checked once and
 * cached; an admin "Rebuild index" checks again, so applying V020 by hand
 * takes effect without a restart. Without the index, search uses the basic
 * engine (the matching that existed before C70).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchCapabilityService {

    private final SearchSqlRepository sqlRepository;
    private volatile SearchCapabilities capabilities;

    public SearchCapabilities get() {
        SearchCapabilities current = capabilities;
        if (current == null) {
            current = refresh();
        }
        return current;
    }

    public SearchCapabilities refresh() {
        SearchCapabilities detected = sqlRepository.detectCapabilities();
        if (!detected.equals(capabilities)) {
            log.info("Search index: keyword={}, semantic={}", detected.keywordIndex(), detected.semantic());
        }
        capabilities = detected;
        return detected;
    }
}
