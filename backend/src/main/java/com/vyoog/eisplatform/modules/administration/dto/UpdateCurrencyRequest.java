package com.vyoog.eisplatform.modules.administration.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCurrencyRequest(@NotNull Boolean enabled) {
}
