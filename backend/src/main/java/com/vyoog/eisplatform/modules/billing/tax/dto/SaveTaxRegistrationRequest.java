package com.vyoog.eisplatform.modules.billing.tax.dto;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SaveTaxRegistrationRequest(
    @NotBlank @Size(min = 2, max = 2) String countryCode,
    @Size(max = 100) String stateCode,
    @NotNull TaxType taxType,
    @NotBlank @Size(max = 50) String registrationNumber,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    Boolean enabled
) {
}
