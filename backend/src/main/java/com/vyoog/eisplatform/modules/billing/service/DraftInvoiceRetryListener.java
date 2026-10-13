package com.vyoog.eisplatform.modules.billing.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

/** Retries an owner's Draft invoices after its billing details change. A failure here never breaks saving the billing details. */
@Component
@RequiredArgsConstructor
public class DraftInvoiceRetryListener {

    private static final Logger log = LoggerFactory.getLogger(DraftInvoiceRetryListener.class);

    private final InvoiceService invoiceService;

    /** After the billing details are committed, so the retry sees them and cannot roll the save back. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBillingDetailsSaved(BillingDetailsSavedEvent event) {
        try {
            int issued = invoiceService.retryDraftsForOwner(event.customerId(), event.organizationId());
            if (issued > 0) {
                log.info("{} Draft invoice(s) were finalized after the billing details were saved", issued);
            }
        } catch (RuntimeException e) {
            log.warn("Retrying Draft invoices after a billing details change failed", e);
        }
    }
}
