package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.BusinessProfileDto;
import com.vyoog.eisplatform.modules.billing.dto.OfflineBankDetailsDto;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;
import com.vyoog.eisplatform.modules.product.model.Currency;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** C60: the invoice document prints the issuer, the footer note and — for an
 * OPEN invoice paid by invoice — the offline payment details. */
class InvoiceDocumentsTest {

    @Test
    void anOfflineInvoicePrintsTheIssuerThePaymentDetailsAndTheFooter() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("EVY-2026-000009");
        invoice.setStatus(InvoiceStatus.OPEN);
        invoice.setPaymentRoute(PaymentRoute.OFFLINE);
        invoice.setCurrency(Currency.INR);
        invoice.setSubtotal(50000);
        invoice.setTotal(50000);
        invoice.setIssuedAt(Instant.parse("2026-10-01T00:00:00Z"));
        invoice.setDueAt(Instant.parse("2026-10-31T00:00:00Z"));
        invoice.setBillToSnapshot("Jane Customer");
        BusinessProfileDto issuer = new BusinessProfileDto("eVyoog Technologies Pvt Ltd", "eVyoog", "29ABCDE1234F1Z5", "ABCDE1234F", null,
            "12 MG Road", null, "Bengaluru", "Karnataka", "560001", "India", "billing@evyoog.example", null, null,
            "EVY", 30, "Thank you for your business.", null);
        OfflineBankDetailsDto bank = new OfflineBankDetailsDto("eVyoog Technologies Pvt Ltd", "HDFC Bank", "MG Road", "50200012345678",
            "CURRENT", "HDFC0001234", null, null, null, "evyoog@hdfcbank", "eVyoog Technologies Pvt Ltd", null, null,
            true, true, true, null);

        String text = new String(InvoiceDocuments.render(invoice, "invoice", issuer, bank).getBody(), StandardCharsets.UTF_8);

        assertThat(text).startsWith("eVyoog Technologies Pvt Ltd\n");
        assertThat(text).contains("GSTIN: 29ABCDE1234F1Z5", "12 MG Road, Bengaluru, Karnataka, 560001, India",
            "Due: 2026-10-31", "Bank: HDFC Bank, MG Road", "IFSC: HDFC0001234", "UPI ID: evyoog@hdfcbank",
            "Quote EVY-2026-000009 as the payment reference.");
        assertThat(text.trim()).endsWith("Thank you for your business.");
    }
}
