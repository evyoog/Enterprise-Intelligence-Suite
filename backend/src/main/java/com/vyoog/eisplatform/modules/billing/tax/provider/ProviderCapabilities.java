package com.vyoog.eisplatform.modules.billing.tax.provider;

import java.util.Set;

/** What a provider says it supports (REQ-BIL-002.33). Codes are upper case. */
public record ProviderCapabilities(String providerName, Set<String> countries, Set<String> currencies, Set<String> transactionTypes) {
    public boolean supportsCountry(String country) {
        return countries.contains(country);
    }

    public boolean supportsCurrency(String currency) {
        return currencies.contains(currency);
    }

    public boolean supportsTransactionType(String type) {
        return transactionTypes.contains(type);
    }
}
