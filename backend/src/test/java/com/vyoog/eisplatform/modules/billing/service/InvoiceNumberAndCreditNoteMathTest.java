package com.vyoog.eisplatform.modules.billing.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** C97 BI-2 (document numbers) and TX-6 (credit note rounding): plain logic. */
class InvoiceNumberAndCreditNoteMathTest {

    @Test
    void theFinancialYearRunsFromAprilToMarchInIndiaTime() {
        // 1 April 2026 00:00 India time = 31 March 2026 18:30 UTC.
        assertThat(InvoiceNumberService.financialYear(Instant.parse("2026-03-31T18:30:00Z"))).isEqualTo("26-27");
        assertThat(InvoiceNumberService.financialYear(Instant.parse("2026-03-31T18:29:59Z"))).isEqualTo("25-26");
        assertThat(InvoiceNumberService.financialYear(Instant.parse("2026-10-13T10:00:00Z"))).isEqualTo("26-27");
        assertThat(InvoiceNumberService.financialYear(Instant.parse("2027-03-31T10:00:00Z"))).isEqualTo("26-27");
        // 31 March 2027 20:00 UTC is already 1 April 2027 01:30 in India: the new financial year.
        assertThat(InvoiceNumberService.financialYear(Instant.parse("2027-03-31T20:00:00Z"))).isEqualTo("27-28");
        assertThat(InvoiceNumberService.financialYear(Instant.parse("2027-01-05T10:00:00Z"))).isEqualTo("26-27");
        assertThat(InvoiceNumberService.financialYear(Instant.parse("2099-05-05T10:00:00Z"))).isEqualTo("99-00");
    }

    @Test
    void numbersFollowTheAgreedFormat() {
        assertThat(InvoiceNumberService.format(InvoiceNumberService.INVOICE_SERIES, "EIS", "26-27", 123)).isEqualTo("EIS/26-27/000123");
        assertThat(InvoiceNumberService.format(InvoiceNumberService.INVOICE_SERIES, null, "26-27", 1)).isEqualTo("EIS/26-27/000001");
        assertThat(InvoiceNumberService.format(InvoiceNumberService.CREDIT_NOTE_SERIES, "EIS", "26-27", 7)).isEqualTo("EIS-CN/26-27/000007");
    }

    @Test
    void aCreditNotesTaxIsSplitSoThePartsAddUpExactly() {
        assertThat(CreditNoteService.allocate(90, new long[] {90, 90})).containsExactly(45, 45);
        long[] odd = CreditNoteService.allocate(91, new long[] {90, 90});
        assertThat(odd[0] + odd[1]).isEqualTo(91);
        assertThat(CreditNoteService.allocate(100, new long[] {1, 1, 1})).containsExactly(34, 33, 33);
        assertThat(CreditNoteService.allocate(0, new long[] {5, 5})).containsExactly(0, 0);
        assertThat(CreditNoteService.allocate(50, new long[] {})).isEmpty();
        assertThat(CreditNoteService.allocate(50, new long[] {0, 0})).containsExactly(0, 0);
        long[] uneven = CreditNoteService.allocate(1000, new long[] {1800, 900, 7});
        assertThat(uneven[0] + uneven[1] + uneven[2]).isEqualTo(1000);
    }
}
