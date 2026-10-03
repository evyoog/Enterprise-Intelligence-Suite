package com.vyoog.eisplatform.common.exception;

/** An action that the resource's current state does not allow (for example
 * retrying an event that is not FAILED). Mapped to HTTP 409, code
 * {@code INVALID_STATE}. */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) {
        super(message);
    }
}
