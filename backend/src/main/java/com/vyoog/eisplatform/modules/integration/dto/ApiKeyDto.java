package com.vyoog.eisplatform.modules.integration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/** {@code key} is set only in the create response (BR-1). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiKeyDto(
    Long id,
    String name,
    String prefix,
    String status,
    Instant createdAt,
    Instant expiresAt,
    Instant lastUsedAt,
    long requestCount,
    String key
) {
}
