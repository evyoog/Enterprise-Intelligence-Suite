package com.vyoog.eisplatform.modules.billing.tax.dto;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;

import java.time.LocalDate;

public record TaxRegistrationDto(Long id, String countryCode, String countryName, String stateCode, String stateName, TaxType taxType,
                                 String registrationNumber, LocalDate effectiveFrom, LocalDate effectiveTo, boolean enabled, boolean fromBusinessProfile) {
}
