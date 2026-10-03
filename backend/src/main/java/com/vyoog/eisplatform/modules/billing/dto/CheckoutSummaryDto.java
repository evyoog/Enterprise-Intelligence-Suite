package com.vyoog.eisplatform.modules.billing.dto;

import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;

import java.time.Instant;
import java.util.List;

/** C55 (REQ-BIL-001.18): everything the checkout screen needs. {@code invoiceId}
 * is null when the subscription produced no invoice (a $0 plan). */
public record CheckoutSummaryDto(
    Long invoiceId,
    String invoiceNumber,
    InvoiceStatus invoiceStatus,
    PaymentRoute paymentRoute,
    Long subscriptionId,
    String subscriptionStatus,
    String currency,
    List<CheckoutItemDto> items,
    long subtotal,
    List<TaxLineDto> taxLines,
    long total,
    Instant dueAt,
    String billingEmail,
    boolean gatewayConfigured,
    boolean payByInvoiceAllowed,
    /** C60: online methods offered (card, upi, netbanking, wallet), in tile order. */
    java.util.List<String> enabledMethods,
    /** C60: Razorpay Checkout appearance, from Billing settings; null = defaults. */
    String checkoutName,
    String checkoutDescription,
    String checkoutThemeColor
) {
}
