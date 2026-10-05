package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.dto.QueryCountDto;
import com.vyoog.eisplatform.modules.search.dto.SearchInsightsDto;
import com.vyoog.eisplatform.modules.search.model.SearchQueryLog;
import com.vyoog.eisplatform.modules.search.repository.SearchQueryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Search insights (C70): what people search for, which searches find nothing,
 * how often semantic search was used and how fast search answered. Built from
 * searches run on the results page; no user identity is stored. Queries are
 * grouped by their folded form, so "Factura" and "factura" count together.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchInsightsService {

    private static final int TOP = 10;

    private final SearchQueryLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String query, int resultCount, String mode, boolean semanticUsed, long tookMs) {
        try {
            SearchQueryLog entry = new SearchQueryLog();
            entry.setQuery(query.length() > 200 ? query.substring(0, 200) : query);
            entry.setResultCount(resultCount);
            entry.setMode(mode);
            entry.setSemanticUsed(semanticUsed);
            entry.setTookMs((int) Math.min(Integer.MAX_VALUE, tookMs));
            entry.setSearchedAt(Instant.now());
            logRepository.save(entry);
        } catch (RuntimeException e) {
            log.warn("Could not record search for insights: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SearchInsightsDto insights(int days) {
        int window = Math.max(1, Math.min(days, 365));
        List<SearchQueryLog> entries = logRepository.findBySearchedAtAfter(Instant.now().minus(Duration.ofDays(window)));
        long total = entries.size();
        long zero = entries.stream().filter(e -> e.getResultCount() == 0).count();
        long semantic = entries.stream().filter(SearchQueryLog::isSemanticUsed).count();
        List<Integer> took = entries.stream().map(SearchQueryLog::getTookMs).sorted().toList();
        long average = took.isEmpty() ? 0 : Math.round(took.stream().mapToInt(Integer::intValue).average().orElse(0));
        long p95 = took.isEmpty() ? 0 : took.get(Math.min(took.size() - 1, (int) Math.ceil(took.size() * 0.95) - 1));
        return new SearchInsightsDto(window, total, zero, rate(zero, total), rate(semantic, total), average, p95,
            top(entries, e -> true), top(entries, e -> e.getResultCount() == 0));
    }

    private List<QueryCountDto> top(List<SearchQueryLog> entries, java.util.function.Predicate<SearchQueryLog> filter) {
        Map<String, List<SearchQueryLog>> byQuery = entries.stream().filter(filter)
            .collect(Collectors.groupingBy(e -> String.join(" ", TextFolding.tokens(TextFolding.fold(e.getQuery())))));
        return byQuery.entrySet().stream()
            .filter(e -> !e.getKey().isBlank())
            .map(e -> new QueryCountDto(mostRecentSpelling(e.getValue()), e.getValue().size()))
            .sorted(Comparator.comparingLong(QueryCountDto::count).reversed().thenComparing(QueryCountDto::query))
            .limit(TOP)
            .toList();
    }

    private String mostRecentSpelling(List<SearchQueryLog> group) {
        return group.stream().max(Comparator.comparing(SearchQueryLog::getSearchedAt))
            .map(SearchQueryLog::getQuery).map(String::trim).orElse("");
    }

    private static double rate(long part, long total) {
        return total == 0 ? 0 : Math.round(part * 1000.0 / total) / 1000.0;
    }

}
