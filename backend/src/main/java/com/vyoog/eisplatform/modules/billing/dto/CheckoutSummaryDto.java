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
    String checkoutThemeColor,
    /** C96: true when the invoice is a Draft because its tax could not be determined yet. Nothing can be paid until it is issued. */
    boolean taxPending,
    String placeOfSupply,
    /** C97 BI-7: links to the evyoog.com Terms, Privacy and Refund pages (Billing settings); null when not set. */
    String termsUrl,
    String privacyUrl,
    String refundPolicyUrl,
    /** C97 BI-9: the optional purchase-order number the customer gave for this invoice. */
    String purchaseOrderNumber,
    /** C97 BI-8: the active payment gateway code and its display name, so the screens say nothing gateway-specific. */
    String gatewayCode,
    String gatewayName
) {
}
