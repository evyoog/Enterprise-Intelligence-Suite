package com.vyoog.eisplatform.modules.billing.dto;

import java.util.List;
import java.util.Map;

/** {@code /me/billing/overview} (REQ-BIL-001, Billing screen Tab 1). Amounts
 * are keyed by ISO currency code since a customer's invoices could in
 * principle span more than one plan currency (billing.md: "one figure per
 * currency if more than one"). */
public record BillingOverviewResponse(
    Map<String, Long> amountDueByCurrency,
    String nextInvoiceDate,
    String nextInvoicePlanName,
    Map<String, Long> spentThisPeriodByCurrency,
    Map<String, Long> spentLastPeriodByCurrency,
    PaymentMethodDto defaultPaymentMethod,
    List<InvoiceDto> recentInvoices,
    boolean gatewayConfigured
) {
}
