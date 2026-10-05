package com.vyoog.eisplatform.modules.search.dto;

import java.time.Instant;
import java.util.List;

public record SynonymDto(Long id, List<String> terms, Instant createdAt) {
}
