package com.vyoog.eisplatform.modules.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record PlatformCreateRequest(
    @NotBlank String name,
    String description,
    String imageUrl
) {
}
