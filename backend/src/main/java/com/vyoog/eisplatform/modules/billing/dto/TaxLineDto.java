package com.vyoog.eisplatform.modules.billing.dto;

import java.math.BigDecimal;

/** C55: one tax line of the checkout summary. {@code ratePercent} is null
 * until REQ-BIL-002 (tax rules) is built — invoices carry a single
 * {@code taxAmount} today. */
public record TaxLineDto(
    String name,
    BigDecimal ratePercent,
    long amount
) {
}
