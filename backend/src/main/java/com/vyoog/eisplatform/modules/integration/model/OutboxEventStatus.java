package com.vyoog.eisplatform.modules.integration.model;

/** REQ-INT-002.2: an outbox event's delivery state. */
public enum OutboxEventStatus {
    PENDING,
    DELIVERED,
    FAILED
}
