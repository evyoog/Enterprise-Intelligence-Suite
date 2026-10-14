package com.vyoog.eisplatform.modules.billing.tax.calc;

import com.vyoog.eisplatform.modules.billing.tax.model.ProviderEnvironment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.ValidationStatus;

import java.util.Set;

/** BR-2: the engine and provider settings read ONCE at the start of a calculation and used to its end, including retries.
 * Changing the switch meanwhile does not affect a calculation already running. */
public record TaxModeSnapshot(
    TaxEngineType engine,
    int configVersion,
    String providerCode,
    String providerCompanyCode,
    ProviderEnvironment providerEnvironment,
    int timeoutSeconds,
    int maxAttempts,
    ValidationStatus validationStatus,
    Set<String> providerCountries,
    Set<String> providerCurrencies,
    Set<String> providerTransactionTypes
) {
}
