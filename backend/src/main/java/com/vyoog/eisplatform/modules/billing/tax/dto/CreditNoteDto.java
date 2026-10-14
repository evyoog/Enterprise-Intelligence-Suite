package com.vyoog.eisplatform.modules.billing.tax.dto;

import java.time.Instant;
import java.util.List;

public record CreditNoteDto(Long id, String creditNoteNumber, Long invoiceId, String invoiceNumber, String currency, long subtotal, long taxAmount,
                            long total, String reason, Instant issuedAt, List<TaxComponentDto> taxLines) {
}
