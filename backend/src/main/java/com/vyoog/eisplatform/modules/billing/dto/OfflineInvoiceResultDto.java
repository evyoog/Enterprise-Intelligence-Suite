package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** C55 (REQ-BIL-001.19): the checkout's "Invoice generated" result. */
public record OfflineInvoiceResultDto(
    Long invoiceId,
    String invoiceNumber,
    long total,
    String currency,
    Instant dueAt,
    String billingEmail,
    OfflineBankDetailsDto bankDetails,
    String subscriptionStatus
) {
}
