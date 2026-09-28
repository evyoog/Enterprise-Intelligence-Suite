package com.vyoog.eisplatform.modules.partner.dto;

import com.vyoog.eisplatform.modules.partner.model.ContractStatus;

import java.time.LocalDate;

public record PartnerContractDto(
    Long id,
    Long providerId,
    String terms,
    LocalDate startDate,
    LocalDate endDate,
    ContractStatus status
) {
}
