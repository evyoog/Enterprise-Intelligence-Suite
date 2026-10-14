package com.vyoog.eisplatform.common.exception;

/** BR-10 (Billing & Payments): thrown by a billing service method before any
 * Razorpay-dependent action, when a credential is missing. Mapped to HTTP
 * 503, code {@code PAYMENT_GATEWAY_NOT_CONFIGURED}. */
public class PaymentGatewayNotConfiguredException extends RuntimeException {
    public PaymentGatewayNotConfiguredException() {
        super("Online payments are not available yet.");
    }

    /** C98: no eligible gateway for this payment (none active and configured for the currency and method). */
    public PaymentGatewayNotConfiguredException(String message) {
        super(message);
    }
}
