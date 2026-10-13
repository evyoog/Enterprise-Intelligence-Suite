package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.tax.calc.BlockCode;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxModeSnapshot;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.ProviderEnvironment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.model.TransactionType;
import com.vyoog.eisplatform.modules.billing.tax.model.ValidationStatus;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderCapabilities;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderContext;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderException;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderTaxRequest;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderTaxResponse;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProviderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/** REQ-BIL-002.30 to .36 (BR-31 to BR-34): the external engine against a fake provider. No database, no network, no sleeping. */
class ExternalTaxEngineTest {

    /** A provider whose answer and failures the test controls. */
    static class FakeProvider implements TaxProvider {
        boolean configured = true;
        final List<ProviderTaxRequest> calls = new ArrayList<>();
        Function<ProviderTaxRequest, ProviderTaxResponse> answer;
        final List<ProviderException> failures = new ArrayList<>();   // thrown first, one per call

        @Override public String code() { return "FAKE"; }
        @Override public String displayName() { return "Fake"; }
        @Override public boolean credentialsPresent() { return configured; }
        @Override public Map<String, Boolean> credentialStatus() { return Map.of("FAKE_KEY", configured); }
        @Override public String maskedEndpoint() { return "fake.example.com"; }
        @Override public ProviderCapabilities testConnection(ProviderContext context) { return new ProviderCapabilities("Fake", Set.of("IN"), Set.of("INR"), Set.of("SALE")); }

        @Override
        public ProviderTaxResponse calculate(ProviderContext context, ProviderTaxRequest request) throws ProviderException {
            calls.add(request);
            if (!failures.isEmpty()) {
                throw failures.remove(0);
            }
            return answer.apply(request);
        }
    }

    private FakeProvider provider;
    private final List<Long> sleeps = new ArrayList<>();
    private ExternalTaxEngine engine;

    @BeforeEach
    void setUp() {
        provider = new FakeProvider();
        provider.answer = this::goodAnswer;
        sleeps.clear();
        engine = new ExternalTaxEngine(new TaxProviderRegistry(List.of(provider)), new TaxRetrySleeper() {
            @Override public void sleep(long millis) { sleeps.add(millis); }
        });
    }

    private TaxModeSnapshot mode(ValidationStatus validation, int attempts) {
        return new TaxModeSnapshot(TaxEngineType.EXTERNAL, 7, "FAKE", "CO1", ProviderEnvironment.SANDBOX, 5, attempts, validation,
            Set.of("IN"), Set.of("INR"), Set.of("SALE"));
    }

    private TaxCalculationRequest request(String country, String currency, String taxClass) {
        var supplier = new TaxCalculationRequest.Supplier("eVyoog", "1 St", "Chennai", "Tamil Nadu", "TN", "600001", "IN", "33ABCDE1234F1Z5",
            List.of(new TaxCalculationRequest.Registration("IN", "TN", TaxType.GST, "33ABCDE1234F1Z5")));
        var customer = new TaxCalculationRequest.Customer(CustomerTypeCondition.BUSINESS, null, 5L, "Acme", "2 Rd", "Bengaluru", "Karnataka", "KA",
            "560001", country, country, "29ABCDE1234F1Z5");
        return new TaxCalculationRequest("INV-1-1", TransactionType.SALE, LocalDate.of(2026, 10, 13), currency, supplier, customer,
            List.of(new TaxCalculationRequest.Line("101", "App - Standard", taxClass, "998439", 99900, 1),
                    new TaxCalculationRequest.Line("102", "Add-on", taxClass, "998439", 10000, 1)));
    }

    private ProviderTaxResponse goodAnswer(ProviderTaxRequest r) {
        List<ProviderTaxResponse.LineResult> lines = r.lines().stream().map(l -> new ProviderTaxResponse.LineResult(l.lineRef(), l.amount(),
            List.of(new ProviderTaxResponse.Tax("IGST", "Integrated GST", "GST", "India", new BigDecimal("18"), l.amount() * 18 / 100)))).toList();
        long total = lines.stream().flatMap(l -> l.taxes().stream()).mapToLong(ProviderTaxResponse.Tax::amount).sum();
        return new ProviderTaxResponse("SUCCESS", r.transactionRef(), "txn_1", r.currency(), total, lines, "{\"copy\":true}");
    }

    private TaxCalculationResult run(TaxCalculationRequest request, TaxModeSnapshot mode) {
        return engine.calculate(request, mode);
    }

    @Test
    void aValidProviderAnswerBecomesTheStoredBreakdown() {
        TaxCalculationResult result = run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3));

        assertThat(result.isCalculated()).isTrue();
        assertThat(result.engine()).isEqualTo(TaxEngineType.EXTERNAL);
        assertThat(result.configVersion()).isEqualTo(7);
        assertThat(result.providerReference()).isEqualTo("txn_1");
        assertThat(result.providerCode()).isEqualTo("FAKE");
        assertThat(result.responseCopy()).isEqualTo("{\"copy\":true}");
        assertThat(result.totalTax()).isEqualTo(17982 + 1800);
        assertThat(result.lines()).hasSize(2);
        assertThat(result.lines().get(0).components()).singleElement().satisfies(c -> {
            assertThat(c.code()).isEqualTo("IGST");
            assertThat(c.taxableAmount()).isEqualTo(99900);
            assertThat(c.taxAmount()).isEqualTo(17982);
            assertThat(c.rate()).isEqualByComparingTo("18");
        });
        assertThat(result.placeOfSupply()).contains("Karnataka");
    }

    @Test
    void theRequestCarriesTheSupplierTheCustomerTheClassAndTheIdempotencyKey() {
        run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3));

        ProviderTaxRequest sent = provider.calls.get(0);
        assertThat(sent.transactionRef()).isEqualTo("INV-1-1");
        assertThat(sent.companyCode()).isEqualTo("CO1");
        assertThat(sent.currency()).isEqualTo("INR");
        assertThat(sent.supplier().taxIds()).extracting(ProviderTaxRequest.TaxIdentifier::number).contains("33ABCDE1234F1Z5");
        assertThat(sent.customer().country()).isEqualTo("IN");
        assertThat(sent.customer().taxIds()).extracting(ProviderTaxRequest.TaxIdentifier::number).containsExactly("29ABCDE1234F1Z5");
        assertThat(sent.lines()).extracting(ProviderTaxRequest.Line::taxClass).containsOnly("SAAS");
        assertThat(sent.lines()).extracting(ProviderTaxRequest.Line::amount).containsExactly(99900L, 10000L);
    }

    @Test
    void transientFailuresAreRetriedWithGrowingWaitsAndTheSameKey() {
        provider.failures.add(ProviderException.transientFailure("HTTP 503", null));
        provider.failures.add(ProviderException.transientFailure("timeout", null));

        TaxCalculationResult result = run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3));

        assertThat(result.isCalculated()).isTrue();
        assertThat(result.attempts()).isEqualTo(3);
        assertThat(provider.calls).extracting(ProviderTaxRequest::transactionRef).containsOnly("INV-1-1");
        assertThat(sleeps).containsExactly(250L, 500L);
    }

    @Test
    void whenTheProviderKeepsFailingTheInvoiceIsBlockedAndNothingElseIsUsed() {
        for (int i = 0; i < 5; i++) {
            provider.failures.add(ProviderException.transientFailure("HTTP 503", null));
        }

        TaxCalculationResult result = run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3));

        assertThat(result.isCalculated()).isFalse();
        assertThat(result.blockCode()).isEqualTo(BlockCode.PROVIDER_UNAVAILABLE);
        assertThat(result.attempts()).isEqualTo(3);
        assertThat(provider.calls).hasSize(3);
        assertThat(result.lines()).isEmpty();       // no tax at all: no silent zero, no fall-back figure
    }

    @Test
    void aPermanentFailureIsNotRetried() {
        provider.failures.add(ProviderException.permanent("The provider rejected the request (HTTP 400)."));

        TaxCalculationResult result = run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3));

        assertThat(result.blockCode()).isEqualTo(BlockCode.PROVIDER_REJECTED);
        assertThat(provider.calls).hasSize(1);
        assertThat(sleeps).isEmpty();
    }

    @Test
    void anUnparseableAnswerBlocksAsInvalid() {
        provider.failures.add(ProviderException.invalidResponse("not JSON", null));
        assertThat(run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3)).blockCode()).isEqualTo(BlockCode.PROVIDER_INVALID_RESPONSE);
    }

    @Test
    void anAdapterThatCrashesBlocksTheInvoiceInsteadOfThrowing() {
        provider.answer = r -> { throw new IllegalStateException("boom"); };
        assertThat(run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3)).blockCode()).isEqualTo(BlockCode.ENGINE_ERROR);
    }

    // ---- BR-33: an answer is trusted only when it passes every check ----

    private void assertInvalid(Function<ProviderTaxRequest, ProviderTaxResponse> answer, String messagePart) {
        provider.answer = answer;
        provider.calls.clear();
        TaxCalculationResult result = run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3));
        assertThat(result.isCalculated()).as(messagePart).isFalse();
        assertThat(result.blockCode()).isEqualTo(BlockCode.PROVIDER_INVALID_RESPONSE);
        assertThat(result.blockMessage()).contains(messagePart);
        assertThat(provider.calls).as("an invalid answer is not retried").hasSize(1);
    }

    private static ProviderTaxResponse with(ProviderTaxResponse base, String status, String ref, String providerRef, String currency, Long total,
                                            List<ProviderTaxResponse.LineResult> lines) {
        return new ProviderTaxResponse(status, ref, providerRef, currency, total, lines, "{}");
    }

    @Test
    void aFailureStatusIsRefused() {
        assertInvalid(r -> with(goodAnswer(r), "ERROR", r.transactionRef(), "x", r.currency(), goodAnswer(r).totalTax(), goodAnswer(r).lines()), "status is ERROR");
    }

    @Test
    void anotherTransactionReferenceIsRefused() {
        assertInvalid(r -> with(goodAnswer(r), "SUCCESS", "SOMETHING-ELSE", "x", r.currency(), goodAnswer(r).totalTax(), goodAnswer(r).lines()), "transaction reference");
    }

    @Test
    void anotherCurrencyIsRefused() {
        assertInvalid(r -> with(goodAnswer(r), "SUCCESS", r.transactionRef(), "x", "USD", goodAnswer(r).totalTax(), goodAnswer(r).lines()), "currency");
    }

    @Test
    void aMissingProviderReferenceIsRefused() {
        assertInvalid(r -> with(goodAnswer(r), "SUCCESS", r.transactionRef(), " ", r.currency(), goodAnswer(r).totalTax(), goodAnswer(r).lines()), "provider reference");
    }

    @Test
    void aChangedTaxableAmountIsRefused() {
        assertInvalid(r -> {
            ProviderTaxResponse good = goodAnswer(r);
            List<ProviderTaxResponse.LineResult> lines = new ArrayList<>(good.lines());
            lines.set(0, new ProviderTaxResponse.LineResult("101", 12345L, lines.get(0).taxes()));
            return with(good, "SUCCESS", r.transactionRef(), "x", r.currency(), good.totalTax(), lines);
        }, "taxable amount of line 101 changed");
    }

    @Test
    void aMissingLineIsRefused() {
        assertInvalid(r -> {
            ProviderTaxResponse good = goodAnswer(r);
            return with(good, "SUCCESS", r.transactionRef(), "x", r.currency(), 17982L, List.of(good.lines().get(0)));
        }, "line 102 is missing");
    }

    @Test
    void anUnknownOrRepeatedLineIsRefused() {
        assertInvalid(r -> {
            ProviderTaxResponse good = goodAnswer(r);
            List<ProviderTaxResponse.LineResult> lines = new ArrayList<>(good.lines());
            lines.add(new ProviderTaxResponse.LineResult("999", 1L, List.of()));
            return with(good, "SUCCESS", r.transactionRef(), "x", r.currency(), good.totalTax(), lines);
        }, "unknown line 999");
        assertInvalid(r -> {
            ProviderTaxResponse good = goodAnswer(r);
            List<ProviderTaxResponse.LineResult> lines = new ArrayList<>(good.lines());
            lines.add(good.lines().get(0));
            return with(good, "SUCCESS", r.transactionRef(), "x", r.currency(), good.totalTax() + 17982, lines);
        }, "appears twice");
    }

    @Test
    void aNegativeTaxIsRefused() {
        assertInvalid(r -> {
            List<ProviderTaxResponse.LineResult> lines = r.lines().stream().map(l -> new ProviderTaxResponse.LineResult(l.lineRef(), l.amount(),
                List.of(new ProviderTaxResponse.Tax("IGST", "IGST", "GST", null, new BigDecimal("18"), -5L)))).toList();
            return with(null, "SUCCESS", r.transactionRef(), "x", r.currency(), -10L, lines);
        }, "negative");
    }

    @Test
    void sumsThatDoNotAddUpAreRefused() {
        assertInvalid(r -> {
            ProviderTaxResponse good = goodAnswer(r);
            return with(good, "SUCCESS", r.transactionRef(), "x", r.currency(), good.totalTax() + 1, good.lines());
        }, "not the sum of the lines");
    }

    @Test
    void aTaxLargerThanTheTaxableAmountIsRefused() {
        assertInvalid(r -> {
            List<ProviderTaxResponse.LineResult> lines = r.lines().stream().map(l -> new ProviderTaxResponse.LineResult(l.lineRef(), l.amount(),
                List.of(new ProviderTaxResponse.Tax("X", "X", null, null, null, l.amount() * 2)))).toList();
            long total = lines.stream().flatMap(l -> l.taxes().stream()).mapToLong(ProviderTaxResponse.Tax::amount).sum();
            return with(null, "SUCCESS", r.transactionRef(), "x", r.currency(), total, lines);
        }, "more than the taxable amount");
    }

    @Test
    void aRateOutsideZeroToHundredIsRefused() {
        assertInvalid(r -> {
            List<ProviderTaxResponse.LineResult> lines = r.lines().stream().map(l -> new ProviderTaxResponse.LineResult(l.lineRef(), l.amount(),
                List.of(new ProviderTaxResponse.Tax("X", "X", null, null, new BigDecimal("180"), 1L)))).toList();
            return with(null, "SUCCESS", r.transactionRef(), "x", r.currency(), (long) lines.size(), lines);
        }, "outside 0 to 100");
    }

    // ---- BR-31: nothing is sent unless the provider is ready and supports the transaction ----

    @Test
    void anUnconfiguredProviderBlocksWithoutCallingIt() {
        provider.configured = false;
        TaxCalculationResult result = run(request("IN", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3));
        assertThat(result.blockCode()).isEqualTo(BlockCode.PROVIDER_NOT_CONFIGURED);
        assertThat(provider.calls).isEmpty();
    }

    @Test
    void aProviderThatIsNotSelectedBlocks() {
        TaxModeSnapshot none = new TaxModeSnapshot(TaxEngineType.EXTERNAL, 7, null, null, ProviderEnvironment.SANDBOX, 5, 3, ValidationStatus.PASSED,
            Set.of("IN"), Set.of("INR"), Set.of("SALE"));
        assertThat(run(request("IN", "INR", "SAAS"), none).blockCode()).isEqualTo(BlockCode.PROVIDER_NOT_CONFIGURED);
    }

    @Test
    void aProviderThatHasNotPassedValidationBlocksWithoutCallingIt() {
        for (ValidationStatus status : List.of(ValidationStatus.NOT_VALIDATED, ValidationStatus.FAILED)) {
            assertThat(run(request("IN", "INR", "SAAS"), mode(status, 3)).blockCode()).isEqualTo(BlockCode.PROVIDER_NOT_VALIDATED);
        }
        assertThat(provider.calls).isEmpty();
    }

    @Test
    void anUnsupportedCountryCurrencyOrTypeBlocksWithoutCallingTheProvider() {
        assertThat(run(request("DE", "INR", "SAAS"), mode(ValidationStatus.PASSED, 3)).blockCode()).isEqualTo(BlockCode.PROVIDER_UNSUPPORTED);
        assertThat(run(request("IN", "USD", "SAAS"), mode(ValidationStatus.PASSED, 3)).blockCode()).isEqualTo(BlockCode.PROVIDER_UNSUPPORTED);
        TaxCalculationRequest credit = request("IN", "INR", "SAAS");
        TaxCalculationRequest creditNote = new TaxCalculationRequest("CN-1", TransactionType.CREDIT_NOTE, credit.date(), "INR", credit.supplier(), credit.customer(), credit.lines());
        assertThat(run(creditNote, mode(ValidationStatus.PASSED, 3)).blockCode()).isEqualTo(BlockCode.PROVIDER_UNSUPPORTED);
        assertThat(provider.calls).isEmpty();
    }

    @Test
    void aMissingCountryOrTaxClassBlocks() {
        TaxCalculationRequest noCountry = request("IN", "INR", "SAAS");
        var customer = noCountry.customer();
        var unknown = new TaxCalculationRequest.Customer(customer.kind(), null, 5L, "Acme", null, null, null, null, null, "Atlantis", null, null);
        assertThat(run(new TaxCalculationRequest("INV-1-1", TransactionType.SALE, noCountry.date(), "INR", noCountry.supplier(), unknown, noCountry.lines()),
            mode(ValidationStatus.PASSED, 3)).blockCode()).isEqualTo(BlockCode.MISSING_CUSTOMER_COUNTRY);
        TaxCalculationResult noClass = run(request("IN", "INR", null), mode(ValidationStatus.PASSED, 3));
        assertThat(noClass.blockCode()).isEqualTo(BlockCode.MISSING_TAX_CLASS);
        assertThat(noClass.blockMessage()).contains("App - Standard");
        assertThat(provider.calls).isEmpty();
    }
}
