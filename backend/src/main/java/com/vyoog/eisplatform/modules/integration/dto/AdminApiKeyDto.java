package com.vyoog.eisplatform.modules.integration.dto;

import java.time.Instant;

public record AdminApiKeyDto(
    Long id,
    Long ownerCustomerId,
    String ownerEmail,
    Long organizationId,
    String organizationName,
    String scope,
    String name,
    String prefix,
    String status,
    Instant createdAt,
    Instant expiresAt,
    Instant lastUsedAt,
    long requestCount
) {
}
