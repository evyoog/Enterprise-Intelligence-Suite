package com.vyoog.eisplatform.modules.administration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRegionRequest(
    @NotBlank @Size(max = 150) String name,
    @NotNull Boolean enabled
) {
}
