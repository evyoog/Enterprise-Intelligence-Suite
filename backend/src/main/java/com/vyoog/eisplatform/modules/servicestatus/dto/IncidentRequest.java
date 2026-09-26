package com.vyoog.eisplatform.modules.servicestatus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Post or update an incident. {@code endedAt} null = still open. */
public record IncidentRequest(
    @NotNull Long productId,
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 4000) String message,
    @NotNull Instant startedAt,
    Instant endedAt
) {
}
