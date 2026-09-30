package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

public record WebhookEventDto(String eventType, Instant receivedAt) {
}
