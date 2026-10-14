package com.vyoog.eisplatform.common.exception;

/** Billing & Payments: Razorpay itself returned an error (network failure,
 * rejected request). Mapped to HTTP 502, code {@code PAYMENT_GATEWAY_ERROR}
 * — its message is passed through. {@code httpStatus} is the provider's HTTP
 * status when it answered at all (C98: 401 or 403 means the credentials were
 * refused), null for a network failure. */
public class RazorpayApiException extends RuntimeException {
    private final Integer httpStatus;

    public RazorpayApiException(String message) {
        super(message);
        this.httpStatus = null;
    }

    public RazorpayApiException(String message, Integer httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public RazorpayApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}
