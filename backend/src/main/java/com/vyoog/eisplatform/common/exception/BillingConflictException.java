package com.vyoog.eisplatform.common.exception;

/** Billing & Payments: paying a non-OPEN invoice, refunding more than
 * refundable, defaulting an expired card. Mapped to HTTP 409, code
 * {@code INVALID_STATE}. */
public class BillingConflictException extends RuntimeException {
    public BillingConflictException(String message) {
        super(message);
    }
}
