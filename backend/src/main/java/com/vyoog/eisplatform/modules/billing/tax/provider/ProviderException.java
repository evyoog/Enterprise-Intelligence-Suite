package com.vyoog.eisplatform.modules.billing.tax.provider;

/** A provider call failed. {@code isTransient()} means a retry may succeed (network error, timeout, 429, 5xx); anything else
 * is permanent and is not retried (BR-34). The message never contains a credential. */
public class ProviderException extends Exception {

    private final boolean transientFailure;
    private final boolean invalidResponse;

    public ProviderException(String message, boolean transientFailure, boolean invalidResponse, Throwable cause) {
        super(message, cause);
        this.transientFailure = transientFailure;
        this.invalidResponse = invalidResponse;
    }

    public static ProviderException transientFailure(String message, Throwable cause) {
        return new ProviderException(message, true, false, cause);
    }

    public static ProviderException permanent(String message) {
        return new ProviderException(message, false, false, null);
    }

    public static ProviderException invalidResponse(String message, Throwable cause) {
        return new ProviderException(message, false, true, cause);
    }

    public boolean isTransient() {
        return transientFailure;
    }

    public boolean isInvalidResponse() {
        return invalidResponse;
    }
}
