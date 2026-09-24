package com.vyoog.eisplatform.modules.dashboard.dto;

import jakarta.validation.constraints.NotBlank;

public record RecordSearchRequest(@NotBlank String query) {
}
