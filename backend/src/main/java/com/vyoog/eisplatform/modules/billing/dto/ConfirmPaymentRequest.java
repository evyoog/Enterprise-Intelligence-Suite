package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentRequest(
    @NotBlank String providerOrderId,
    @NotBlank String providerPaymentId,
    @NotBlank String signature
) {
}
