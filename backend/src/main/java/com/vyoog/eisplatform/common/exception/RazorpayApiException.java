package com.vyoog.eisplatform.common.exception;

/** Billing & Payments: Razorpay itself returned an error (network failure,
 * rejected request). Mapped to HTTP 502, code {@code PAYMENT_GATEWAY_ERROR}
 * — its message is passed through. */
public class RazorpayApiException extends RuntimeException {
    public RazorpayApiException(String message) {
        super(message);
    }

    public RazorpayApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
