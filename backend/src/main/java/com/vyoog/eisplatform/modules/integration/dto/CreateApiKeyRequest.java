package com.vyoog.eisplatform.modules.integration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** {@code scope} is READ or READ_WRITE (C92, API-4 B); missing means READ, the least privilege. */
public record CreateApiKeyRequest(
    @NotBlank @Size(max = 100) String name,
    Instant expiresAt,
    String scope
) {
}
