package com.vyoog.eisplatform.modules.billing.tax.calc;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxCalculationStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;

import java.math.BigDecimal;
import java.util.List;

/** The outcome of one calculation: either validated tax lines (CALCULATED) or a block code and message (BLOCKED).
 * Zero tax is only ever the sum of explicit zero-rate, exempt or non-taxable lines (BR-18). */
public record TaxCalculationResult(
    TaxCalculationStatus status,
    TaxEngineType engine,
    int configVersion,
    BlockCode blockCode,
    String blockMessage,
    String placeOfSupply,
    String providerCode,
    String providerReference,
    int attempts,
    List<LineResult> lines,
    String responseCopy
) {
    public record Component(String code, String name, TaxType taxType, String jurisdiction, TaxTreatment treatment, BigDecimal rate,
                            long taxableAmount, long taxAmount, Long ruleId, Integer ruleVersion, String note) {
    }

    public record LineResult(String lineRef, long taxableAmount, List<Component> components) {
        public long tax() {
            return components.stream().mapToLong(Component::taxAmount).sum();
        }
    }

    public boolean isCalculated() {
        return status == TaxCalculationStatus.CALCULATED;
    }

    public long totalTax() {
        return lines == null ? 0 : lines.stream().mapToLong(LineResult::tax).sum();
    }

    public static TaxCalculationResult blocked(TaxEngineType engine, int configVersion, BlockCode code, String message, String providerCode, int attempts) {
        return new TaxCalculationResult(TaxCalculationStatus.BLOCKED, engine, configVersion, code,
            message == null || message.isBlank() ? code.defaultMessage() : message, null, providerCode, null, attempts, List.of(), null);
    }
}
