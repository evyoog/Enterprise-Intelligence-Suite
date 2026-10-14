package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.modules.billing.tax.service.StubTaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxIntegrationSupport;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import com.vyoog.eisplatform.modules.billing.tax.dto.ComponentDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveApprovalPolicyRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxClassRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxExemptionRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxRegistrationRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxRuleRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxRuleDto;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-BIL-002.10 to .14, .18, .19, .21, .37: rule life cycle, versions, maker and checker, validation, classes, registrations,
 * exemptions and the preview. */
@SpringBootTest
@ActiveProfiles("test")
@Import({TaxTestSupport.class, TaxIntegrationSupport.class, StubTaxProvider.class})
class TaxRuleServiceTest {

    private static final String MAKER = "admin-maker";
    private static final String CHECKER = "admin-checker";
    private static final LocalDate TODAY = LocalDate.now(ZoneOffset.UTC);

    @Autowired private TaxRuleService rules;
    @Autowired private TaxClassService classes;
    @Autowired private TaxRegistrationService registrations;
    @Autowired private TaxExemptionService exemptions;
    @Autowired private TaxSettingsService settings;
    @Autowired private TaxRuleRepository ruleRepository;
    @Autowired private TaxIntegrationSupport fixtures;
    @Autowired private TaxTestSupport tax;

    @BeforeEach
    void reset() {
        fixtures.resetAll();
        fixtures.supplier();
    }

    private static ComponentDto c(String code, String rate) {
        return new ComponentDto(code, code + " tax", new BigDecimal(rate), false);
    }

    private SaveTaxRuleRequest india(String name, String state, SupplyType supply, LocalDate from, LocalDate to, ComponentDto... components) {
        return new SaveTaxRuleRequest(name, "IN", state, "India", TaxType.GST, null, CustomerTypeCondition.ANY, supply, TaxIdCondition.ANY,
            TaxTreatment.TAXABLE, from, to, null, List.of(components));
    }

    private TaxRuleDto approved(String name, SupplyType supply, LocalDate from, ComponentDto... components) {
        TaxRuleDto r = rules.create(MAKER, india(name, null, supply, from, null, components));
        rules.submit(MAKER, r.id());
        return rules.approve(CHECKER, r.id());
    }

    @Test
    void aRuleGoesFromDraftThroughApprovalToActive() {
        TaxRuleDto draft = rules.create(MAKER, india("GST within state", null, SupplyType.INTRA_STATE, TODAY, null, c("CGST", "9"), c("sgst", "9")));

        assertThat(draft.approvalStatus()).isEqualTo(RuleApprovalStatus.DRAFT);
        assertThat(draft.active()).isFalse();
        assertThat(draft.inForce()).isFalse();
        assertThat(draft.version()).isEqualTo(1);
        assertThat(draft.components()).extracting(ComponentDto::code).containsExactly("CGST", "SGST");   // upper cased, in order
        assertThat(draft.totalRate()).isEqualByComparingTo("18");

        assertThat(rules.submit(MAKER, draft.id()).approvalStatus()).isEqualTo(RuleApprovalStatus.PENDING_APPROVAL);
        TaxRuleDto approved = rules.approve(CHECKER, draft.id());

        assertThat(approved.approvalStatus()).isEqualTo(RuleApprovalStatus.APPROVED);
        assertThat(approved.active()).isTrue();
        assertThat(approved.inForce()).isTrue();
        assertThat(approved.approvedAt()).isNotNull();
        assertThat(settings.status().internal().rulesInForce()).isEqualTo(1);
    }

    @Test
    void theAdministratorWhoEditedARuleCannotApproveItUnlessThePolicyAllowsIt() {
        TaxRuleDto draft = rules.create(MAKER, india("Maker checker", null, SupplyType.ANY, TODAY, null, c("GST", "18")));
        rules.submit(MAKER, draft.id());

        assertThatThrownBy(() -> rules.approve(MAKER, draft.id())).isInstanceOf(BillingConflictException.class).hasMessageContaining("different administrator");
        assertThatThrownBy(() -> rules.reject(MAKER, draft.id(), "no")).isInstanceOf(BillingConflictException.class);
        // Someone else edits it: that person can no longer approve it, the original maker now can.
        rules.reject(CHECKER, draft.id(), "Wrong rate");
        rules.update(CHECKER, draft.id(), india("Maker checker", null, SupplyType.ANY, TODAY, null, c("GST", "18")));
        rules.submit(CHECKER, draft.id());
        assertThatThrownBy(() -> rules.approve(CHECKER, draft.id())).isInstanceOf(BillingConflictException.class);
        assertThat(rules.approve(MAKER, draft.id()).approvalStatus()).isEqualTo(RuleApprovalStatus.APPROVED);
    }

    @Test
    void theSelfApprovalPolicyLetsASingleAdministratorWork() {
        settings.saveApprovalPolicy(MAKER, new SaveApprovalPolicyRequest(true));
        TaxRuleDto draft = rules.create(MAKER, india("Solo", null, SupplyType.ANY, TODAY, null, c("GST", "18")));
        rules.submit(MAKER, draft.id());

        assertThat(rules.approve(MAKER, draft.id()).approvalStatus()).isEqualTo(RuleApprovalStatus.APPROVED);
        assertThat(settings.status().allowSelfApproval()).isTrue();
    }

    @Test
    void aRejectedRuleKeepsItsReasonCanBeEditedAndResubmitted() {
        TaxRuleDto draft = rules.create(MAKER, india("To reject", null, SupplyType.ANY, TODAY, null, c("GST", "18")));
        rules.submit(MAKER, draft.id());

        TaxRuleDto rejected = rules.reject(CHECKER, draft.id(), "Rate is out of date");
        assertThat(rejected.approvalStatus()).isEqualTo(RuleApprovalStatus.REJECTED);
        assertThat(rejected.rejectionReason()).isEqualTo("Rate is out of date");

        TaxRuleDto edited = rules.update(MAKER, draft.id(), india("To reject", null, SupplyType.ANY, TODAY, null, c("GST", "12")));
        assertThat(edited.approvalStatus()).isEqualTo(RuleApprovalStatus.DRAFT);
        assertThat(edited.rejectionReason()).isNull();
        assertThat(edited.components().get(0).rate()).isEqualByComparingTo("12");
    }

    @Test
    void anApprovedRuleCannotBeEditedOrDeletedButDraftsCanBeDeleted() {
        TaxRuleDto approved = approved("Locked", SupplyType.ANY, TODAY, c("GST", "18"));

        assertThatThrownBy(() -> rules.update(MAKER, approved.id(), india("Locked", null, SupplyType.ANY, TODAY, null, c("GST", "28"))))
            .isInstanceOf(BillingConflictException.class).hasMessageContaining("new version");
        assertThatThrownBy(() -> rules.delete(MAKER, approved.id())).isInstanceOf(BillingConflictException.class);

        TaxRuleDto draft = rules.create(MAKER, india("Throwaway", "TN", SupplyType.ANY, TODAY, null, c("GST", "1")));
        rules.delete(MAKER, draft.id());
        assertThat(ruleRepository.findById(draft.id())).isEmpty();
    }

    @Test
    void aNewVersionIsADraftAndApprovingItClosesThePreviousVersionTheDayBefore() {
        TaxRuleDto v1 = approved("Versioned", SupplyType.ANY, TODAY.minusDays(30), c("GST", "18"));

        TaxRuleDto v2draft = rules.newVersion(MAKER, v1.id());
        assertThat(v2draft.version()).isEqualTo(2);
        assertThat(v2draft.ruleKey()).isEqualTo(v1.ruleKey());
        assertThat(v2draft.approvalStatus()).isEqualTo(RuleApprovalStatus.DRAFT);
        assertThat(v2draft.components()).hasSize(1);
        assertThat(v2draft.effectiveFrom()).isEqualTo(TODAY);
        assertThatThrownBy(() -> rules.newVersion(MAKER, v1.id())).as("one open version at a time").isInstanceOf(BillingConflictException.class);

        LocalDate start = TODAY.plusDays(10);
        rules.update(MAKER, v2draft.id(), india("Versioned", null, SupplyType.ANY, start, null, c("GST", "20")));
        rules.submit(MAKER, v2draft.id());
        TaxRuleDto v2 = rules.approve(CHECKER, v2draft.id());

        TaxRuleDto closed = rules.get(v1.id());
        assertThat(closed.effectiveTo()).isEqualTo(start.minusDays(1));
        assertThat(closed.latestVersion()).isFalse();
        assertThat(closed.inForce()).as("version 1 is still in force until the day before version 2").isTrue();
        assertThat(v2.latestVersion()).isTrue();
        assertThat(v2.inForce()).isFalse();
        assertThat(rules.list()).filteredOn(r -> r.ruleKey().equals(v1.ruleKey())).extracting(TaxRuleDto::version).containsExactly(2, 1);
    }

    @Test
    void aNewVersionMustStartAfterThePreviousVersionStarted() {
        TaxRuleDto v1 = approved("Too early", SupplyType.ANY, TODAY.minusDays(5), c("GST", "18"));
        TaxRuleDto v2 = rules.newVersion(MAKER, v1.id());
        rules.update(MAKER, v2.id(), india("Too early", null, SupplyType.ANY, TODAY.minusDays(5), null, c("GST", "20")));
        rules.submit(MAKER, v2.id());

        assertThatThrownBy(() -> rules.approve(CHECKER, v2.id())).isInstanceOf(BillingConflictException.class).hasMessageContaining("start after");
    }

    @Test
    void anOverlappingRuleOfTheSameScopeCannotBeApproved() {
        approved("First", SupplyType.INTRA_STATE, TODAY, c("GST", "18"));
        TaxRuleDto second = rules.create(MAKER, india("Second", null, SupplyType.INTRA_STATE, TODAY.plusDays(5), null, c("GST", "12")));
        rules.submit(MAKER, second.id());

        assertThatThrownBy(() -> rules.approve(CHECKER, second.id())).isInstanceOf(BillingConflictException.class).hasMessageContaining("First");

        // Different scope, or dates that do not overlap, are fine.
        TaxRuleDto otherScope = rules.create(MAKER, india("Inter", null, SupplyType.INTER_STATE, TODAY, null, c("IGST", "18")));
        rules.submit(MAKER, otherScope.id());
        assertThat(rules.approve(CHECKER, otherScope.id()).approvalStatus()).isEqualTo(RuleApprovalStatus.APPROVED);
    }

    @Test
    void deactivatingAndActivatingARuleControlsWhetherItIsUsed() {
        TaxRuleDto r = approved("Toggle", SupplyType.ANY, TODAY, c("GST", "18"));

        TaxRuleDto off = rules.deactivate(CHECKER, r.id());
        assertThat(off.active()).isFalse();
        assertThat(off.inForce()).isFalse();
        assertThat(settings.status().internal().rulesInForce()).isZero();
        assertThat(rules.activate(CHECKER, r.id()).inForce()).isTrue();

        TaxRuleDto draft = rules.create(MAKER, india("Draft", "TN", SupplyType.ANY, TODAY, null, c("GST", "1")));
        assertThatThrownBy(() -> rules.activate(MAKER, draft.id())).isInstanceOf(BillingConflictException.class);
        assertThatThrownBy(() -> rules.deactivate(MAKER, draft.id())).isInstanceOf(BillingConflictException.class);
    }

    @Test
    void rulesAreValidatedWhenSaved() {
        assertThatThrownBy(() -> rules.create(MAKER, india("No components", null, SupplyType.ANY, TODAY, null)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least one component");
        assertThatThrownBy(() -> rules.create(MAKER, india("Duplicate", null, SupplyType.ANY, TODAY, null, c("GST", "9"), c("gst", "9"))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("used twice");
        assertThatThrownBy(() -> rules.create(MAKER, india("Bad state", "Narnia", SupplyType.ANY, TODAY, null, c("GST", "9"))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not a known state");
        assertThatThrownBy(() -> rules.create(MAKER, india("Backwards", null, SupplyType.ANY, TODAY, TODAY.minusDays(1), c("GST", "9"))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("end date");
        assertThatThrownBy(() -> rules.create(MAKER, new SaveTaxRuleRequest("Bad country", "ZZ", null, null, TaxType.VAT, null, null, null, null,
            TaxTreatment.TAXABLE, TODAY, null, null, List.of(c("VAT", "20")))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown country");
        assertThatThrownBy(() -> rules.create(MAKER, new SaveTaxRuleRequest("Exempt with rate", "IN", null, null, TaxType.GST, null, null, null, null,
            TaxTreatment.EXEMPT, TODAY, null, null, List.of(c("GST", "9")))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no components");
        assertThatThrownBy(() -> rules.create(MAKER, new SaveTaxRuleRequest("Unknown class", "IN", null, null, TaxType.GST, "NOPE", null, null, null,
            TaxTreatment.TAXABLE, TODAY, null, null, List.of(c("GST", "9")))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown tax class");
    }

    @Test
    void aStateIsStoredAsItsCatalogKeyAndANonTaxableRuleHasNoComponents() {
        TaxRuleDto state = rules.create(MAKER, india("Tamil Nadu only", "tamil nadu", SupplyType.ANY, TODAY, null, c("GST", "18")));
        assertThat(state.stateCode()).isEqualTo("TN");
        assertThat(state.stateName()).isEqualTo("Tamil Nadu");

        TaxRuleDto zero = rules.create(MAKER, new SaveTaxRuleRequest("Export", "US", null, "United States", TaxType.SALES_TAX, null, CustomerTypeCondition.BUSINESS,
            SupplyType.CROSS_BORDER, TaxIdCondition.ANY, TaxTreatment.ZERO_RATED, TODAY, null, "Zero rated export of services", null));
        assertThat(zero.components()).isEmpty();
        assertThat(zero.countryName()).isEqualTo("United States");
    }

    @Test
    void theTaxClassOfARuleMustExistAndIsStoredUpperCase() {
        fixtures.taxClass("SAAS", "998439");
        TaxRuleDto r = rules.create(MAKER, new SaveTaxRuleRequest("Class rule", "IN", null, null, TaxType.GST, "saas", null, null, null,
            TaxTreatment.TAXABLE, TODAY, null, null, List.of(c("GST", "18"))));
        assertThat(r.productClassCode()).isEqualTo("SAAS");
    }

    @Test
    void thePreviewShowsTheComponentsOrTheBlockReasonAndCreatesNothing() {
        approved("Within", SupplyType.INTRA_STATE, TODAY.minusDays(1), c("CGST", "9"), c("SGST", "9"));
        approved("Between", SupplyType.INTER_STATE, TODAY.minusDays(1), c("IGST", "18"));
        long invoicesBefore = 0;

        TaxPreviewDto within = rules.preview(new TaxPreviewRequest(100000L, "inr", "India", "Tamil Nadu", null, null, null, null));
        assertThat(within.status()).isEqualTo("CALCULATED");
        assertThat(within.components()).extracting(TaxPreviewDto.Component::code).containsExactly("CGST", "SGST");
        assertThat(within.totalTax()).isEqualTo(18000);
        assertThat(within.total()).isEqualTo(118000);
        assertThat(within.placeOfSupply()).isEqualTo("Tamil Nadu, India");

        TaxPreviewDto between = rules.preview(new TaxPreviewRequest(100000L, "INR", "IN", "Karnataka", null, CustomerTypeCondition.BUSINESS, "29ABCDE1234F1Z5", null));
        assertThat(between.components()).extracting(TaxPreviewDto.Component::code).containsExactly("IGST");

        TaxPreviewDto none = rules.preview(new TaxPreviewRequest(100000L, "INR", "France", null, null, null, null, null));
        assertThat(none.status()).isEqualTo("BLOCKED");
        assertThat(none.blockCode()).isEqualTo("NO_MATCHING_RULE");
        assertThat(none.total()).isEqualTo(100000);
        assertThat(rules.preview(new TaxPreviewRequest(100000L, "INR", "Atlantis", null, null, null, null, null)).blockCode()).isEqualTo("MISSING_CUSTOMER_COUNTRY");
        assertThat(invoicesBefore).isZero();
    }

    @Test
    void taxClassesAreCreatedUpdatedAndAssignedToProducts() {
        var created = classes.create(MAKER, new SaveTaxClassRequest("saas-it", "SaaS", "998439", "Software as a service", null));
        assertThat(created.code()).isEqualTo("SAAS-IT");
        assertThat(created.enabled()).isTrue();
        assertThatThrownBy(() -> classes.create(MAKER, new SaveTaxClassRequest("SAAS-IT", "Again", null, null, true)))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.DuplicateResourceException.class);

        var product = fixtures.product("10.00", null);
        var assigned = classes.assign(MAKER, product.getId(), new com.vyoog.eisplatform.modules.billing.tax.dto.AssignTaxClassRequest("saas-it"));
        assertThat(assigned.taxClassCode()).isEqualTo("SAAS-IT");
        assertThat(classes.list()).filteredOn(c -> c.code().equals("SAAS-IT")).singleElement().satisfies(c -> assertThat(c.productCount()).isEqualTo(1));
        assertThatThrownBy(() -> classes.assign(MAKER, product.getId(), new com.vyoog.eisplatform.modules.billing.tax.dto.AssignTaxClassRequest("NOPE"))).isInstanceOf(IllegalArgumentException.class);

        classes.update(MAKER, created.id(), new SaveTaxClassRequest("ignored", "SaaS renamed", "998439", null, false));
        assertThatThrownBy(() -> classes.assign(MAKER, fixtures.product("5.00", null).getId(), new com.vyoog.eisplatform.modules.billing.tax.dto.AssignTaxClassRequest("SAAS-IT")))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("disabled");
        assertThat(classes.assign(MAKER, product.getId(), new com.vyoog.eisplatform.modules.billing.tax.dto.AssignTaxClassRequest(null)).taxClassCode()).isNull();
    }

    @Test
    void registrationsAreKeptAndTheBusinessProfileGstinIsListedReadOnly() {
        var row = registrations.create(MAKER, new SaveTaxRegistrationRequest("de", null, TaxType.VAT, "DE123456789", TODAY, null, null));
        assertThat(row.countryCode()).isEqualTo("DE");
        assertThat(row.enabled()).isTrue();
        assertThat(registrations.list()).extracting(r -> r.registrationNumber()).contains(TaxTestSupport.VALID_GSTIN_TN, "DE123456789");
        assertThat(registrations.list().get(0).fromBusinessProfile()).isTrue();
        assertThatThrownBy(() -> registrations.create(MAKER, new SaveTaxRegistrationRequest("ZZ", null, TaxType.VAT, "X", null, null, null)))
            .isInstanceOf(IllegalArgumentException.class);
        registrations.delete(MAKER, row.id());
        assertThat(registrations.list()).extracting(r -> r.registrationNumber()).doesNotContain("DE123456789");
    }

    @Test
    void anExemptionIsApprovedByAnotherAdministrator() {
        var customer = fixtures.customer();
        var e = exemptions.create(MAKER, new SaveTaxExemptionRequest("CUSTOMER", customer.getId(), "in", "SEZ unit", "SEZ-1", TODAY, null));
        assertThat(e.status().name()).isEqualTo("PENDING_APPROVAL");
        assertThat(e.inForce()).isFalse();

        assertThatThrownBy(() -> exemptions.approve(MAKER, e.id())).isInstanceOf(BillingConflictException.class);
        var approved = exemptions.approve(CHECKER, e.id());
        assertThat(approved.status().name()).isEqualTo("APPROVED");
        assertThat(approved.inForce()).isTrue();
        assertThat(approved.ownerLabel()).contains(customer.getEmail());
        assertThat(exemptions.revoke(CHECKER, e.id()).status().name()).isEqualTo("REVOKED");
        assertThatThrownBy(() -> exemptions.create(MAKER, new SaveTaxExemptionRequest("PERSON", 1L, "IN", "x", null, TODAY, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
