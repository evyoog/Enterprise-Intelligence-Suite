package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;

public record SetActiveGatewayRequest(@NotBlank String code) {
}
