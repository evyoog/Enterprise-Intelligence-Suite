package com.vyoog.eisplatform.modules.product.dto;

import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductPlanDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private BillingPeriod billingPeriod;
    private Integer sortOrder;
    private Currency currency;
    private Integer usageLimit;
    private String includedFeatures;
    private BigDecimal usagePrice;
    private String tierPricing;
    private BigDecimal overageCharge;
}
