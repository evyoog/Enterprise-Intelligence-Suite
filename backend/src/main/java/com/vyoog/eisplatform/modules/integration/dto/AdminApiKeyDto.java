package com.vyoog.eisplatform.modules.integration.dto;

import java.time.Instant;

public record AdminApiKeyDto(
    Long id,
    Long ownerCustomerId,
    String ownerEmail,
    String name,
    String prefix,
    String status,
    Instant createdAt,
    Instant expiresAt,
    Instant lastUsedAt,
    long requestCount
) {
}
