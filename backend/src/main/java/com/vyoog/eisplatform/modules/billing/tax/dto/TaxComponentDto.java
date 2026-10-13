package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.math.BigDecimal;

/** One tax component printed on an invoice or credit note. */
public record TaxComponentDto(String code, String name, String taxType, String jurisdiction, String treatment, BigDecimal rate,
                         long taxableAmount, long taxAmount, String note) {
}
