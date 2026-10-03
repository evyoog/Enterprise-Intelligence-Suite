package com.vyoog.eisplatform.modules.integration.service;

import java.time.Instant;

/** REQ-INT-002: the read-only view of an outbox event that a handler receives. */
public record PlatformEvent(
    String eventId,
    String eventType,
    String aggregateType,
    String aggregateId,
    Instant occurredAt,
    String payload
) {
}
