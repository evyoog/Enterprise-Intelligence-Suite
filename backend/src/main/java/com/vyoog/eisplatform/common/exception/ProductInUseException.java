package com.vyoog.eisplatform.common.exception;

/** Thrown when deleting a Product would orphan real subscription/access/usage
 * history — see ProductService#deleteProduct. */
public class ProductInUseException extends RuntimeException {

    public ProductInUseException(String message) {
        super(message);
    }
}
