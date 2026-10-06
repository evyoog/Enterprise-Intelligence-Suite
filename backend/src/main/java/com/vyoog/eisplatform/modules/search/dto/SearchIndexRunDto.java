package com.vyoog.eisplatform.modules.search.dto;

import java.time.Instant;

public record SearchIndexRunDto(
    Long id,
    String trigger,
    String status,
    int documents,
    int chunks,
    int embedded,
    String error,
    Instant startedAt,
    Instant finishedAt
) {
}
