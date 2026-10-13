package com.vyoog.eisplatform.modules.billing.model;

/** See docs/02-requirements/FRD/billing-payments/workflow.md for the full
 * state diagram. VOID exists in the model but nothing in this pass sets it —
 * who may void an invoice is Not specified (FRD Open question 4). */
public enum InvoiceStatus {
    /** C96 (BR-40): tax could not be determined, so the invoice is not final: no number, not payable, hidden from the customer. */
    DRAFT,
    OPEN,
    PAID,
    PARTIALLY_REFUNDED,
    REFUNDED,
    VOID
}
