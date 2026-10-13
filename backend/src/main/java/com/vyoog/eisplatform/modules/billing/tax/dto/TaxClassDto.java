package com.vyoog.eisplatform.modules.billing.tax.dto;

public record TaxClassDto(Long id, String code, String name, String hsnSacCode, String description, boolean enabled, long productCount) {
}
