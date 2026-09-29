package com.vyoog.eisplatform.modules.administration.service;

import com.vyoog.eisplatform.modules.administration.model.PlatformCurrency;
import com.vyoog.eisplatform.modules.administration.model.PlatformFeatureFlag;
import com.vyoog.eisplatform.modules.administration.repository.PlatformCurrencyRepository;
import com.vyoog.eisplatform.modules.administration.repository.PlatformFeatureFlagRepository;
import com.vyoog.eisplatform.modules.product.model.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Seeds the fixed currency set (mirroring {@link Currency}, see
 * PlatformCurrency's own javadoc) and the "groups_enabled" feature flag
 * (05.04.01 Groups, sprint 2026.4.1, gated starting sprint 2026.4.2 — see
 * OrganizationSelfService#requireGroupsEnabled), on every startup,
 * idempotently — same pattern as {@link com.vyoog.eisplatform.modules.authorization.service.RbacSeeder}.
 */
@Component
@RequiredArgsConstructor
public class PlatformAdministrationSeeder implements ApplicationRunner {

    private final PlatformCurrencyRepository currencyRepository;
    private final PlatformFeatureFlagRepository flagRepository;

    private static final Map<String, String> CURRENCY_NAMES = Map.of(
        "USD", "US Dollar", "EUR", "Euro", "GBP", "British Pound", "INR", "Indian Rupee"
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Currency currency : Currency.values()) {
            if (currencyRepository.existsById(currency.name())) continue;
            PlatformCurrency row = new PlatformCurrency();
            row.setCode(currency.name());
            row.setName(CURRENCY_NAMES.getOrDefault(currency.name(), currency.name()));
            row.setEnabled(true);
            currencyRepository.save(row);
        }

        if (!flagRepository.existsById("groups_enabled")) {
            PlatformFeatureFlag flag = new PlatformFeatureFlag();
            flag.setFlagKey("groups_enabled");
            flag.setEnabled(true);
            flag.setDescription("05.04.01 Groups — organization admins can create groups and add/remove members.");
            flagRepository.save(flag);
        }
    }
}
