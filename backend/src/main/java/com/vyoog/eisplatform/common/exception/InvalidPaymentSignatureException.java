package com.vyoog.eisplatform.common.exception;

/** Billing & Payments, BR-5: a checkout or webhook result whose signature
 * does not verify — the caller changes nothing. Mapped to HTTP 400, code
 * {@code SIGNATURE_INVALID}. */
public class InvalidPaymentSignatureException extends RuntimeException {
    public InvalidPaymentSignatureException() {
        super("The payment result could not be verified.");
    }
}
