package com.vyoog.eisplatform.modules.billing.controller;

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

    static ResponseEntity<byte[]> render(Invoice invoice, String type) {
        if ("receipt".equals(type) && invoice.getStatus() != InvoiceStatus.PAID
                && invoice.getStatus() != InvoiceStatus.PARTIALLY_REFUNDED && invoice.getStatus() != InvoiceStatus.REFUNDED) {
            throw new ResourceNotFoundException("A receipt is only available for a paid invoice.");
        }
        StringBuilder body = new StringBuilder();
        body.append("receipt".equals(type) ? "RECEIPT\n" : "INVOICE\n");
        body.append(invoice.getInvoiceNumber()).append("\n");
        body.append("Status: ").append(invoice.getStatus()).append("\n");
        body.append("Issued: ").append(invoice.getIssuedAt()).append("\n");
        body.append("Bill to:\n").append(invoice.getBillToSnapshot()).append("\n\n");
        for (var line : invoice.getLines()) {
            body.append(String.format("%-40s x%-3d %10.2f%n", line.getDescription(), line.getQuantity(), line.getAmount() / 100.0));
        }
        body.append("\nSubtotal: ").append(invoice.getSubtotal() / 100.0).append(" ").append(invoice.getCurrency()).append("\n");
        body.append("Tax: ").append(invoice.getTaxAmount() / 100.0).append(" ").append(invoice.getCurrency()).append("\n");
        body.append("Total: ").append(invoice.getTotal() / 100.0).append(" ").append(invoice.getCurrency()).append("\n");

        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        String filename = invoice.getInvoiceNumber() + ("receipt".equals(type) ? "-receipt.txt" : "-invoice.txt");
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
            .body(bytes);
    }
}
