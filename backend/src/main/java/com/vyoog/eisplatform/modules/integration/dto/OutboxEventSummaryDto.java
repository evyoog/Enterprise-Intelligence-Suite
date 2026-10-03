package com.vyoog.eisplatform.modules.integration.dto;

import java.time.Instant;

public record OutboxEventSummaryDto(
    Long id,
    String eventId,
    String eventType,
    String aggregateType,
    String aggregateId,
    Instant occurredAt,
    String status,
    int attempts,
    Instant nextAttemptAt,
    String lastError
) {
}
