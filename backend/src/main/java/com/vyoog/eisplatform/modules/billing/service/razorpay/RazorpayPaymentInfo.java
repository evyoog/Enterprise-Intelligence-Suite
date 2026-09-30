package com.vyoog.eisplatform.modules.billing.service.razorpay;

/** Display-only fields read back from Razorpay after a payment — never a
 * card number or CVV (BR-BIL-001). {@code status} is Razorpay's own payment
 * status string ("captured", "failed", ...). */
public record RazorpayPaymentInfo(
    String paymentId,
    String status,
    String methodType,
    String methodNetwork,
    String methodLast4,
    String failureReason
) {
}
