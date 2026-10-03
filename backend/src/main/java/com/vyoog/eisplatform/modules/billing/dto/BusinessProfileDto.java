package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

/** C60: the business that issues invoices (printed on every invoice and
 * receipt) and the invoicing rules. Not secrets. Fields are null until a
 * billing administrator saves them, except the defaults {@code invoicePrefix}
 * "INV" and {@code paymentTermsDays} 0. */
public record BusinessProfileDto(
    String legalName,
    String tradeName,
    String gstin,
    String pan,
    String cin,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country,
    String email,
    String phone,
    String website,
    String invoicePrefix,
    int paymentTermsDays,
    String invoiceFooterNote,
    Instant updatedAt
) {
    public boolean isComplete() {
        return legalName != null && addressLine1 != null;
    }
}
