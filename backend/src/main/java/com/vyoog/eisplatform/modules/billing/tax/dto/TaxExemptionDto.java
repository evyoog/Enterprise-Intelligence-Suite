package com.vyoog.eisplatform.modules.billing.tax.dto;

import com.vyoog.eisplatform.modules.billing.tax.model.ExemptionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaxExemptionDto(Long id, String ownerType, Long ownerId, String ownerLabel, String countryCode, String countryName, String reason,
                              String certificateNumber, LocalDate effectiveFrom, LocalDate effectiveTo, ExemptionStatus status, boolean inForce,
                              String createdBy, Instant createdAt, String approvedBy, Instant approvedAt) {
}
