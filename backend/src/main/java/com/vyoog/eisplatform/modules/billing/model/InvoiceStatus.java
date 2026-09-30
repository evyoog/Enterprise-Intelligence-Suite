package com.vyoog.eisplatform.modules.billing.model;

/** See docs/02-requirements/FRD/billing-payments/workflow.md for the full
 * state diagram. VOID exists in the model but nothing in this pass sets it —
 * who may void an invoice is Not specified (FRD Open question 4). */
public enum InvoiceStatus {
    OPEN,
    PAID,
    PARTIALLY_REFUNDED,
    REFUNDED,
    VOID
}
