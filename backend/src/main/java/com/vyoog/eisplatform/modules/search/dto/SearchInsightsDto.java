package com.vyoog.eisplatform.modules.search.dto;

import java.util.List;

/**
 * Search insights for the last {@code days} days (C70), from searches run on
 * the results page.
 */
public record SearchInsightsDto(
    int days,
    long totalSearches,
    long zeroResultSearches,
    double zeroResultRate,
    double semanticUsedRate,
    long averageTookMs,
    long p95TookMs,
    List<QueryCountDto> topQueries,
    List<QueryCountDto> topZeroResultQueries
) {
}
