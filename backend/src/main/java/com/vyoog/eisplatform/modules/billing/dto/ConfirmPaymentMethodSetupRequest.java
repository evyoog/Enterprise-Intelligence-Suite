package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.billing.model.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentMethodSetupRequest(
    @NotBlank String providerOrderId,
    @NotBlank String providerPaymentId,
    @NotBlank String signature,
    @NotNull PaymentMethodType type,
    boolean consent,
    boolean makeDefault
) {
}
