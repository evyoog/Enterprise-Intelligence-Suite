package com.vyoog.eisplatform.modules.billing.tax.model;

/** LEGACY: issued before C96 with no calculation; CALCULATED: tax lines recorded; BLOCKED: the invoice is a Draft because tax could not be determined. */
public enum InvoiceTaxStatus {
    LEGACY, CALCULATED, BLOCKED
}
