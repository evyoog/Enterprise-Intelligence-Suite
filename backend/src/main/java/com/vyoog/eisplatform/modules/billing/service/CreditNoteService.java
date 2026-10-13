package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.model.CreditNote;
import com.vyoog.eisplatform.modules.billing.model.CreditNoteTaxLine;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceTaxLine;
import com.vyoog.eisplatform.modules.billing.repository.CreditNoteRepository;
import com.vyoog.eisplatform.modules.billing.repository.CreditNoteTaxLineRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceTaxLineRepository;
import com.vyoog.eisplatform.modules.billing.tax.dto.CreditNoteDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxComponentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** REQ-BIL-002.46 / BR-42 (TX-6): a refund issues a credit note that reverses the original invoice's tax lines in proportion.
 * The engine in force today is never asked: the credit note uses the original rates and components, so it always matches
 * the invoice it reverses. */
@Service
@RequiredArgsConstructor
public class CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteTaxLineRepository taxLineRepository;
    private final InvoiceTaxLineRepository invoiceTaxLineRepository;
    private final InvoiceNumberService numberService;
    private final BillingSettingsService billingSettingsService;
    private final AuditService auditService;

    /** Returns null when there is nothing left to credit. {@code grossAmount} is what was refunded (tax included). */
    @Transactional
    public CreditNote issueForRefund(Invoice invoice, long grossAmount, String reason, String issuedBy, Long refundId) {
        long alreadyCredited = creditNoteRepository.totalCredited(invoice.getId());
        long remaining = invoice.getTotal() - alreadyCredited;
        if (grossAmount <= 0 || remaining <= 0 || invoice.getTotal() <= 0) {
            return null;
        }
        long gross = Math.min(grossAmount, remaining);
        boolean last = gross == remaining;

        long subtotal;
        if (last) {
            subtotal = invoice.getSubtotal() - creditNoteRepository.subtotalCredited(invoice.getId());
        } else {
            subtotal = BigDecimal.valueOf(gross).multiply(BigDecimal.valueOf(invoice.getSubtotal()))
                .divide(BigDecimal.valueOf(invoice.getTotal()), 0, RoundingMode.HALF_UP).longValueExact();
        }
        long tax = gross - subtotal;

        CreditNote note = new CreditNote();
        note.setCreditNoteNumber(numberService.next(InvoiceNumberService.CREDIT_NOTE_SERIES, billingSettingsService.current().getInvoicePrefix(), Instant.now()));
        note.setInvoiceId(invoice.getId());
        note.setOwnerCustomerId(invoice.getOwnerCustomerId());
        note.setOwnerOrganizationId(invoice.getOwnerOrganizationId());
        note.setCurrency(invoice.getCurrency().name());
        note.setSubtotal(subtotal);
        note.setTaxAmount(tax);
        note.setTotal(gross);
        note.setReason(reason);
        note.setTaxEngine(invoice.getTaxEngine() == null ? null : invoice.getTaxEngine().name());
        note.setPaymentRefundId(refundId);
        note.setIssuedBy(issuedBy);
        note = creditNoteRepository.save(note);

        List<InvoiceTaxLine> original = invoiceTaxLineRepository.findByInvoiceIdOrderBySortOrderAscIdAsc(invoice.getId());
        // What earlier credit notes already took back, per original tax line.
        long[] already = new long[original.size()];
        final Long noteId = note.getId();
        List<Long> earlierIds = creditNoteRepository.findByInvoiceIdOrderByIdAsc(invoice.getId()).stream()
            .map(CreditNote::getId).filter(id -> !id.equals(noteId)).toList();
        if (!earlierIds.isEmpty()) {
            for (CreditNoteTaxLine earlier : taxLineRepository.findByCreditNoteIdIn(earlierIds)) {
                if (earlier.getSortOrder() >= 0 && earlier.getSortOrder() < already.length) {
                    already[earlier.getSortOrder()] += earlier.getTaxAmount();
                }
            }
        }
        long[] allocation;
        if (last) {
            // The last credit note takes exactly what is left of each component, so the credit notes add up to the invoice.
            allocation = new long[original.size()];
            for (int i = 0; i < allocation.length; i++) {
                allocation[i] = original.get(i).getTaxAmount() - already[i];
            }
        } else {
            allocation = allocate(tax, original.stream().mapToLong(InvoiceTaxLine::getTaxAmount).toArray());
            for (int i = 0; i < allocation.length; i++) {
                allocation[i] = Math.min(allocation[i], original.get(i).getTaxAmount() - already[i]);
            }
        }
        BigDecimal ratio = BigDecimal.valueOf(gross).divide(BigDecimal.valueOf(invoice.getTotal()), 12, RoundingMode.HALF_UP);
        for (int i = 0; i < original.size(); i++) {
            InvoiceTaxLine o = original.get(i);
            CreditNoteTaxLine line = new CreditNoteTaxLine();
            line.setCreditNoteId(note.getId());
            line.setSortOrder(i);
            line.setComponentCode(o.getComponentCode());
            line.setComponentName(o.getComponentName());
            line.setTaxType(o.getTaxType());
            line.setJurisdiction(o.getJurisdiction());
            line.setRate(o.getRate());
            line.setTaxableAmount(BigDecimal.valueOf(o.getTaxableAmount()).multiply(ratio).setScale(0, RoundingMode.HALF_UP).longValueExact());
            line.setTaxAmount(allocation[i]);
            taxLineRepository.save(line);
        }
        auditService.recordSuccess("CREDIT_NOTE_ISSUED", null, invoice.getOwnerCustomerId(), null, "CreditNote", note.getId().toString(),
            invoice.getOwnerOrganizationId(), "Credit note " + note.getCreditNoteNumber() + " for invoice " + invoice.getInvoiceNumber() + ": " + gross + " (tax " + tax + ")");
        return note;
    }

    /** Splits {@code total} across the weights so the parts add up to exactly {@code total} (largest remainder). */
    static long[] allocate(long total, long[] weights) {
        long[] out = new long[weights.length];
        long weightSum = 0;
        for (long w : weights) {
            weightSum += w;
        }
        if (weights.length == 0 || weightSum <= 0) {
            return out;
        }
        List<long[]> remainders = new ArrayList<>();   // {index, remainder numerator}
        long given = 0;
        for (int i = 0; i < weights.length; i++) {
            long numerator = total * weights[i];
            out[i] = numerator / weightSum;
            given += out[i];
            remainders.add(new long[] {i, numerator % weightSum});
        }
        remainders.sort(Comparator.comparingLong((long[] r) -> r[1]).reversed().thenComparingLong(r -> r[0]));
        for (int k = 0; given < total && k < remainders.size(); k++, given++) {
            out[(int) remainders.get(k)[0]]++;
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<CreditNoteDto> listForInvoice(Invoice invoice) {
        return creditNoteRepository.findByInvoiceIdOrderByIdAsc(invoice.getId()).stream().map(n -> toDto(n, invoice.getInvoiceNumber())).toList();
    }

    @Transactional(readOnly = true)
    public CreditNoteDto get(Invoice invoice, Long creditNoteId) {
        CreditNote n = creditNoteRepository.findById(creditNoteId).filter(c -> c.getInvoiceId().equals(invoice.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Credit note not found"));
        return toDto(n, invoice.getInvoiceNumber());
    }

    private CreditNoteDto toDto(CreditNote n, String invoiceNumber) {
        List<TaxComponentDto> lines = taxLineRepository.findByCreditNoteIdOrderBySortOrderAscIdAsc(n.getId()).stream()
            .map(l -> new TaxComponentDto(l.getComponentCode(), l.getComponentName(), l.getTaxType() == null ? null : l.getTaxType().name(), l.getJurisdiction(),
                "TAXABLE", l.getRate(), l.getTaxableAmount(), l.getTaxAmount(), null)).toList();
        return new CreditNoteDto(n.getId(), n.getCreditNoteNumber(), n.getInvoiceId(), invoiceNumber, n.getCurrency(), n.getSubtotal(), n.getTaxAmount(),
            n.getTotal(), n.getReason(), n.getIssuedAt(), lines);
    }
}
