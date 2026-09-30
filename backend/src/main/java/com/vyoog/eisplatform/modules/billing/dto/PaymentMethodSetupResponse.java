package com.vyoog.eisplatform.modules.billing.dto;

/** What Razorpay Checkout needs to run its own tokenizing flow (a small
 * verification order — see PaymentMethodService's own javadoc for why this
 * MVP doesn't call Razorpay's separate Customer/Token API). */
public record PaymentMethodSetupResponse(
    String providerOrderId,
    long amount,
    String currency,
    String keyId
) {
}
