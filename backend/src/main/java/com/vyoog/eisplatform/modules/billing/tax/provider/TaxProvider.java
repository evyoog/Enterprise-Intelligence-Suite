package com.vyoog.eisplatform.modules.billing.tax.provider;

/** REQ-BIL-002.30: the only thing the billing module knows about an external tax provider. A new vendor is one new
 * implementation of this interface, registered as a Spring bean. Implementations read their credentials from the backend
 * environment (BR-30) and never return them. */
public interface TaxProvider {

    /** Stable code stored in the settings, for example {@code GENERIC_REST}. */
    String code();

    String displayName();

    /** True when the backend environment holds everything this provider needs to make a call. */
    boolean credentialsPresent();

    /** Each credential the provider needs and whether it is set. Never the values. */
    java.util.Map<String, Boolean> credentialStatus();

    /** The provider's host only (for example {@code tax.example.com}), or null. */
    String maskedEndpoint();

    /** Calls the provider to prove the connection and the credentials work, and returns what it supports. */
    ProviderCapabilities testConnection(ProviderContext context) throws ProviderException;

    /** One calculation. The adapter performs exactly one attempt; retries are the engine's job (BR-34). */
    ProviderTaxResponse calculate(ProviderContext context, ProviderTaxRequest request) throws ProviderException;
}
