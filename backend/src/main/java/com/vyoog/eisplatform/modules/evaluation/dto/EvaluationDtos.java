package com.vyoog.eisplatform.modules.evaluation.dto;

import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.dto.ProductPlanDto;

import java.math.BigDecimal;
import java.util.List;

/** C102 / REQ-MKT-005: trials, demo requests and comparison. */
public final class EvaluationDtos {

    private EvaluationDtos() {
    }

    public record StartTrialRequest(Long productId) {
    }

    /** {@code preferredTime} and {@code message} are free text from the customer. */
    public record DemoRequest(Long productId, String preferredTime, String message) {
    }

    /** What the customer's estimate says about tax: ESTIMATED (amounts given), NOT_SIGNED_IN, NEEDS_BILLING_DETAILS, or AT_CHECKOUT (cannot be estimated here). */
    public record TaxEstimate(String status, BigDecimal amount, BigDecimal total, String message) {
    }

    /** One column of the comparison: a product, or a product with one plan. Money is in the item's own currency (never converted). */
    public record CompareItem(ProductDto product, ProductPlanDto plan, String currency, BigDecimal unitPrice, String billingPeriod, int seats, BigDecimal subtotal,
                              TaxEstimate tax, List<String> includedFeatures, Integer trialDays) {
    }

    public record Comparison(int seats, List<CompareItem> items) {
    }
}
