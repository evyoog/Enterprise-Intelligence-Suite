package com.vyoog.eisplatform.modules.integration.dto;

import java.time.Instant;
import java.util.List;

public record OutboxEventDetailDto(
    Long id,
    String eventId,
    String eventType,
    String aggregateType,
    String aggregateId,
    Instant occurredAt,
    String status,
    int attempts,
    Instant nextAttemptAt,
    String lastError,
    Instant deliveredAt,
    String payload,
    List<EventReceiptDto> receipts
) {
}
