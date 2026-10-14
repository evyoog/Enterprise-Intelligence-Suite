package com.vyoog.eisplatform.modules.billing.tax.dto;

import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** REQ-BIL-002.21: try the internal rules without creating an invoice. {@code amount} is in the smallest currency unit. */
public record TaxPreviewRequest(
    @NotNull @Positive Long amount,
    @NotBlank @Size(max = 10) String currency,
    @NotBlank @Size(max = 100) String country,
    @Size(max = 100) String state,
    @Size(max = 40) String productClassCode,
    CustomerTypeCondition customerType,
    @Size(max = 50) String taxId,
    LocalDate date
) {
}
