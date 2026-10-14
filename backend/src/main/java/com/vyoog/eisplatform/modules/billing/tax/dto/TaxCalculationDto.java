package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.time.Instant;

public record TaxCalculationDto(Long id, String transactionRef, String engine, int configVersion, String status, String reasonCode, String reasonMessage,
                                String providerCode, String providerReference, int attempts, Long taxTotal, Instant calculatedAt) {
}
