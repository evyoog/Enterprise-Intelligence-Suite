package com.vyoog.eisplatform.modules.administration.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFeatureFlagRequest(
    @NotNull Boolean enabled,
    @Size(max = 500) String description
) {
}
