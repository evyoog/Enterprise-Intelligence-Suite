package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.model.BillingDetails;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The billing e-mail address saved for an invoice's owner, if any. */
@Component
@RequiredArgsConstructor
public class BillingDetailsEmail {

    private final BillingDetailsRepository repository;

    public String forInvoice(Invoice invoice) {
        BillingDetails details = invoice.getOwnerCustomerId() != null
            ? repository.findByOwnerCustomerId(invoice.getOwnerCustomerId()).orElse(null)
            : repository.findByOwnerOrganizationId(invoice.getOwnerOrganizationId()).orElse(null);
        return details == null || details.getBillingEmail() == null || details.getBillingEmail().isBlank() ? null : details.getBillingEmail();
    }
}
