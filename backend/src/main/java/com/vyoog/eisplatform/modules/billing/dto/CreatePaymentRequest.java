package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.Pattern;

/** Optional body of "start a payment" (C59, REQ-BIL-001.22): a saved method
 * the customer chose at checkout, and the method tile used for the Razorpay
 * Checkout preselection. Never carries card data (BR-BIL-001). */
public record CreatePaymentRequest(
    Long paymentMethodId,
    @Pattern(regexp = "card|upi|netbanking|wallet", message = "must be card, upi, netbanking or wallet") String method
) {
}
