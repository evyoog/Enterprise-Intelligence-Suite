package com.vyoog.eisplatform.modules.billing.dto;

import java.math.BigDecimal;

/** One tax component of the checkout summary (C96): the component name (for example CGST), its rate and the amount summed over the
 * invoice's lines. {@code ratePercent} is null when a provider gave no rate. */
public record TaxLineDto(
    String name,
    BigDecimal ratePercent,
    long amount
) {
}
