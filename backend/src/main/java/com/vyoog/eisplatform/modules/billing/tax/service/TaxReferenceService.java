package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxReferenceDto;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import com.vyoog.eisplatform.modules.billing.tax.support.StateCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reference lists for the Tax Configuration forms. Contains no rates (BR-10). */
@Service
@RequiredArgsConstructor
public class TaxReferenceService {

    private final BillingSettingsService billingSettingsService;

    public TaxReferenceDto reference() {
        List<TaxReferenceDto.Country> countries = Arrays.stream(Locale.getISOCountries())
            .map(c -> new TaxReferenceDto.Country(c, CountryResolver.nameOf(c))).sorted((a, b) -> a.name().compareToIgnoreCase(b.name())).toList();
        Map<String, List<TaxReferenceDto.State>> states = new LinkedHashMap<>();
        for (String c : List.of("IN", "US", "CA", "AU")) {
            states.put(c, StateCatalog.statesOf(c).stream().map(s -> new TaxReferenceDto.State(s.code(), s.name(), s.gstCode())).toList());
        }
        BillingSettings p = billingSettingsService.current();
        String country = CountryResolver.resolve(p.getBusinessCountry()).orElse(null);
        String state = country == null ? null : StateCatalog.normalize(country, p.getBusinessState()).orElse(null);
        return new TaxReferenceDto(countries, states, names(TaxType.values()), names(TaxTreatment.values()), names(CustomerTypeCondition.values()),
            names(SupplyType.values()), names(TaxIdCondition.values()), country, state);
    }

    private static List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
