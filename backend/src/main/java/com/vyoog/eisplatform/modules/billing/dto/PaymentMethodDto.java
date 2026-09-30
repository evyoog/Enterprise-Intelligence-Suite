package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.billing.model.PaymentMethodType;

public record PaymentMethodDto(
    Long id,
    PaymentMethodType type,
    String network,
    String last4,
    Integer expiryMonth,
    Integer expiryYear,
    String cardType,
    String issuer,
    String upiMasked,
    boolean isDefault,
    boolean expired
) {
}
