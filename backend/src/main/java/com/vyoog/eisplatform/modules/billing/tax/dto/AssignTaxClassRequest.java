package com.vyoog.eisplatform.modules.billing.tax.dto;

import jakarta.validation.constraints.Size;

/** A blank or null {@code taxClassCode} clears the product's class. */
public record AssignTaxClassRequest(@Size(max = 40) String taxClassCode) {
}
