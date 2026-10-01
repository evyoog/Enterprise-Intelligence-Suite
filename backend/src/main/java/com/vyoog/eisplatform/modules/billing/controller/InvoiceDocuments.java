package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.BusinessProfileDto;
import com.vyoog.eisplatform.modules.billing.dto.OfflineBankDetailsDto;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** REQ-BIL-001.11: a plain-text invoice/receipt document, downloadable as an
 * attachment. Decision C47: this pass does not add a PDF-rendering library —
 * FRD Open question 8 (which legal/registration fields must appear) is
 * still unanswered, so a polished PDF would need redoing anyway once that's
 * decided. The content (invoice number, lines, totals, bill-to) is real and
 * complete; only the visual formatting is deferred. */
final class InvoiceDocuments {

    private InvoiceDocuments() {
    }

    /** C60: {@code issuer} (Billing settings → Business profile) is printed at
     * the top, and the footer note at the end. An OPEN invoice to be paid by
     * invoice also carries the offline payment instructions. */
    static ResponseEntity<byte[]> render(Invoice invoice, String type, BusinessProfileDto issuer, OfflineBankDetailsDto bank) {
        if ("receipt".equals(type) && invoice.getStatus() != InvoiceStatus.PAID
                && invoice.getStatus() != InvoiceStatus.PARTIALLY_REFUNDED && invoice.getStatus() != InvoiceStatus.REFUNDED) {
            throw new ResourceNotFoundException("A receipt is only available for a paid invoice.");
        }
        StringBuilder body = new StringBuilder();
        if (issuer != null && issuer.legalName() != null) {
            body.append(issuer.legalName()).append("\n");
            if (issuer.tradeName() != null) body.append(issuer.tradeName()).append("\n");
            body.append(join(", ", issuer.addressLine1(), issuer.addressLine2(), issuer.city(), issuer.state(), issuer.postalCode(), issuer.country())).append("\n");
            if (issuer.gstin() != null) body.append("GSTIN: ").append(issuer.gstin()).append("\n");
            if (issuer.pan() != null) body.append("PAN: ").append(issuer.pan()).append("\n");
            if (issuer.cin() != null) body.append("CIN: ").append(issuer.cin()).append("\n");
            String contact = join(" | ", issuer.email(), issuer.phone(), issuer.website());
            if (!contact.isEmpty()) body.append(contact).append("\n");
            body.append("\n");
        }
        body.append("receipt".equals(type) ? "RECEIPT\n" : "INVOICE\n");
        body.append(invoice.getInvoiceNumber()).append("\n");
        body.append("Status: ").append(invoice.getStatus()).append("\n");
        body.append("Issued: ").append(invoice.getIssuedAt()).append("\n");
        if (invoice.getDueAt() != null && !"receipt".equals(type)) body.append("Due: ").append(invoice.getDueAt()).append("\n");
        body.append("Bill to:\n").append(invoice.getBillToSnapshot()).append("\n\n");
        for (var line : invoice.getLines()) {
            body.append(String.format("%-40s x%-3d %10.2f%n", line.getDescription(), line.getQuantity(), line.getAmount() / 100.0));
        }
        body.append("\nSubtotal: ").append(invoice.getSubtotal() / 100.0).append(" ").append(invoice.getCurrency()).append("\n");
        body.append("Tax: ").append(invoice.getTaxAmount() / 100.0).append(" ").append(invoice.getCurrency()).append("\n");
        body.append("Total: ").append(invoice.getTotal() / 100.0).append(" ").append(invoice.getCurrency()).append("\n");
        if (!"receipt".equals(type) && invoice.getStatus() == InvoiceStatus.OPEN
                && invoice.getPaymentRoute() == PaymentRoute.OFFLINE && bank != null && bank.isComplete()) {
            body.append("\nPay to:\n");
            body.append("Account name: ").append(bank.accountName()).append("\n");
            body.append("Bank: ").append(bank.bankName()).append(bank.branchName() != null ? ", " + bank.branchName() : "").append("\n");
            body.append("Account number: ").append(bank.accountNumber()).append(bank.accountType() != null ? " (" + bank.accountType() + ")" : "").append("\n");
            if (bank.ifsc() != null) body.append("IFSC: ").append(bank.ifsc()).append("\n");
            if (bank.swiftBic() != null) body.append("SWIFT/BIC: ").append(bank.swiftBic()).append("\n");
            if (bank.iban() != null) body.append("IBAN: ").append(bank.iban()).append("\n");
            if (bank.upiId() != null) body.append("UPI ID: ").append(bank.upiId()).append("\n");
            if (bank.chequeEnabled() && bank.chequePayableTo() != null) body.append("Cheques payable to: ").append(bank.chequePayableTo()).append("\n");
            body.append("Quote ").append(invoice.getInvoiceNumber()).append(" as the payment reference.\n");
            if (bank.instructions() != null) body.append(bank.instructions()).append("\n");
        }
        if (issuer != null && issuer.invoiceFooterNote() != null) {
            body.append("\n").append(issuer.invoiceFooterNote()).append("\n");
        }

        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        String filename = invoice.getInvoiceNumber() + ("receipt".equals(type) ? "-receipt.txt" : "-invoice.txt");
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
            .body(bytes);
    }

    private static String join(String separator, String... parts) {
        return java.util.Arrays.stream(parts).filter(p -> p != null && !p.isBlank()).collect(java.util.stream.Collectors.joining(separator));
    }
}
