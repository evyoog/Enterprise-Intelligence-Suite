package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

public record InvoiceLineDto(
    String description,
    Instant periodStart,
    Instant periodEnd,
    int quantity,
    long unitAmount,
    long amount
) {
}
