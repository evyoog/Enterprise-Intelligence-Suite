package com.vyoog.eisplatform.modules.product.dto;

import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductPlanCreateRequest(
    @NotBlank String name,
    @NotNull @Positive BigDecimal price,
    @NotNull BillingPeriod billingPeriod,
    Integer sortOrder
) {
}
