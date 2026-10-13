package com.vyoog.eisplatform.modules.billing.tax.dto;

/** {@code changed} is false when switching on was refused; {@code report} then lists every failed check (REQ-BIL-002.4). */
public record TaxModeResultDto(boolean changed, TaxStatusDto status, ValidationReportDto report) {
}
