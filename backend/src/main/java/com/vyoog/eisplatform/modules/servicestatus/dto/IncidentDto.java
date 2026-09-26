package com.vyoog.eisplatform.modules.servicestatus.dto;

import java.time.Instant;

public record IncidentDto(
    Long id,
    Long productId,
    String productName,
    String title,
    String message,
    Instant startedAt,
    Instant endedAt,
    boolean open
) {
}
