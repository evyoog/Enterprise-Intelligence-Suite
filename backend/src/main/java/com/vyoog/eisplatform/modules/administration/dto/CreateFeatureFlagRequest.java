package com.vyoog.eisplatform.modules.administration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFeatureFlagRequest(
    @NotBlank @Size(max = 100) String flagKey,
    boolean enabled,
    @Size(max = 500) String description
) {
}
