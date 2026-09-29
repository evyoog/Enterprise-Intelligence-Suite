package com.vyoog.eisplatform.modules.product.dto;

import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductPlanCreateRequest(
    @NotBlank String name,
    @NotNull @Positive BigDecimal price,
    @NotNull BillingPeriod billingPeriod,
    Integer sortOrder,
    // Null defaults to USD in the entity (see ProductPlan#currency).
    Currency currency,
    @PositiveOrZero Integer usageLimit,
    String includedFeatures,
    @PositiveOrZero BigDecimal usagePrice,
    String tierPricing,
    @PositiveOrZero BigDecimal overageCharge
) {
}
