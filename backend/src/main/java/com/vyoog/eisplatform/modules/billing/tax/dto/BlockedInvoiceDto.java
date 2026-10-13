package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.time.Instant;

public record BlockedInvoiceDto(Long invoiceId, String ownerLabel, String currency, long subtotal, Instant createdAt, String blockCode,
                                String blockReason, String taxEngine) {
}
