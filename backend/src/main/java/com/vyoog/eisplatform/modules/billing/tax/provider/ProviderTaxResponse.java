package com.vyoog.eisplatform.modules.billing.tax.provider;

import java.math.BigDecimal;
import java.util.List;

/** The vendor-neutral answer, parsed but NOT yet trusted: the engine validates it (BR-33). Null fields are validation failures. */
public record ProviderTaxResponse(
    String status,
    String transactionRef,
    String providerReference,
    String currency,
    Long totalTax,
    List<LineResult> lines,
    String rawCopy
) {
    public record Tax(String code, String name, String taxType, String jurisdiction, BigDecimal rate, Long amount) {
    }

    public record LineResult(String lineRef, Long taxableAmount, List<Tax> taxes) {
    }
}
