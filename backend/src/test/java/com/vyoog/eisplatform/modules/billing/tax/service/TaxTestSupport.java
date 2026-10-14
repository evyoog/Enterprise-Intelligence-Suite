package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.dto.SaveBillingDetailsRequest;
import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.repository.BillingSettingsRepository;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRule;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRuleComponent;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxSettings;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRuleRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Test fixtures for tax (REQ-BIL-002). The platform ships no tax rate, so a test that expects an issued invoice must set up
 * what an administrator would: the supplier's business profile, an approved rule, and the customer's billing details. The H2
 * database is shared by the tests of one Spring context, so every method is safe to call more than once. */
@TestComponent
public class TaxTestSupport {

    public static final String VALID_GSTIN_TN = "33ABCDE1234F1Z5";
    public static final String VALID_GSTIN_KA = "29ABCDE1234F1Z5";

    @Autowired private BillingSettingsRepository billingSettingsRepository;
    @Autowired private TaxRuleRepository ruleRepository;
    @Autowired private TaxSettingsRepository taxSettingsRepository;
    @Autowired private BillingDetailsService billingDetailsService;
    @Autowired private com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository organizationRepository;

    /** The seller: an Indian company in Tamil Nadu. */
    public void supplierInTamilNadu() {
        BillingSettings s = billingSettingsRepository.findFirstByOrderByIdAsc().orElseGet(BillingSettings::new);
        s.setBusinessLegalName("eVyoog Test Pvt Ltd");
        s.setBusinessAddressLine1("1 Test Street");
        s.setBusinessCity("Chennai");
        s.setBusinessState("Tamil Nadu");
        s.setBusinessPostalCode("600001");
        s.setBusinessCountry("India");
        s.setBusinessGstin(VALID_GSTIN_TN);
        s.setBusinessPan("ABCDE1234F");
        s.setBusinessEmail("billing@example.com");
        billingSettingsRepository.save(s);
    }

    /** Engine switch back to the internal engine with the defaults. */
    public void internalEngine() {
        TaxSettings s = taxSettingsRepository.findById(TaxSettings.SINGLETON_ID).orElseGet(TaxSettings::new);
        s.setExternalEngineEnabled(false);
        taxSettingsRepository.save(s);
    }

    /** An approved, active rule that charges 0% on everything in a country: for tests about invoices, not about tax. */
    public TaxRule zeroRateRule(String country) {
        return approvedRule("Test zero rate " + country, country, null, SupplyType.ANY, null, TaxTreatment.TAXABLE, LocalDate.of(2020, 1, 1), null,
            comp("GST", "GST", "0"));
    }

    public TaxRule approvedRule(String name, String country, String stateKey, SupplyType supply, String classCode, TaxTreatment treatment,
                                LocalDate from, LocalDate to, TaxRuleComponent... components) {
        TaxRule existing = ruleRepository.findAll().stream().filter(r -> r.getName().equals(name) && r.getApprovalStatus() == RuleApprovalStatus.APPROVED
            && java.util.Objects.equals(r.getEffectiveFrom(), from)).findFirst().orElse(null);
        if (existing != null) {
            return existing;
        }
        TaxRule r = new TaxRule();
        r.setRuleKey(UUID.randomUUID().toString());
        r.setVersion(1);
        r.setName(name);
        r.setCountryCode(country);
        r.setStateCode(stateKey);
        r.setTaxType(TaxType.GST);
        r.setProductClassCode(classCode);
        r.setCustomerType(CustomerTypeCondition.ANY);
        r.setSupplyType(supply);
        r.setTaxIdCondition(TaxIdCondition.ANY);
        r.setTreatment(treatment);
        r.setEffectiveFrom(from);
        r.setEffectiveTo(to);
        r.setApprovalStatus(RuleApprovalStatus.APPROVED);
        r.setActive(true);
        r.setCreatedBy("test-maker");
        r.setApprovedBy("test-checker");
        int order = 0;
        for (TaxRuleComponent c : components) {
            c.setRule(r);
            c.setSortOrder(order++);
            r.getComponents().add(c);
        }
        return ruleRepository.save(r);
    }

    public static TaxRuleComponent comp(String code, String name, String rate) {
        TaxRuleComponent c = new TaxRuleComponent();
        c.setCode(code);
        c.setName(name);
        c.setRate(new BigDecimal(rate));
        return c;
    }

    /** Billing details in Tamil Nadu, India, for a customer, so a tax calculation can decide the place of supply. */
    public void billingDetailsForCustomer(Long customerId, String email) {
        billingDetailsService.saveForCustomer(customerId, new SaveBillingDetailsRequest(
            "Bill Payer", email, "1 Main St", null, "Chennai", "Tamil Nadu", "600001", "India", null));
    }

    /** Everything an issued invoice needs: supplier, internal engine, a 0% rule for India and the customer's billing details. */
    public void readyToInvoiceInIndia(com.vyoog.eisplatform.modules.registration.model.Customer customer) {
        supplierInTamilNadu();
        internalEngine();
        zeroRateRule("IN");
        billingDetailsForCustomer(customer.getId(), customer.getEmail());
    }

    /** C97 BI-4: an organization with billing details in Tamil Nadu, optionally enabled for Pay by invoice. */
    public com.vyoog.eisplatform.modules.registration.model.Organization organizationReadyToInvoice(
            com.vyoog.eisplatform.modules.registration.model.Customer admin, boolean payByInvoice) {
        supplierInTamilNadu();
        internalEngine();
        zeroRateRule("IN");
        var org = new com.vyoog.eisplatform.modules.registration.model.Organization();
        org.setName("Invoice Org " + System.nanoTime());
        org.setCode("INV-" + System.nanoTime());
        org.setBusinessEmail("org-" + System.nanoTime() + "@example.com");
        org.setCountry("India");
        org.setPayByInvoiceEnabled(payByInvoice);
        org = organizationRepository.save(org);
        billingDetailsService.saveForOrganization(admin.getId(), org.getId(), new SaveBillingDetailsRequest(
            "Org Payer", org.getBusinessEmail(), "1 Main St", null, "Chennai", "Tamil Nadu", "600001", "India", null));
        return org;
    }
}
