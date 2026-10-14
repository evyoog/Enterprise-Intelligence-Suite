package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.util.List;
import java.util.Map;

/** Reference lists for the Tax Configuration forms. No rates. */
public record TaxReferenceDto(
    List<Country> countries,
    Map<String, List<State>> states,
    List<String> taxTypes,
    List<String> treatments,
    List<String> customerTypes,
    List<String> supplyTypes,
    List<String> taxIdConditions,
    String supplierCountryCode,
    String supplierStateCode
) {
    public record Country(String code, String name) {
    }

    public record State(String code, String name, String gstCode) {
    }
}
