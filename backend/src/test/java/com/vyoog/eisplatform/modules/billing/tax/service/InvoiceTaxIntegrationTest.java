package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
import com.vyoog.eisplatform.modules.billing.dto.SaveBillingDetailsRequest;
import com.vyoog.eisplatform.modules.billing.model.CreditNote;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.billing.service.CheckoutService;
import com.vyoog.eisplatform.modules.billing.service.CreditNoteService;
import com.vyoog.eisplatform.modules.billing.service.InvoiceNumberService;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.billing.tax.service.StubTaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxIntegrationSupport;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import com.vyoog.eisplatform.modules.billing.tax.dto.CreditNoteDto;
import com.vyoog.eisplatform.modules.billing.tax.model.InvoiceTaxStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport.comp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-BIL-002.40 to .47, C97 TX-1 to TX-6 and BI-2/BI-3: tax on real invoices, Draft invoices, numbering, credit notes and the
 * tax ID check. Internal engine, so every rate here is a rule the test approved. */
@SpringBootTest
@ActiveProfiles("test")
@Import({TaxTestSupport.class, TaxIntegrationSupport.class, StubTaxProvider.class})
class InvoiceTaxIntegrationTest {

    @Autowired private InvoiceService invoiceService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private CheckoutService checkoutService;
    @Autowired private CreditNoteService creditNoteService;
    @Autowired private BillingDetailsService billingDetailsService;
    @Autowired private TaxIntegrationSupport fixtures;
    @Autowired private TaxTestSupport tax;

    @BeforeEach
    void reset() {
        fixtures.resetAll();
        fixtures.supplier();
        fixtures.taxClass("SAAS", "998439");
    }

    private void indiaGst() {
        tax.approvedRule("GST within state", "IN", null, SupplyType.INTRA_STATE, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null,
            comp("CGST", "Central GST", "9"), comp("SGST", "State GST", "9"));
        tax.approvedRule("GST between states", "IN", null, SupplyType.INTER_STATE, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null,
            comp("IGST", "Integrated GST", "18"));
    }

    private Invoice invoice(Customer customer, String price) {
        return invoiceService.generateForSubscription(fixtures.subscription(customer, fixtures.product(price, "SAAS")).getId());
    }

    @Test
    void anInvoiceWithinTheSellersStateShowsCgstAndSgstAndAddsThemToThePrice() {
        indiaGst();
        Customer customer = fixtures.customerIn("Tamil Nadu", "33ABCDE1234F1Z5");

        Invoice invoice = invoice(customer, "1000.00");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(invoice.getSubtotal()).isEqualTo(100000);          // TX-1: the price is without tax
        assertThat(invoice.getTaxAmount()).isEqualTo(18000);
        assertThat(invoice.getTotal()).isEqualTo(118000);
        assertThat(invoice.getTaxStatus()).isEqualTo(InvoiceTaxStatus.CALCULATED);
        assertThat(invoice.getTaxEngine()).isEqualTo(TaxEngineType.INTERNAL);
        assertThat(invoice.getPlaceOfSupply()).isEqualTo("Tamil Nadu, India");
        assertThat(invoice.getCustomerTaxId()).isEqualTo("33ABCDE1234F1Z5");
        assertThat(invoice.getIssuerSnapshot()).contains("eVyoog Test Pvt Ltd").contains("GSTIN: " + TaxTestSupport.VALID_GSTIN_TN).contains("PAN: ABCDE1234F");
        assertThat(invoiceService.taxLinesOf(invoice.getId())).extracting(t -> t.code() + ":" + t.taxAmount()).containsExactly("CGST:9000", "SGST:9000");

        InvoiceDto detail = invoiceService.getAdminInvoiceDetail(invoice.getId());
        assertThat(detail.taxLines()).hasSize(2);
        assertThat(detail.lines().get(0).taxAmount()).isEqualTo(18000);
        assertThat(detail.lines().get(0).taxClassCode()).isEqualTo("SAAS");
        assertThat(detail.lines().get(0).hsnSacCode()).isEqualTo("998439");
        assertThat(detail.taxStatus()).isEqualTo("CALCULATED");
    }

    @Test
    void anInvoiceToAnotherStateShowsIgst() {
        indiaGst();
        Invoice invoice = invoice(fixtures.customerIn("Karnataka", null), "1000.00");

        assertThat(invoice.getTaxAmount()).isEqualTo(18000);
        assertThat(invoiceService.taxLinesOf(invoice.getId())).extracting(t -> t.code()).containsExactly("IGST");
        assertThat(invoice.getPlaceOfSupply()).isEqualTo("Karnataka, India");
    }

    @Test
    void aCartInvoiceCarriesSeveralLinesAndTheirOwnTaxAmounts() {
        indiaGst();
        Customer customer = fixtures.customerIn("Tamil Nadu", null);
        var s1 = fixtures.subscription(customer, fixtures.product("1000.00", "SAAS"));
        var s2 = fixtures.subscription(customer, fixtures.product("500.00", "SAAS"));

        Invoice invoice = invoiceService.generateForSubscriptions(List.of(s1.getId(), s2.getId()));

        assertThat(invoice.getSubtotal()).isEqualTo(150000);
        assertThat(invoice.getTaxAmount()).isEqualTo(27000);
        assertThat(invoice.getLines()).extracting(l -> l.getTaxAmount()).containsExactly(18000L, 9000L);
        assertThat(invoiceService.taxLinesOf(invoice.getId())).hasSize(4);
    }

    @Test
    void withoutAMatchingRuleTheInvoiceIsADraftWithNoNumberAndNoAssumedTax() {
        Customer customer = fixtures.customerIn("Tamil Nadu", null);

        Invoice draft = invoice(customer, "1000.00");

        assertThat(draft.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(draft.getInvoiceNumber()).isNull();
        assertThat(draft.getDueAt()).isNull();
        assertThat(draft.getTaxStatus()).isEqualTo(InvoiceTaxStatus.BLOCKED);
        assertThat(draft.getTaxBlockCode()).isEqualTo("NO_MATCHING_RULE");
        assertThat(draft.getTaxBlockReason()).contains("IN");
        assertThat(draft.getTaxAmount()).isZero();
        assertThat(invoiceService.taxLinesOf(draft.getId())).isEmpty();
        assertThat(invoiceService.listBlocked()).extracting(b -> b.invoiceId()).contains(draft.getId());
    }

    @Test
    void aDraftIsHiddenFromTheCustomerCannotBePaidAndShowsAsTaxPendingAtCheckout() {
        Customer customer = fixtures.customerIn("Tamil Nadu", null);
        Invoice draft = invoice(customer, "1000.00");

        assertThat(invoiceService.listForCustomer(customer.getId(), PageRequest.of(0, 20)).getContent()).isEmpty();
        assertThat(invoiceService.listForAdmin(PageRequest.of(0, 50)).getContent()).extracting(i -> i.id()).contains(draft.getId());
        assertThat(invoiceService.getOwnInvoiceDetail(customer.getId(), null, draft.getId()).taxBlockCode()).as("the customer is not told why").isNull();
        assertThat(invoiceService.getAdminInvoiceDetail(draft.getId()).taxBlockCode()).isEqualTo("NO_MATCHING_RULE");

        var summary = checkoutService.summaryForInvoice(customer.getId(), null, draft.getId());
        assertThat(summary.taxPending()).isTrue();
        assertThat(summary.invoiceNumber()).isNull();
        // C97 BI-4: an individual may not pay by invoice at all, so a Draft is refused before its status is even looked at.
        assertThatThrownBy(() -> checkoutService.chooseOffline(customer.getId(), null, customer.getId(), draft.getId()))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ForbiddenException.class);
    }

    @Test
    void whenTheRuleIsApprovedRetryTaxIssuesTheInvoiceAndNothingElseChanges() {
        Customer customer = fixtures.customerIn("Tamil Nadu", null);
        Invoice draft = invoice(customer, "1000.00");
        assertThat(draft.getStatus()).isEqualTo(InvoiceStatus.DRAFT);

        indiaGst();
        Invoice issued = invoiceService.retryTax(draft.getId(), "admin-1");

        assertThat(issued.getId()).isEqualTo(draft.getId());
        assertThat(issued.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(issued.getInvoiceNumber()).matches("EIS/\\d{2}-\\d{2}/\\d{6}");
        assertThat(issued.getTaxAmount()).isEqualTo(18000);
        assertThat(issued.getTaxBlockCode()).isNull();
        assertThat(issued.getTaxBlockReason()).isNull();
        assertThat(Duration.between(issued.getIssuedAt(), issued.getDueAt()).toDays()).isEqualTo(15);
        assertThat(invoiceService.listForCustomer(customer.getId(), PageRequest.of(0, 20)).getContent()).extracting(i -> i.id()).containsExactly(issued.getId());
        assertThat(invoiceService.listBlocked()).extracting(b -> b.invoiceId()).doesNotContain(issued.getId());
    }

    @Test
    void retryingAnInvoiceThatIsStillBlockedKeepsItADraft() {
        Invoice draft = invoice(fixtures.customerIn("Tamil Nadu", null), "1000.00");
        Invoice again = invoiceService.retryTax(draft.getId(), "admin-1");
        assertThat(again.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(again.getTaxBlockCode()).isEqualTo("NO_MATCHING_RULE");
    }

    @Test
    void withoutBillingDetailsTheInvoiceWaitsAndIsIssuedWhenTheCustomerSavesThem() {
        indiaGst();
        Customer customer = fixtures.customer();       // no billing details yet
        Invoice draft = invoice(customer, "1000.00");
        assertThat(draft.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(draft.getTaxBlockCode()).isEqualTo("MISSING_CUSTOMER_COUNTRY");

        billingDetailsService.saveForCustomer(customer.getId(), new SaveBillingDetailsRequest("Tax Payer", customer.getEmail(), "1 Main St", null,
            "Chennai", "Tamil Nadu", "600001", "India", null));

        Invoice issued = invoiceRepository.findById(draft.getId()).orElseThrow();
        assertThat(issued.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(issued.getTaxAmount()).isEqualTo(18000);
    }

    @Test
    void invoiceNumbersFollowTheFinancialYearSequence() {
        indiaGst();
        Customer customer = fixtures.customerIn("Tamil Nadu", null);
        Invoice first = invoice(customer, "10.00");
        Invoice second = invoice(customer, "10.00");

        String fy = InvoiceNumberService.financialYear(first.getIssuedAt());
        assertThat(first.getInvoiceNumber()).startsWith("EIS/" + fy + "/");
        long a = Long.parseLong(first.getInvoiceNumber().substring(first.getInvoiceNumber().lastIndexOf('/') + 1));
        long b = Long.parseLong(second.getInvoiceNumber().substring(second.getInvoiceNumber().lastIndexOf('/') + 1));
        assertThat(b).isEqualTo(a + 1);
    }

    @Test
    void anIssuedInvoiceNeverChangesWhenTheRulesChangeAfterwards() {
        indiaGst();
        Invoice issued = invoice(fixtures.customerIn("Tamil Nadu", null), "1000.00");
        tax.approvedRule("Later rule", "IN", "TN", SupplyType.INTRA_STATE, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("GST", "GST", "28"));
        // every rule is removed too
        fixtures.resetAll();

        Invoice again = invoiceRepository.findById(issued.getId()).orElseThrow();
        assertThat(again.getTaxAmount()).isEqualTo(18000);
        assertThat(again.getTotal()).isEqualTo(118000);
        assertThat(invoiceService.taxLinesOf(issued.getId())).extracting(t -> t.code() + ":" + t.rate().stripTrailingZeros().toPlainString())
            .containsExactly("CGST:9", "SGST:9");
        assertThatThrownBy(() -> invoiceService.retryTax(issued.getId(), "admin-1")).isInstanceOf(BillingConflictException.class);
    }

    // ---- credit notes (TX-6) ----

    @Test
    void aPartialRefundIssuesACreditNoteThatReversesTheTaxInProportion() {
        indiaGst();
        Invoice invoice = invoice(fixtures.customerIn("Tamil Nadu", null), "1000.00");     // 1000.00 + 90.00 + 90.00 = 1180.00

        CreditNote note = creditNoteService.issueForRefund(invoice, 59000, "Half refund", "1", null);

        assertThat(note.getCreditNoteNumber()).startsWith("EIS-CN/");
        assertThat(note.getTotal()).isEqualTo(59000);
        assertThat(note.getSubtotal()).isEqualTo(50000);
        assertThat(note.getTaxAmount()).isEqualTo(9000);
        List<CreditNoteDto> notes = creditNoteService.listForInvoice(invoice);
        assertThat(notes).singleElement().satisfies(n -> {
            assertThat(n.taxLines()).extracting(t -> t.code() + ":" + t.taxAmount()).containsExactly("CGST:4500", "SGST:4500");
            assertThat(n.invoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
        });
        // the original invoice and its tax lines are untouched
        assertThat(invoiceService.taxLinesOf(invoice.getId())).extracting(t -> t.taxAmount()).containsExactly(9000L, 9000L);
        assertThat(invoiceRepository.findById(invoice.getId()).orElseThrow().getTaxAmount()).isEqualTo(18000);
    }

    @Test
    void creditNotesNeverCreditMoreThanTheInvoiceAndTheLastOneIsExact() {
        indiaGst();
        Invoice invoice = invoice(fixtures.customerIn("Tamil Nadu", null), "333.33");        // 33333 + 3000 + 3000 = 39333

        CreditNote first = creditNoteService.issueForRefund(invoice, 10000, "a", "1", null);
        CreditNote second = creditNoteService.issueForRefund(invoice, 10000, "b", "1", null);
        CreditNote last = creditNoteService.issueForRefund(invoice, 19333, "c", "1", null);

        assertThat(first.getTotal() + second.getTotal() + last.getTotal()).isEqualTo(39333);
        assertThat(first.getSubtotal() + second.getSubtotal() + last.getSubtotal()).isEqualTo(33333);
        assertThat(first.getTaxAmount() + second.getTaxAmount() + last.getTaxAmount()).isEqualTo(6000);
        for (CreditNote n : List.of(first, second, last)) {
            assertThat(n.getSubtotal() + n.getTaxAmount()).isEqualTo(n.getTotal());
        }
        assertThat(creditNoteService.issueForRefund(invoice, 100, "d", "1", null)).as("nothing left to credit").isNull();
        List<CreditNoteDto> all = creditNoteService.listForInvoice(invoice);
        assertThat(all).hasSize(3);
        long cgst = all.stream().flatMap(n -> n.taxLines().stream()).filter(t -> t.code().equals("CGST")).mapToLong(t -> t.taxAmount()).sum();
        long sgst = all.stream().flatMap(n -> n.taxLines().stream()).filter(t -> t.code().equals("SGST")).mapToLong(t -> t.taxAmount()).sum();
        assertThat(cgst).isEqualTo(3000);
        assertThat(sgst).isEqualTo(3000);
    }

    @Test
    void aCreditNoteUsesTheOriginalRatesEvenAfterTheEngineChanged() {
        indiaGst();
        Invoice invoice = invoice(fixtures.customerIn("Tamil Nadu", null), "1000.00");
        fixtures.resetAll();      // no rule at all any more, and the settings are reset

        CreditNote note = creditNoteService.issueForRefund(invoice, 118000, "Full refund", "1", null);

        assertThat(note.getTaxAmount()).isEqualTo(18000);
        assertThat(note.getTaxEngine()).isEqualTo("INTERNAL");
    }

    // ---- Validate tax (TX-4, TX-5) ----

    @Test
    void anIndianCustomerMustGiveAValidGstinOrNone() {
        Customer customer = fixtures.customer();
        SaveBillingDetailsRequest bad = new SaveBillingDetailsRequest("N", customer.getEmail(), "1 St", null, "Chennai", "TN", "600001", "India", "GSTIN123");
        assertThatThrownBy(() -> billingDetailsService.saveForCustomer(customer.getId(), bad))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("15-character GSTIN");

        var saved = billingDetailsService.saveForCustomer(customer.getId(), new SaveBillingDetailsRequest("N", customer.getEmail(), "1 St", null,
            "Chennai", "TN", "600001", "India", " 33abcde1234f1z5 "));
        assertThat(saved.taxId()).isEqualTo("33ABCDE1234F1Z5");
        assertThat(billingDetailsService.saveForCustomer(customer.getId(), new SaveBillingDetailsRequest("N", customer.getEmail(), "1 St", null,
            "Chennai", "TN", "600001", "India", "")).taxId()).as("a blank tax ID is fine").isIn(null, "");
    }

    @Test
    void foreignCustomersMayGiveFreeTextTaxIdsAndAValidGstinDoesNotChangeTheTaxForIndianCustomers() {
        indiaGst();
        Customer customer = fixtures.customer();
        billingDetailsService.saveForCustomer(customer.getId(), new SaveBillingDetailsRequest("N", customer.getEmail(), "1 St", null,
            "Berlin", "Berlin", "10115", "Germany", "DE 123456789"));
        assertThat(billingDetailsService.getForCustomer(customer.getId()).taxId()).isEqualTo("DE 123456789");

        // TX-5: with or without a valid GSTIN, an Indian customer in the same state pays the same tax.
        Invoice withGstin = invoice(fixtures.customerIn("Tamil Nadu", "33ABCDE1234F1Z5"), "1000.00");
        Invoice withoutGstin = invoice(fixtures.customerIn("Tamil Nadu", null), "1000.00");
        assertThat(withGstin.getTaxAmount()).isEqualTo(withoutGstin.getTaxAmount());
        assertThat(withGstin.getCustomerTaxId()).isEqualTo("33ABCDE1234F1Z5");
        assertThat(withoutGstin.getCustomerTaxId()).isNull();
    }
}
