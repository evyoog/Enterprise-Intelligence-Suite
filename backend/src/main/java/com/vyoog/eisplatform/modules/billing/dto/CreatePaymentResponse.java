package com.vyoog.eisplatform.modules.billing.dto;

import java.util.Map;

/** What the frontend needs to open the chosen provider's payment window for an invoice (REQ-BIL-001.5, C98). {@code paymentId} is EIS's own
 * payment row id — the frontend echoes it back in {@link ConfirmPaymentRequest}. {@code provider} says which gateway the backend routed this
 * payment to; {@code keyId} is that provider's public key (kept for Razorpay Checkout); {@code clientConfig} is everything public the browser
 * needs for any provider. Never a secret. */
public record CreatePaymentResponse(
    Long paymentId,
    String providerOrderId,
    long amount,
    String currency,
    String keyId,
    String provider,
    Map<String, String> clientConfig
) {
}
