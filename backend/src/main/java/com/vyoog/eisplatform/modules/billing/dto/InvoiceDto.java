package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;
import com.vyoog.eisplatform.modules.product.model.Currency;

import java.time.Instant;
import java.util.List;

/** {@code lines} and {@code payments} are populated for the invoice-detail
 * view only (null in a list row) — same "list vs. detail" shape the rest of
 * this codebase uses elsewhere (e.g. ProductDto). {@code ownerLabel} is
 * populated only on the admin screens (REQ-BIL-001.12), null on the
 * customer's own screens where it would just repeat "you". */
public record InvoiceDto(
    Long id,
    String invoiceNumber,
    InvoiceStatus status,
    Currency currency,
    long subtotal,
    long taxAmount,
    long total,
    Instant periodStart,
    Instant periodEnd,
    Instant issuedAt,
    Instant dueAt,
    String billToSnapshot,
    PaymentRoute paymentRoute,
    String ownerLabel,
    List<InvoiceLineDto> lines,
    List<PaymentDto> payments,
    /** C96: LEGACY (issued before tax calculation), CALCULATED or BLOCKED (a Draft). */
    String taxStatus,
    String taxEngine,
    String placeOfSupply,
    String customerTaxId,
    String issuerSnapshot,
    /** The tax components, detail view only. */
    List<com.vyoog.eisplatform.modules.billing.tax.dto.TaxComponentDto> taxLines,
    /** Admin views only: why a Draft is blocked. Never shown to the customer. */
    String taxBlockCode,
    String taxBlockReason,
    /** C97 BI-9: optional customer purchase-order number, printed on the invoice. */
    String purchaseOrderNumber,
    /** C100 (PB-1 note): the coupon applied and the discount it gave; {@code subtotal} is net of the discount, so subtotal + tax = total. */
    String couponCode,
    long discountAmount
) {
}
