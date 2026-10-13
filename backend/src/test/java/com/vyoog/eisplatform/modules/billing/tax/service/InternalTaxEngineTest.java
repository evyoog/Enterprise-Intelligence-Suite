package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import com.vyoog.eisplatform.modules.billing.tax.calc.BlockCode;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxExemption;
import com.vyoog.eisplatform.modules.billing.tax.model.ExemptionStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRule;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TransactionType;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxExemptionRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport.comp;
import static org.assertj.core.api.Assertions.assertThat;

/** REQ-BIL-002.10 to .20: the internal engine against rules in the database. The platform ships no rate, so every rule here is
 * set up by the test, as an administrator would. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TaxTestSupport.class)
class InternalTaxEngineTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 13);

    @Autowired private InternalTaxEngine engine;
    @Autowired private TaxSettingsService settingsService;
    @Autowired private TaxRuleRepository ruleRepository;
    @Autowired private TaxExemptionRepository exemptionRepository;
    @Autowired private TaxTestSupport tax;

    @BeforeEach
    void cleanRules() {
        exemptionRepository.deleteAll();
        ruleRepository.deleteAll();
        tax.internalEngine();
    }

    private TaxCalculationRequest.Supplier seller(String state) {
        return new TaxCalculationRequest.Supplier("eVyoog", "1 St", "Chennai", "Tamil Nadu", state, "600001", "IN", "33ABCDE1234F1Z5", List.of());
    }

    private TaxCalculationRequest.Customer customer(String country, String stateKey, String taxId, CustomerTypeCondition kind) {
        return new TaxCalculationRequest.Customer(kind, 11L, null, "Customer", "Addr", "City", stateKey, stateKey, "000000", country, country, taxId);
    }

    private TaxCalculationRequest request(TaxCalculationRequest.Supplier seller, TaxCalculationRequest.Customer customer, String taxClass, long... amounts) {
        java.util.ArrayList<TaxCalculationRequest.Line> lines = new java.util.ArrayList<>();
        for (int i = 0; i < amounts.length; i++) {
            lines.add(new TaxCalculationRequest.Line(String.valueOf(i + 1), "Line " + (i + 1), taxClass, null, amounts[i], 1));
        }
        return new TaxCalculationRequest("T-1", TransactionType.SALE, TODAY, "INR", seller, customer, lines);
    }

    private TaxCalculationResult calc(TaxCalculationRequest request) {
        return engine.calculate(request, settingsService.snapshot());
    }

    /** India GST as an administrator would configure it: within state CGST + SGST, between states IGST. The rates are test data. */
    private void indiaGst() {
        tax.approvedRule("GST within state", "IN", null, SupplyType.INTRA_STATE, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null,
            comp("CGST", "Central GST", "9"), comp("SGST", "State GST", "9"));
        tax.approvedRule("GST between states", "IN", null, SupplyType.INTER_STATE, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null,
            comp("IGST", "Integrated GST", "18"));
    }

    @Test
    void withinTheSellersStateTheTaxIsCgstAndSgst() {
        indiaGst();
        TaxCalculationResult r = calc(request(seller("TN"), customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 100000));

        assertThat(r.isCalculated()).isTrue();
        assertThat(r.lines().get(0).components()).extracting(TaxCalculationResult.Component::code).containsExactly("CGST", "SGST");
        assertThat(r.lines().get(0).components()).extracting(TaxCalculationResult.Component::taxAmount).containsExactly(9000L, 9000L);
        assertThat(r.totalTax()).isEqualTo(18000);
        assertThat(r.placeOfSupply()).isEqualTo("Tamil Nadu, India");
    }

    @Test
    void betweenStatesTheTaxIsIgst() {
        indiaGst();
        TaxCalculationResult r = calc(request(seller("TN"), customer("IN", "KA", null, CustomerTypeCondition.INDIVIDUAL), null, 100000));

        assertThat(r.lines().get(0).components()).singleElement().satisfies(c -> {
            assertThat(c.code()).isEqualTo("IGST");
            assertThat(c.taxAmount()).isEqualTo(18000);
            assertThat(c.rate()).isEqualByComparingTo("18");
            assertThat(c.ruleId()).isNotNull();
            assertThat(c.ruleVersion()).isEqualTo(1);
        });
        assertThat(r.placeOfSupply()).isEqualTo("Karnataka, India");
    }

    @Test
    void aValidGstinSetsThePlaceOfSupplyOverTheBillingState() {
        indiaGst();
        // The customer's billing address is in Tamil Nadu but the GSTIN is registered in Karnataka (29).
        TaxCalculationResult r = calc(request(seller("TN"), customer("IN", "TN", "29ABCDE1234F1Z5", CustomerTypeCondition.BUSINESS), null, 100000));

        assertThat(r.lines().get(0).components()).extracting(TaxCalculationResult.Component::code).containsExactly("IGST");
        assertThat(r.placeOfSupply()).isEqualTo("Karnataka, India");
    }

    @Test
    void anInvalidGstinIsNotUsedForThePlaceOfSupply() {
        indiaGst();
        TaxCalculationResult r = calc(request(seller("TN"), customer("IN", "TN", "NOT-A-GSTIN", CustomerTypeCondition.BUSINESS), null, 100000));
        assertThat(r.lines().get(0).components()).extracting(TaxCalculationResult.Component::code).containsExactly("CGST", "SGST");
    }

    @Test
    void whenTheStateIsMissingOnlyARuleThatDoesNotNeedItCanApply() {
        indiaGst();
        TaxCalculationResult r = calc(request(seller("TN"), customer("IN", null, null, CustomerTypeCondition.INDIVIDUAL), null, 100000));

        assertThat(r.isCalculated()).isFalse();
        assertThat(r.blockCode()).isEqualTo(BlockCode.NO_MATCHING_RULE);
        assertThat(r.blockMessage()).contains("place of supply could not be decided");

        tax.approvedRule("GST any supply", "IN", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("GST", "GST", "18"));
        TaxCalculationResult ok = calc(request(seller("TN"), customer("IN", null, null, CustomerTypeCondition.INDIVIDUAL), null, 100000));
        assertThat(ok.totalTax()).isEqualTo(18000);
    }

    @Test
    void noMatchingRuleBlocksAndNeverAssumesZero() {
        TaxCalculationResult r = calc(request(seller("TN"), customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 100000));

        assertThat(r.isCalculated()).isFalse();
        assertThat(r.blockCode()).isEqualTo(BlockCode.NO_MATCHING_RULE);
        assertThat(r.lines()).isEmpty();
        assertThat(r.blockMessage()).contains("IN").contains("Tamil Nadu");
    }

    @Test
    void aCountryWithoutRulesBlocksEvenWhenAnotherCountryHasThem() {
        indiaGst();
        TaxCalculationResult r = calc(request(seller("TN"), customer("US", "CA", null, CustomerTypeCondition.INDIVIDUAL), null, 100000));
        assertThat(r.blockCode()).isEqualTo(BlockCode.NO_MATCHING_RULE);
    }

    @Test
    void aMissingCustomerOrSupplierCountryBlocks() {
        indiaGst();
        var unknown = new TaxCalculationRequest.Customer(CustomerTypeCondition.INDIVIDUAL, 1L, null, "X", null, null, null, null, null, "Atlantis", null, null);
        TaxCalculationResult noCountry = calc(request(seller("TN"), unknown, null, 100));
        assertThat(noCountry.blockCode()).isEqualTo(BlockCode.MISSING_CUSTOMER_COUNTRY);
        assertThat(noCountry.blockMessage()).contains("Atlantis");

        var noSupplier = new TaxCalculationRequest.Supplier(null, null, null, null, null, null, null, null, List.of());
        assertThat(calc(request(noSupplier, customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 100)).blockCode())
            .isEqualTo(BlockCode.MISSING_SUPPLIER_PROFILE);
    }

    @Test
    void onlyApprovedActiveRulesInForceAreUsed() {
        TaxRule draft = tax.approvedRule("Not approved", "IN", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, TODAY.minusYears(1), null, comp("GST", "GST", "18"));
        TaxCalculationRequest req = request(seller("TN"), customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 100000);

        draft.setApprovalStatus(RuleApprovalStatus.DRAFT);
        ruleRepository.save(draft);
        assertThat(calc(req).blockCode()).as("draft").isEqualTo(BlockCode.NO_MATCHING_RULE);
        draft.setApprovalStatus(RuleApprovalStatus.PENDING_APPROVAL);
        ruleRepository.save(draft);
        assertThat(calc(req).blockCode()).as("pending").isEqualTo(BlockCode.NO_MATCHING_RULE);
        draft.setApprovalStatus(RuleApprovalStatus.REJECTED);
        ruleRepository.save(draft);
        assertThat(calc(req).blockCode()).as("rejected").isEqualTo(BlockCode.NO_MATCHING_RULE);
        draft.setApprovalStatus(RuleApprovalStatus.APPROVED);
        draft.setActive(false);
        ruleRepository.save(draft);
        assertThat(calc(req).blockCode()).as("deactivated").isEqualTo(BlockCode.NO_MATCHING_RULE);
        draft.setActive(true);
        ruleRepository.save(draft);
        assertThat(calc(req).isCalculated()).as("approved and active").isTrue();
    }

    @Test
    void effectiveDatesAreRespected() {
        TaxRule future = tax.approvedRule("Starts tomorrow", "IN", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, TODAY.plusDays(1), null, comp("GST", "GST", "18"));
        TaxCalculationRequest req = request(seller("TN"), customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 100000);
        assertThat(calc(req).blockCode()).as("not yet in force").isEqualTo(BlockCode.NO_MATCHING_RULE);

        future.setEffectiveFrom(TODAY);
        ruleRepository.save(future);
        assertThat(calc(req).isCalculated()).as("starts today").isTrue();

        future.setEffectiveFrom(TODAY.minusDays(10));
        future.setEffectiveTo(TODAY.minusDays(1));
        ruleRepository.save(future);
        assertThat(calc(req).blockCode()).as("ended yesterday").isEqualTo(BlockCode.NO_MATCHING_RULE);

        future.setEffectiveTo(TODAY);
        ruleRepository.save(future);
        assertThat(calc(req).isCalculated()).as("ends today, still in force").isTrue();
    }

    @Test
    void theMostSpecificRuleWins() {
        tax.approvedRule("Country wide", "US", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("TAX", "Sales tax", "5"));
        tax.approvedRule("California", "US", "CA", SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null,
            comp("STATE", "State tax", "6"), comp("COUNTY", "County tax", "1.25"));
        TaxCalculationResult ca = calc(request(seller("TN"), customer("US", "CA", null, CustomerTypeCondition.INDIVIDUAL), null, 100000));
        TaxCalculationResult ny = calc(request(seller("TN"), customer("US", "NY", null, CustomerTypeCondition.INDIVIDUAL), null, 100000));

        assertThat(ca.lines().get(0).components()).extracting(TaxCalculationResult.Component::code).containsExactly("STATE", "COUNTY");
        assertThat(ca.totalTax()).isEqualTo(6000 + 1250);
        assertThat(ny.lines().get(0).components()).extracting(TaxCalculationResult.Component::code).containsExactly("TAX");
        assertThat(ca.placeOfSupply()).isEqualTo("California, United States");
    }

    @Test
    void twoDifferentRulesOfTheSameSpecificityAreAmbiguous() {
        tax.approvedRule("Rule A", "US", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("A", "A", "5"));
        tax.approvedRule("Rule B", "US", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("B", "B", "7"));

        TaxCalculationResult r = calc(request(seller("TN"), customer("US", "CA", null, CustomerTypeCondition.INDIVIDUAL), null, 100000));

        assertThat(r.blockCode()).isEqualTo(BlockCode.AMBIGUOUS_RULES);
        assertThat(r.blockMessage()).contains("Rule A").contains("Rule B");
    }

    @Test
    void theTaxClassSelectsTheRuleAndAnUnclassifiedProductOnlyMatchesAnyClassRules() {
        tax.approvedRule("Any class", "GB", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("VAT", "VAT", "20"));
        tax.approvedRule("Books", "GB", null, SupplyType.ANY, "BOOKS", TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("VAT", "VAT", "0"));

        assertThat(calc(request(seller("TN"), customer("GB", null, null, CustomerTypeCondition.BUSINESS), "BOOKS", 10000)).totalTax()).isZero();
        assertThat(calc(request(seller("TN"), customer("GB", null, null, CustomerTypeCondition.BUSINESS), "SAAS", 10000)).totalTax()).isEqualTo(2000);
        assertThat(calc(request(seller("TN"), customer("GB", null, null, CustomerTypeCondition.BUSINESS), null, 10000)).totalTax()).isEqualTo(2000);
    }

    @Test
    void aClassSpecificRuleDoesNotApplyToAnUnclassifiedProduct() {
        tax.approvedRule("Only SaaS", "GB", null, SupplyType.ANY, "SAAS", TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("VAT", "VAT", "20"));
        assertThat(calc(request(seller("TN"), customer("GB", null, null, CustomerTypeCondition.BUSINESS), null, 10000)).blockCode()).isEqualTo(BlockCode.NO_MATCHING_RULE);
    }

    @Test
    void customerTypeAndTaxIdConditionsNarrowARule() {
        TaxRule business = tax.approvedRule("Business only", "DE", null, SupplyType.ANY, null, TaxTreatment.REVERSE_CHARGE, LocalDate.of(2025, 1, 1), null);
        business.setCustomerType(CustomerTypeCondition.BUSINESS);
        business.setTaxIdCondition(com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition.PROVIDED);
        ruleRepository.save(business);
        tax.approvedRule("Everyone else", "DE", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("VAT", "VAT", "19"));

        TaxCalculationResult b2b = calc(request(seller("TN"), customer("DE", null, "DE 123456789", CustomerTypeCondition.BUSINESS), null, 10000));
        assertThat(b2b.lines().get(0).components()).singleElement().satisfies(c -> {
            assertThat(c.treatment()).isEqualTo(TaxTreatment.REVERSE_CHARGE);
            assertThat(c.taxAmount()).isZero();
        });
        assertThat(calc(request(seller("TN"), customer("DE", null, null, CustomerTypeCondition.BUSINESS), null, 10000)).totalTax()).as("no tax ID").isEqualTo(1900);
        assertThat(calc(request(seller("TN"), customer("DE", null, "DE 123456789", CustomerTypeCondition.INDIVIDUAL), null, 10000)).totalTax()).as("individual").isEqualTo(1900);
    }

    @Test
    void crossBorderSuppliesUseTheirOwnRule() {
        tax.approvedRule("Export of services", "US", null, SupplyType.CROSS_BORDER, null, TaxTreatment.ZERO_RATED, LocalDate.of(2025, 1, 1), null);
        TaxCalculationResult r = calc(request(seller("TN"), customer("US", "CA", null, CustomerTypeCondition.BUSINESS), null, 500000));

        assertThat(r.isCalculated()).isTrue();
        assertThat(r.totalTax()).isZero();
        assertThat(r.lines().get(0).components()).singleElement().satisfies(c -> {
            assertThat(c.code()).isEqualTo("ZERO_RATED");
            assertThat(c.treatment()).isEqualTo(TaxTreatment.ZERO_RATED);
            assertThat(c.taxableAmount()).isEqualTo(500000);
            assertThat(c.ruleId()).isNotNull();
        });
    }

    @Test
    void severalLinesAreTaxedAndRoundedPerLineAndPerComponent() {
        indiaGst();
        TaxCalculationResult r = calc(request(seller("TN"), customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 1001, 333));

        // 1001 x 9% = 90.09 -> 90 ; 333 x 9% = 29.97 -> 30
        assertThat(r.lines().get(0).components()).extracting(TaxCalculationResult.Component::taxAmount).containsExactly(90L, 90L);
        assertThat(r.lines().get(1).components()).extracting(TaxCalculationResult.Component::taxAmount).containsExactly(30L, 30L);
        assertThat(r.totalTax()).isEqualTo(240);
    }

    @Test
    void halfARoundsUp() {
        tax.approvedRule("Half", "FR", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("VAT", "VAT", "10"));
        // 5 x 10% = 0.5 -> 1
        assertThat(calc(request(seller("TN"), customer("FR", null, null, CustomerTypeCondition.INDIVIDUAL), null, 5)).totalTax()).isEqualTo(1);
        // 4 x 10% = 0.4 -> 0, an explicit zero amount from a real rate
        assertThat(calc(request(seller("TN"), customer("FR", null, null, CustomerTypeCondition.INDIVIDUAL), null, 4)).totalTax()).isZero();
    }

    @Test
    void aCompoundComponentIsChargedOnTheBasePlusTheComponentsBeforeIt() {
        tax.approvedRule("Compound", "CA", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null,
            comp("GST", "Federal", "5"), compound(comp("PST", "Provincial", "10")));
        TaxCalculationResult r = calc(request(seller("TN"), customer("CA", null, null, CustomerTypeCondition.INDIVIDUAL), null, 10000));

        // 5% of 10000 = 500; 10% of (10000 + 500) = 1050
        assertThat(r.lines().get(0).components()).extracting(TaxCalculationResult.Component::taxAmount).containsExactly(500L, 1050L);
        assertThat(r.lines().get(0).components().get(1).taxableAmount()).isEqualTo(10500);
    }

    private static com.vyoog.eisplatform.modules.billing.tax.model.TaxRuleComponent compound(com.vyoog.eisplatform.modules.billing.tax.model.TaxRuleComponent c) {
        c.setCompound(true);
        return c;
    }

    @Test
    void anApprovedExemptionMakesTheResultAnExplicitExempt() {
        indiaGst();
        TaxExemption e = new TaxExemption();
        e.setOwnerCustomerId(11L);
        e.setCountryCode("IN");
        e.setReason("SEZ unit");
        e.setCertificateNumber("SEZ-42");
        e.setEffectiveFrom(TODAY.minusDays(1));
        e.setStatus(ExemptionStatus.PENDING_APPROVAL);
        e = exemptionRepository.save(e);
        TaxCalculationRequest req = request(seller("TN"), customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 100000);
        assertThat(calc(req).totalTax()).as("pending exemptions are ignored").isEqualTo(18000);

        e.setStatus(ExemptionStatus.APPROVED);
        exemptionRepository.save(e);
        TaxCalculationResult r = calc(req);
        assertThat(r.isCalculated()).isTrue();
        assertThat(r.totalTax()).isZero();
        assertThat(r.lines().get(0).components()).singleElement().satisfies(c -> {
            assertThat(c.treatment()).isEqualTo(TaxTreatment.EXEMPT);
            assertThat(c.note()).contains("SEZ unit").contains("SEZ-42");
        });

        e.setEffectiveTo(TODAY.minusDays(1));
        exemptionRepository.save(e);
        assertThat(calc(req).totalTax()).as("expired exemptions are ignored").isEqualTo(18000);
    }

    @Test
    void anExemptionOnlyAppliesToItsOwnerAndCountry() {
        indiaGst();
        TaxExemption e = new TaxExemption();
        e.setOwnerCustomerId(999L);
        e.setCountryCode("IN");
        e.setReason("Other customer");
        e.setEffectiveFrom(TODAY.minusDays(1));
        e.setStatus(ExemptionStatus.APPROVED);
        exemptionRepository.save(e);
        assertThat(calc(request(seller("TN"), customer("IN", "TN", null, CustomerTypeCondition.INDIVIDUAL), null, 100000)).totalTax()).isEqualTo(18000);
    }

    @Test
    void theSameRuleFamilyIsUsedAtMostOncePerVersion() {
        TaxRule v1 = tax.approvedRule("Versioned", "IT", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("VAT", "VAT", "20"));
        TaxRule v2 = tax.approvedRule("Versioned v2", "IT", null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2025, 1, 1), null, comp("VAT", "VAT", "22"));
        v2.setRuleKey(v1.getRuleKey());
        v2.setVersion(2);
        ruleRepository.save(v2);

        // Two versions of one family can never both be in force; if data says so, only the newest counts and the result is not ambiguous.
        TaxCalculationResult r = calc(request(seller("TN"), customer("IT", null, null, CustomerTypeCondition.INDIVIDUAL), null, 10000));
        assertThat(r.isCalculated()).isTrue();
        assertThat(r.totalTax()).isEqualTo(2200);
    }
}
