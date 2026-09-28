package com.vyoog.eisplatform.modules.partner.dto;

import com.vyoog.eisplatform.modules.partner.model.ProviderStatus;

import java.time.Instant;

public record ProviderDto(
    Long id,
    String name,
    String contactName,
    String contactEmail,
    String description,
    ProviderStatus status,
    Instant createdAt
) {
}
