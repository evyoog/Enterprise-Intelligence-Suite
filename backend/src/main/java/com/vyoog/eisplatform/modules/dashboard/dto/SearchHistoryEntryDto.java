package com.vyoog.eisplatform.modules.dashboard.dto;

import java.time.Instant;

public record SearchHistoryEntryDto(String query, Instant searchedAt) {
}
