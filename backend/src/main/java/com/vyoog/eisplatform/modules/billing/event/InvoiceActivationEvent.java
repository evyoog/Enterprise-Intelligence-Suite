package com.vyoog.eisplatform.modules.billing.event;

import java.util.List;

/** C97 BI-1: published when an invoice's purchase may start. {@code reason} is PAID (a card or UPI payment, or an offline payment, was
 * received) or PAY_BY_INVOICE (the customer chose to pay by invoice, so the purchase starts at once with a due date). The registration module
 * activates the subscriptions that were waiting for it; nothing else here depends on that module. */
public record InvoiceActivationEvent(Long invoiceId, List<Long> subscriptionIds, String reason) {
    public static final String PAID = "PAID";
    public static final String PAY_BY_INVOICE = "PAY_BY_INVOICE";
}
