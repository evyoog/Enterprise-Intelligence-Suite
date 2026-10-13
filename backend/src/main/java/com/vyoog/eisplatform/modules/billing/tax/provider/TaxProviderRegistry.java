package com.vyoog.eisplatform.modules.billing.tax.provider;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** The adapters available for the provider code stored in the settings. */
@Component
public class TaxProviderRegistry {

    private final List<TaxProvider> providers;

    public TaxProviderRegistry(List<TaxProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public List<TaxProvider> all() {
        return providers;
    }

    public Optional<TaxProvider> find(String code) {
        return code == null ? Optional.empty() : providers.stream().filter(p -> p.code().equals(code)).findFirst();
    }
}
