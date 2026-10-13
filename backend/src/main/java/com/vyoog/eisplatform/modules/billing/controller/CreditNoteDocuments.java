package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.dto.BusinessProfileDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.CreditNoteDto;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** TX-6: a plain-text credit note, built the same way as {@link InvoiceDocuments}: issuer, number, the invoice it reverses, the amounts
 * and each reversed tax component with its original rate. */
final class CreditNoteDocuments {

    private CreditNoteDocuments() {
    }

    static ResponseEntity<byte[]> render(CreditNoteDto note, String issuerSnapshot, BusinessProfileDto issuer, String billTo) {
        StringBuilder body = new StringBuilder();
        if (issuerSnapshot != null && !issuerSnapshot.isBlank()) {
            body.append(issuerSnapshot).append("\n\n");
        } else if (issuer != null && issuer.legalName() != null) {
            body.append(issuer.legalName()).append("\n\n");
        }
        body.append("CREDIT NOTE\n").append(note.creditNoteNumber()).append("\n");
        body.append("Issued: ").append(note.issuedAt()).append("\n");
        body.append("Against invoice: ").append(note.invoiceNumber()).append("\n");
        if (billTo != null) body.append("Bill to:\n").append(billTo).append("\n");
        if (note.reason() != null) body.append("Reason: ").append(note.reason()).append("\n");
        body.append("\nSubtotal credited: ").append(note.subtotal() / 100.0).append(" ").append(note.currency()).append("\n");
        for (var t : note.taxLines()) {
            body.append(t.name()).append(t.rate() == null ? "" : " @ " + t.rate().stripTrailingZeros().toPlainString() + "%").append(": ")
                .append(t.taxAmount() / 100.0).append(" ").append(note.currency()).append("\n");
        }
        body.append("Total tax credited: ").append(note.taxAmount() / 100.0).append(" ").append(note.currency()).append("\n");
        body.append("Total credited: ").append(note.total() / 100.0).append(" ").append(note.currency()).append("\n");
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        String filename = note.creditNoteNumber().replace('/', '-') + "-credit-note.txt";
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString()).body(bytes);
    }
}
