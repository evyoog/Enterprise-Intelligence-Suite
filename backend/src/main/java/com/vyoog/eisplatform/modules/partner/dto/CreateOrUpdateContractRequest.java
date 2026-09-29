package com.vyoog.eisplatform.modules.partner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** 14.01.02.01/.02 Create contract / Manage terms — the same upsert (see
 * PartnerContract's own javadoc). Start/end order is checked in the service,
 * not here, since it's a cross-field rule. */
public record CreateOrUpdateContractRequest(
    @NotBlank String terms,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {
}
