package com.vyoog.eisplatform.modules.billing.dto;

/** What the frontend needs to open Razorpay Checkout for an invoice
 * (REQ-BIL-001.5). {@code paymentId} is EIS's own payment row id — the
 * frontend echoes it back in {@link ConfirmPaymentRequest}. */
public record CreatePaymentResponse(
    Long paymentId,
    String providerOrderId,
    long amount,
    String currency,
    String keyId
) {
}
