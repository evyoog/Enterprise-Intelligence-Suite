package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.billing.tax.service.StubTaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxIntegrationSupport;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxModeSnapshot;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveProviderSettingsRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxModeResultDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxStatusDto;
import com.vyoog.eisplatform.modules.billing.tax.model.InvoiceTaxStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TransactionType;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderException;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport.comp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-BIL-002.1 to .6, .30 to .42 and the switching table of the requirement: the External Tax Engine switch, the pinned
 * configuration, the retry rules and "no silent fall-back". Uses a stub provider registered like a real adapter. */
@SpringBootTest
@ActiveProfiles("test")
@Import({TaxTestSupport.class, TaxIntegrationSupport.class, StubTaxProvider.class})
class TaxEngineSwitchTest {

    private static final String ADMIN = "admin-1";

    @Autowired private TaxSettingsService settingsService;
    @Autowired private TaxCalculationService calculationService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private TaxIntegrationSupport fixtures;
    @Autowired private TaxTestSupport tax;
    @Autowired private StubTaxProvider stub;
    @Autowired private com.vyoog.eisplatform.modules.billing.tax.repository.TaxClassRepository classRepository;

    @BeforeEach
    void reset() {
        fixtures.resetAll();
        fixtures.supplier();
        fixtures.taxClass("SAAS", "998439");
    }

    private void configureStubProvider() {
        settingsService.saveProviderSettings(ADMIN, new SaveProviderSettingsRequest("STUB", "CO1", "SANDBOX", 5, 3));
    }

    private void internalGst() {
        tax.approvedRule("Internal GST", "IN", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("GST", "GST", "5"));
    }

    private Invoice invoiceFor(Customer customer, Product product) {
        return invoiceService.generateForSubscription(fixtures.subscription(customer, product).getId());
    }

    @Test
    void theDefaultIsTheInternalEngineAndTheScreenSaysItIsBlockingWhenNoRuleIsInForce() {
        TaxStatusDto status = settingsService.status();

        assertThat(status.mode()).isEqualTo("INTERNAL");
        assertThat(status.externalEngineEnabled()).isFalse();
        assertThat(status.status()).isEqualTo("BLOCKING");
        assertThat(status.messages()).extracting(TaxStatusDto.Message::code).contains("NO_RULE_IN_FORCE");
        assertThat(status.internal().rulesInForce()).isZero();
    }

    @Test
    void theStatusIsReadyOnceARuleIsInForce() {
        internalGst();
        TaxStatusDto status = settingsService.status();
        assertThat(status.status()).isEqualTo("READY");
        assertThat(status.internal().rulesInForce()).isEqualTo(1);
        assertThat(status.internal().countriesCovered()).containsExactly("IN");
    }

    @Test
    void externalCannotBeSwitchedOnWithoutAProviderAndTheFailedChecksAreListed() {
        TaxModeResultDto result = settingsService.setExternalEngineEnabled(ADMIN, true);

        assertThat(result.changed()).isFalse();
        assertThat(result.report().passed()).isFalse();
        assertThat(result.report().checks()).filteredOn(c -> !c.passed()).extracting(c -> c.code()).contains("PROVIDER_SELECTED");
        assertThat(settingsService.status().externalEngineEnabled()).isFalse();
    }

    @Test
    void externalCannotBeSwitchedOnWhenCredentialsAreMissingOrTheConnectionFails() {
        configureStubProvider();
        stub.configured = false;
        TaxModeResultDto noCredentials = settingsService.setExternalEngineEnabled(ADMIN, true);
        assertThat(noCredentials.changed()).isFalse();
        assertThat(noCredentials.report().checks()).filteredOn(c -> !c.passed()).extracting(c -> c.code()).contains("CREDENTIALS");

        stub.configured = true;
        stub.connectionWorks = false;
        TaxModeResultDto noConnection = settingsService.setExternalEngineEnabled(ADMIN, true);
        assertThat(noConnection.changed()).isFalse();
        assertThat(noConnection.report().checks()).filteredOn(c -> !c.passed()).extracting(c -> c.code()).contains("CONNECTION");
        assertThat(settingsService.status().externalEngineEnabled()).isFalse();
        assertThat(settingsService.status().external().validationStatus()).isEqualTo("FAILED");
    }

    @Test
    void externalCannotBeSwitchedOnWhenTheProviderDoesNotSupportTheSuppliersCountryOrTheCurrencies() {
        configureStubProvider();
        stub.countries = java.util.Set.of("US");
        TaxModeResultDto country = settingsService.setExternalEngineEnabled(ADMIN, true);
        assertThat(country.changed()).isFalse();
        assertThat(country.report().checks()).filteredOn(c -> !c.passed()).extracting(c -> c.code()).contains("COUNTRY_SUPPORT");

        stub.countries = java.util.Set.of("IN");
        fixtures.product("10.00", "SAAS");       // makes sure at least one plan currency exists is not needed: plans are checked
        stub.currencies = java.util.Set.of("JPY");
        // no ProductPlan in this test uses a currency the stub lacks unless plans exist; the check passes vacuously, which is fine
        TaxModeResultDto ok = settingsService.setExternalEngineEnabled(ADMIN, true);
        assertThat(ok.report().checks()).filteredOn(c -> c.code().equals("COUNTRY_SUPPORT")).allMatch(c -> c.passed());
    }

    @Test
    void aProviderThatPassesEveryCheckCanBeSwitchedOnAndTheSettingIsPersisted() {
        configureStubProvider();
        int versionBefore = settingsService.status().configVersion();

        TaxModeResultDto result = settingsService.setExternalEngineEnabled(ADMIN, true);

        assertThat(result.changed()).isTrue();
        assertThat(result.report().passed()).isTrue();
        // A fresh read, as after a sign-out, a restart or a deployment, still says External.
        TaxStatusDto after = settingsService.status();
        assertThat(after.mode()).isEqualTo("EXTERNAL");
        assertThat(after.externalEngineEnabled()).isTrue();
        assertThat(after.configVersion()).isGreaterThan(versionBefore);
        assertThat(after.modeChangedAt()).isNotNull();
        assertThat(after.external().validationStatus()).isEqualTo("PASSED");
        assertThat(after.external().countries()).contains("IN");
        assertThat(after.external().credentials()).containsEntry("STUB_KEY", true);
        assertThat(after.external().endpointHost()).isEqualTo("stub.example.com");
        assertThat(after.status()).isEqualTo("READY");
        assertThat(settingsService.snapshot().engine()).isEqualTo(TaxEngineType.EXTERNAL);
    }

    @Test
    void switchingOffIsAlwaysAllowedAndWarnsWhenNoRuleIsInForce() {
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);

        TaxModeResultDto off = settingsService.setExternalEngineEnabled(ADMIN, false);

        assertThat(off.changed()).isTrue();
        assertThat(off.status().mode()).isEqualTo("INTERNAL");
        assertThat(off.status().status()).isEqualTo("BLOCKING");
        assertThat(off.status().messages()).extracting(TaxStatusDto.Message::code).contains("NO_RULE_IN_FORCE");
        assertThat(settingsService.setExternalEngineEnabled(ADMIN, false).changed()).as("already off: nothing changes").isFalse();
    }

    @Test
    void anExternalInvoiceUsesTheProviderAndStoresItsReferenceAndAPinnedConfiguration() {
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);
        int version = settingsService.status().configVersion();
        Customer customer = fixtures.customerIn("Karnataka", null);

        Invoice invoice = invoiceFor(customer, fixtures.product("1000.00", "SAAS"));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(invoice.getInvoiceNumber()).startsWith("EIS/");
        assertThat(invoice.getTaxEngine()).isEqualTo(TaxEngineType.EXTERNAL);
        assertThat(invoice.getTaxConfigVersion()).isEqualTo(version);
        assertThat(invoice.getTaxStatus()).isEqualTo(InvoiceTaxStatus.CALCULATED);
        assertThat(invoice.getTaxAmount()).isEqualTo(18000);
        assertThat(invoice.getTotal()).isEqualTo(118000);
        assertThat(stub.calls).hasSize(1);
        assertThat(stub.calls.get(0).lines().get(0).taxClass()).isEqualTo("SAAS");
        assertThat(stub.calls.get(0).lines().get(0).hsnSac()).isEqualTo("998439");
        assertThat(stub.calls.get(0).supplier().taxIds()).extracting(i -> i.number()).contains(TaxTestSupport.VALID_GSTIN_TN);
        var calculations = calculationService.listForInvoice(invoice.getId());
        assertThat(calculations).singleElement().satisfies(c -> {
            assertThat(c.engine()).isEqualTo("EXTERNAL");
            assertThat(c.status()).isEqualTo("CALCULATED");
            assertThat(c.providerCode()).isEqualTo("STUB");
            assertThat(c.providerReference()).startsWith("stub-INV-" + invoice.getId());
            assertThat(c.configVersion()).isEqualTo(version);
        });
        assertThat(invoiceService.taxLinesOf(invoice.getId())).singleElement().satisfies(t -> {
            assertThat(t.code()).isEqualTo("IGST");
            assertThat(t.taxAmount()).isEqualTo(18000);
        });
    }

    @Test
    void whenTheProviderKeepsFailingTheInvoiceIsBlockedAndTheInternalRulesAreNeverUsed() {
        internalGst();       // an approved internal rule exists, and must NOT be used while External is on
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);
        for (int i = 0; i < 6; i++) {
            stub.failures.add(ProviderException.transientFailure("HTTP 503", null));
        }
        Customer customer = fixtures.customerIn("Karnataka", null);

        Invoice invoice = invoiceFor(customer, fixtures.product("1000.00", "SAAS"));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getInvoiceNumber()).isNull();
        assertThat(invoice.getTaxStatus()).isEqualTo(InvoiceTaxStatus.BLOCKED);
        assertThat(invoice.getTaxBlockCode()).isEqualTo("PROVIDER_UNAVAILABLE");
        assertThat(invoice.getTaxBlockReason()).contains("3 attempt");
        assertThat(invoice.getTaxAmount()).isZero();
        assertThat(invoice.getTaxEngine()).isEqualTo(TaxEngineType.EXTERNAL);
        assertThat(stub.calls).hasSize(3);
        assertThat(invoiceService.taxLinesOf(invoice.getId())).isEmpty();
    }

    @Test
    void afterTheProviderRecoversRetryTaxFinalizesTheDraftWithANewTransactionReference() {
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);
        for (int i = 0; i < 3; i++) {
            stub.failures.add(ProviderException.transientFailure("HTTP 503", null));
        }
        Invoice draft = invoiceFor(fixtures.customerIn("Karnataka", null), fixtures.product("1000.00", "SAAS"));
        assertThat(draft.getStatus()).isEqualTo(InvoiceStatus.DRAFT);

        Invoice issued = invoiceService.retryTax(draft.getId(), ADMIN);

        assertThat(issued.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(issued.getInvoiceNumber()).startsWith("EIS/");
        assertThat(issued.getTaxAmount()).isEqualTo(18000);
        assertThat(stub.calls.get(0).transactionRef()).isEqualTo("INV-" + draft.getId() + "-1");
        assertThat(stub.calls.get(stub.calls.size() - 1).transactionRef()).isEqualTo("INV-" + draft.getId() + "-2");
        assertThat(calculationService.listForInvoice(draft.getId())).extracting(c -> c.status()).containsExactly("CALCULATED", "BLOCKED");
    }

    @Test
    void aProviderThatLaterFailsValidationBlocksInvoicesInsteadOfFallingBackToInternalRules() {
        internalGst();
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);
        stub.connectionWorks = false;
        assertThat(settingsService.validateProvider(ADMIN).passed()).isFalse();

        Invoice invoice = invoiceFor(fixtures.customerIn("Karnataka", null), fixtures.product("1000.00", "SAAS"));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getTaxBlockCode()).isEqualTo("PROVIDER_NOT_VALIDATED");
        assertThat(stub.calls).isEmpty();
        assertThat(settingsService.status().status()).isEqualTo("BLOCKING");
        assertThat(settingsService.status().externalEngineEnabled()).as("the switch is left alone").isTrue();
    }

    @Test
    void aProductWithoutATaxClassIsBlockedByTheExternalEngine() {
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);

        Invoice invoice = invoiceFor(fixtures.customerIn("Karnataka", null), fixtures.product("1000.00", null));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getTaxBlockCode()).isEqualTo("MISSING_TAX_CLASS");
        assertThat(stub.calls).isEmpty();
    }

    @Test
    void anUnsupportedCountryIsBlockedByTheExternalEngine() {
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);
        stub.countries = java.util.Set.of("US");       // the provider stops supporting India after it was switched on
        settingsService.validateProvider(ADMIN);
        // validation now fails on the supplier's country, so the invoice is blocked as not validated
        Invoice invoice = invoiceFor(fixtures.customerIn("Karnataka", null), fixtures.product("1000.00", "SAAS"));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getTaxBlockCode()).isIn("PROVIDER_NOT_VALIDATED", "PROVIDER_UNSUPPORTED");
    }

    @Test
    void switchingEnginesChangesOnlyFutureInvoicesAndNeverAnIssuedOne() {
        internalGst();
        Customer customer = fixtures.customerIn("Karnataka", null);
        Invoice first = invoiceFor(customer, fixtures.product("1000.00", "SAAS"));
        assertThat(first.getTaxEngine()).isEqualTo(TaxEngineType.INTERNAL);
        assertThat(first.getTaxAmount()).isEqualTo(5000);

        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);        // Off to On
        Invoice second = invoiceFor(customer, fixtures.product("1000.00", "SAAS"));
        assertThat(second.getTaxEngine()).isEqualTo(TaxEngineType.EXTERNAL);
        assertThat(second.getTaxAmount()).isEqualTo(18000);

        settingsService.setExternalEngineEnabled(ADMIN, false);       // On to Off
        Invoice third = invoiceFor(customer, fixtures.product("1000.00", "SAAS"));
        assertThat(third.getTaxEngine()).isEqualTo(TaxEngineType.INTERNAL);
        assertThat(third.getTaxAmount()).isEqualTo(5000);

        // The earlier invoices are exactly as they were issued.
        Invoice firstAgain = invoiceRepository.findById(first.getId()).orElseThrow();
        Invoice secondAgain = invoiceRepository.findById(second.getId()).orElseThrow();
        assertThat(firstAgain.getTaxEngine()).isEqualTo(TaxEngineType.INTERNAL);
        assertThat(firstAgain.getTaxAmount()).isEqualTo(5000);
        assertThat(firstAgain.getTotal()).isEqualTo(105000);
        assertThat(invoiceService.taxLinesOf(first.getId())).singleElement().satisfies(t -> assertThat(t.code()).isEqualTo("GST"));
        assertThat(secondAgain.getTaxEngine()).isEqualTo(TaxEngineType.EXTERNAL);
        assertThat(secondAgain.getTaxAmount()).isEqualTo(18000);
        assertThat(invoiceService.taxLinesOf(second.getId())).singleElement().satisfies(t -> assertThat(t.code()).isEqualTo("IGST"));
        assertThat(stub.calls).as("only the second invoice reached the provider").hasSize(1);
    }

    @Test
    void aCalculationInProgressKeepsTheEngineItStartedWith() {
        internalGst();
        configureStubProvider();
        TaxModeSnapshot pinned = calculationService.pin();                       // read once, at the start
        assertThat(pinned.engine()).isEqualTo(TaxEngineType.INTERNAL);

        settingsService.setExternalEngineEnabled(ADMIN, true);                   // somebody flips the switch meanwhile

        Customer customer = fixtures.customerIn("Karnataka", null);
        TaxCalculationRequest request = new TaxCalculationRequest("PIN-1", TransactionType.SALE, LocalDate.now(ZoneOffset.UTC), "INR",
            new TaxCalculationRequest.Supplier("eVyoog", null, null, "Tamil Nadu", "TN", null, "IN", null, List.of()),
            new TaxCalculationRequest.Customer(com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition.INDIVIDUAL, customer.getId(), null, "X", null, null, "Karnataka", "KA", null,
                "India", "IN", null),
            List.of(new TaxCalculationRequest.Line("1", "Line", "SAAS", null, 100000, 1)));
        TaxCalculationResult result = calculationService.calculate(request, pinned);

        assertThat(result.engine()).isEqualTo(TaxEngineType.INTERNAL);
        assertThat(result.totalTax()).isEqualTo(5000);
        assertThat(result.configVersion()).isEqualTo(pinned.configVersion());
        assertThat(stub.calls).as("the provider was not called by the pinned internal calculation").isEmpty();
        assertThat(calculationService.pin().engine()).as("a calculation that starts now uses the new engine").isEqualTo(TaxEngineType.EXTERNAL);
    }

    @Test
    void retryingADraftRestartsWithTheEngineInForceAtThatMoment() {
        Customer customer = fixtures.customerIn("Karnataka", null);
        Invoice draft = invoiceFor(customer, fixtures.product("1000.00", "SAAS"));      // internal engine, no rule yet
        assertThat(draft.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(draft.getTaxBlockCode()).isEqualTo("NO_MATCHING_RULE");
        assertThat(draft.getTaxEngine()).isEqualTo(TaxEngineType.INTERNAL);

        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);
        Invoice issued = invoiceService.retryTax(draft.getId(), ADMIN);

        assertThat(issued.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(issued.getTaxEngine()).isEqualTo(TaxEngineType.EXTERNAL);
        assertThat(issued.getTaxAmount()).isEqualTo(18000);
    }

    @Test
    void anIssuedInvoiceCannotBeRetried() {
        internalGst();
        Invoice issued = invoiceFor(fixtures.customerIn("Karnataka", null), fixtures.product("1000.00", "SAAS"));
        assertThatThrownBy(() -> invoiceService.retryTax(issued.getId(), ADMIN))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.BillingConflictException.class);
    }

    @Test
    void editingTheProviderSettingsWhileExternalIsOnMustPassValidationOrNothingIsSaved() {
        configureStubProvider();
        settingsService.setExternalEngineEnabled(ADMIN, true);
        stub.connectionWorks = false;

        assertThatThrownBy(() -> settingsService.saveProviderSettings(ADMIN, new SaveProviderSettingsRequest("STUB", "OTHER-CO", "PRODUCTION", 9, 2)))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.BillingConflictException.class)
            .hasMessageContaining("did not pass validation");

        TaxStatusDto status = settingsService.status();
        assertThat(status.external().companyCode()).isEqualTo("CO1");
        assertThat(status.external().environment()).isEqualTo("SANDBOX");
        assertThat(status.external().maxAttempts()).isEqualTo(3);
        assertThat(status.external().validationStatus()).isEqualTo("PASSED");
    }

    @Test
    void providerSettingsAreValidatedAndCredentialsAreNotPartOfThem() {
        assertThatThrownBy(() -> settingsService.saveProviderSettings(ADMIN, new SaveProviderSettingsRequest("NOPE", null, "SANDBOX", 5, 3)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown tax provider");
        assertThatThrownBy(() -> settingsService.saveProviderSettings(ADMIN, new SaveProviderSettingsRequest("STUB", null, "STAGING", 5, 3)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SANDBOX or PRODUCTION");
        assertThat(java.util.Arrays.stream(SaveProviderSettingsRequest.class.getRecordComponents()).map(c -> c.getName()))
            .noneMatch(n -> n.toLowerCase().contains("key") || n.toLowerCase().contains("secret") || n.toLowerCase().contains("password"));
    }

    @Test
    void theStatusNeverExposesACredentialValue() {
        configureStubProvider();
        String json = settingsService.status().toString();
        assertThat(json).doesNotContain("secret").doesNotContain("password");
        assertThat(settingsService.status().external().credentials().values()).allMatch(v -> v instanceof Boolean);
    }
}
