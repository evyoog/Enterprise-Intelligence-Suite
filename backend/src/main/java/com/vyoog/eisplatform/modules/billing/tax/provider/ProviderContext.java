package com.vyoog.eisplatform.modules.billing.tax.provider;

import com.vyoog.eisplatform.modules.billing.tax.model.ProviderEnvironment;

import java.time.Duration;

/** The non-secret provider settings an adapter receives with every call. */
public record ProviderContext(String companyCode, ProviderEnvironment environment, Duration timeout) {
}
