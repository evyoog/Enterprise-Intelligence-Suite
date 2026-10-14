package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.tax.model.TaxSettings;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxCalculationRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxExemptionRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRuleRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxSettingsRepository;
import com.vyoog.eisplatform.modules.billing.dto.SaveBillingDetailsRequest;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxClass;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxClassRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import org.springframework.boot.test.context.TestComponent;

/** Resets the shared tax state between integration tests: the engine switch, provider settings, the rules, the exemptions. */
@TestComponent
public class TaxIntegrationSupport {

    @Autowired private TaxSettingsRepository settingsRepository;
    @Autowired private TaxRuleRepository ruleRepository;
    @Autowired private TaxExemptionRepository exemptionRepository;
    @Autowired private TaxCalculationRepository calculationRepository;
    @Autowired private StubTaxProvider stub;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private BillingDetailsService billingDetailsService;
    @Autowired private TaxClassRepository classRepository;
    @Autowired private TaxTestSupport support;
    @Autowired private com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository invoiceRepository;

    public void resetAll() {
        stub.reset();
        // Draft invoices left by other tests would change the status shown (invoices waiting).
        invoiceRepository.deleteAll(invoiceRepository.findByStatusOrderByIdAsc(com.vyoog.eisplatform.modules.billing.model.InvoiceStatus.DRAFT));
        exemptionRepository.deleteAll();
        ruleRepository.deleteAll();
        settingsRepository.deleteAll();
        settingsRepository.save(new TaxSettings());
    }

    public Customer customer() {
        Customer c = new Customer();
        c.setEmail("tax-" + System.nanoTime() + "@example.com");
        c.setFirstName("Tax");
        c.setLastName("Payer");
        return customerRepository.save(c);
    }

    /** A customer with billing details in the given Indian state (and optional GSTIN), country India. */
    public Customer customerIn(String state, String gstin) {
        Customer c = customer();
        billingDetailsService.saveForCustomer(c.getId(), new SaveBillingDetailsRequest("Tax Payer", c.getEmail(), "1 Main St", null, "City", state,
            "600001", "India", gstin));
        return c;
    }

    public TaxClass taxClass(String code, String sac) {
        return classRepository.findByCode(code).orElseGet(() -> {
            TaxClass c = new TaxClass();
            c.setCode(code);
            c.setName(code);
            c.setHsnSacCode(sac);
            return classRepository.save(c);
        });
    }

    public Product product(String price, String taxClassCode) {
        Product p = new Product();
        p.setName("Taxed Product " + System.nanoTime());
        p.setPrice(new BigDecimal(price));
        p.setStatus(ProductStatus.ACTIVE);
        p.setTaxClassCode(taxClassCode);
        return productRepository.save(p);
    }

    public ProductSubscription subscription(Customer customer, Product product) {
        ProductSubscription s = new ProductSubscription();
        s.setProductId(product.getId());
        s.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        s.setOwnerCustomerId(customer.getId());
        s.setStatus(SubscriptionStatus.ACTIVE);
        return subscriptionRepository.save(s);
    }

    /** Seller in Tamil Nadu, India. */
    public void supplier() {
        support.supplierInTamilNadu();
    }
}
