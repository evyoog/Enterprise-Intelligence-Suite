package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.time.Instant;
import java.util.List;

/** REQ-BIL-002.4 and .33: the result of the provider validation checks. */
public record ValidationReportDto(boolean passed, Instant validatedAt, List<Check> checks, List<String> countries, List<String> currencies,
                                  List<String> transactionTypes) {
    public record Check(String code, boolean passed, String label, String detail) {
    }
}
