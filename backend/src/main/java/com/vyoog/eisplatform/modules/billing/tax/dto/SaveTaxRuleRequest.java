package com.vyoog.eisplatform.modules.billing.tax.dto;

import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** {@code countryCode} is an ISO alpha-2 code. {@code stateCode} may be a state name or code; it is normalised (India must be a known state). */
public record SaveTaxRuleRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(min = 2, max = 2) String countryCode,
    @Size(max = 100) String stateCode,
    @Size(max = 150) String jurisdiction,
    @NotNull TaxType taxType,
    @Size(max = 40) String productClassCode,
    CustomerTypeCondition customerType,
    SupplyType supplyType,
    TaxIdCondition taxIdCondition,
    @NotNull TaxTreatment treatment,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveTo,
    @Size(max = 500) String notes,
    @Valid List<ComponentDto> components
) {
}
