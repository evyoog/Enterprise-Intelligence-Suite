package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.util.List;

public record TaxPreviewDto(
    String status,
    String blockCode,
    String blockMessage,
    String placeOfSupply,
    long taxableAmount,
    long totalTax,
    long total,
    List<Component> components
) {
    public record Component(String code, String name, String taxType, String jurisdiction, String treatment, java.math.BigDecimal rate,
                            long taxableAmount, long taxAmount, Long ruleId, Integer ruleVersion, String note) {
    }
}
