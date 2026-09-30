package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.billing.model.PaymentMethodType;
import jakarta.validation.constraints.NotNull;

public record SetupPaymentMethodRequest(
    @NotNull PaymentMethodType type,
    boolean consent,
    boolean makeDefault
) {
}
