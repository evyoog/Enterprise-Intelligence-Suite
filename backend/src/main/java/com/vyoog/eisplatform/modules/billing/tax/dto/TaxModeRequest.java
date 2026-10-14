package com.vyoog.eisplatform.modules.billing.tax.dto;

import jakarta.validation.constraints.NotNull;

public record TaxModeRequest(@NotNull Boolean externalEngineEnabled) {
}
