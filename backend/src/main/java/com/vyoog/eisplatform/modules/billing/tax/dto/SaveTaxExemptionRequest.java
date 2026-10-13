package com.vyoog.eisplatform.modules.billing.tax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** {@code ownerType} is CUSTOMER or ORGANIZATION. */
public record SaveTaxExemptionRequest(
    @NotBlank @Size(max = 12) String ownerType,
    @NotNull Long ownerId,
    @NotBlank @Size(min = 2, max = 2) String countryCode,
    @NotBlank @Size(max = 200) String reason,
    @Size(max = 100) String certificateNumber,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveTo
) {
}
