package com.vyoog.eisplatform.common.exception;

import java.util.List;

/** Cart and checkout (REQ-MKT-003, C59): a checkout refused because the cart
 * is empty ({@code CART_EMPTY}) or an item has a purchase-validation issue
 * ({@code CART_INVALID}, with the same {@code issues} the validate endpoint
 * returns). Mapped to HTTP 409. */
public class CartConflictException extends RuntimeException {

    private final String code;
    private final List<?> issues;

    public CartConflictException(String code, String message, List<?> issues) {
        super(message);
        this.code = code;
        this.issues = issues;
    }

    public String getCode() {
        return code;
    }

    public List<?> getIssues() {
        return issues;
    }
}
