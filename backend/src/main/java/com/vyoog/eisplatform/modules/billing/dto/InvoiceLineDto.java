package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** C96: {@code taxAmount} is the sum of the line's tax components; {@code taxClassCode} and {@code hsnSacCode} are what the
 * line was taxed as (BI-3 prints the SAC code). */
public record InvoiceLineDto(
    String description,
    Instant periodStart,
    Instant periodEnd,
    int quantity,
    long unitAmount,
    long amount,
    long taxAmount,
    String taxClassCode,
    String hsnSacCode
) {
}
