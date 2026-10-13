package com.vyoog.eisplatform.modules.integration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/** {@code key} is set only in the create response (BR-1). {@code personal} marks a legacy key that belongs to a
 * person, not to an organization (C92). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiKeyDto(
    Long id,
    String name,
    String prefix,
    String status,
    String scope,
    boolean personal,
    String createdByName,
    Instant createdAt,
    Instant expiresAt,
    Instant lastUsedAt,
    long requestCount,
    String key
) {
}
