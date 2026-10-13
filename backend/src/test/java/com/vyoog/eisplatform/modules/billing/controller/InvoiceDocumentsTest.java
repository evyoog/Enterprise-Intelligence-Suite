package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.BusinessProfileDto;
import com.vyoog.eisplatform.modules.billing.dto.OfflineBankDetailsDto;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.model.InvoiceLine;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxComponentDto;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        String text = new String(InvoiceDocuments.render(invoice, "invoice", issuer, bank, java.util.List.of()).getBody(), StandardCharsets.UTF_8);

        assertThat(text).startsWith("eVyoog Technologies Pvt Ltd\n");
        assertThat(text).contains("GSTIN: 29ABCDE1234F1Z5", "12 MG Road, Bengaluru, Karnataka, 560001, India",
            "Due: 2026-10-31", "Bank: HDFC Bank, MG Road", "IFSC: HDFC0001234", "UPI ID: evyoog@hdfcbank",
            "Quote EVY-2026-000009 as the payment reference.");
        assertThat(text.trim()).endsWith("Thank you for your business.");
    }

    private Invoice taxedInvoice() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("EIS/26-27/000123");
        invoice.setStatus(InvoiceStatus.OPEN);
        invoice.setCurrency(Currency.INR);
        invoice.setSubtotal(100000);
        invoice.setTaxAmount(18000);
        invoice.setTotal(118000);
        invoice.setIssuedAt(Instant.parse("2026-10-13T00:00:00Z"));
        invoice.setBillToSnapshot("Acme Pvt Ltd\n1 MG Road");
        invoice.setCustomerTaxId("33ABCDE1234F1Z5");
        invoice.setPlaceOfSupply("Tamil Nadu, India");
        invoice.setIssuerSnapshot("eVyoog Technologies Pvt Ltd\n12 MG Road, Bengaluru, Karnataka, 560001, India\nGSTIN: 29ABCDE1234F1Z5\nPAN: ABCDE1234F");
        InvoiceLine line = new InvoiceLine();
        line.setDescription("App - Standard");
        line.setQuantity(1);
        line.setAmount(100000);
        line.setHsnSacCode("998439");
        invoice.getLines().add(line);
        return invoice;
    }

    /** BI-3: legal name, address, GSTIN and PAN of the issuer, number and date, place of supply, SAC code, the customer's name, address and GSTIN. */
    @Test
    void anIssuedInvoicePrintsEveryLegalFieldAndEachTaxComponent() {
        var cgst = new TaxComponentDto("CGST", "Central GST", "GST", "India", "TAXABLE", new java.math.BigDecimal("9.0000"), 100000, 9000, null);
        var sgst = new TaxComponentDto("SGST", "State GST", "GST", "India", "TAXABLE", new java.math.BigDecimal("9.0000"), 100000, 9000, null);

        String text = new String(InvoiceDocuments.render(taxedInvoice(), "invoice", null, null, java.util.List.of(cgst, sgst)).getBody(), StandardCharsets.UTF_8);

        assertThat(text).startsWith("eVyoog Technologies Pvt Ltd\n");
        assertThat(text).contains("12 MG Road, Bengaluru, Karnataka, 560001, India", "GSTIN: 29ABCDE1234F1Z5", "PAN: ABCDE1234F",
            "EIS/26-27/000123", "Issued: 2026-10-13", "Acme Pvt Ltd", "GSTIN / Tax ID: 33ABCDE1234F1Z5", "Place of supply: Tamil Nadu, India",
            "SAC/HSN: 998439", "Subtotal: 1000.0 INR", "Central GST @ 9%: 90.0 INR", "State GST @ 9%: 90.0 INR", "Total tax: 180.0 INR", "Total: 1180.0 INR");
    }

    @Test
    void anInvoiceIssuedBeforeTaxWasCalculatedStillPrintsASingleTaxLine() {
        Invoice invoice = taxedInvoice();
        invoice.setTaxAmount(0);
        invoice.setTotal(100000);
        String text = new String(InvoiceDocuments.render(invoice, "invoice", null, null, java.util.List.of()).getBody(), StandardCharsets.UTF_8);
        assertThat(text).contains("Tax: 0.0 INR").doesNotContain("Total tax");
    }

    @Test
    void aDraftHasNoDocument() {
        Invoice draft = taxedInvoice();
        draft.setStatus(InvoiceStatus.DRAFT);
        draft.setInvoiceNumber(null);
        assertThatThrownBy(() -> InvoiceDocuments.render(draft, "invoice", null, null, java.util.List.of())).isInstanceOf(ResourceNotFoundException.class);
    }
}
