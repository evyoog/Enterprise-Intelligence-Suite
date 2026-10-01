package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.billing.dto.CheckoutSummaryDto;
import com.vyoog.eisplatform.modules.billing.dto.RecordOfflinePaymentRequest;
import com.vyoog.eisplatform.modules.billing.dto.SaveBusinessProfileRequest;
import com.vyoog.eisplatform.modules.billing.dto.SaveOfflineBankDetailsRequest;
import com.vyoog.eisplatform.modules.billing.dto.SavePaymentMethodSettingsRequest;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.OfflinePaymentMethod;
import com.vyoog.eisplatform.modules.billing.repository.BillingSettingsRepository;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C60: full Billing settings — business profile and invoicing, more offline
 * payment details, checkout payment methods. The settings row is shared by
 * the whole test context, so every test here deletes it afterwards. */
@SpringBootTest
@ActiveProfiles("test")
class BillingSettingsServiceTest {

    @Autowired private BillingSettingsService billingSettingsService;
    @Autowired private OfflinePaymentService offlinePaymentService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private BillingSettingsRepository settingsRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;

    @MockBean private RazorpayClient razorpayClient;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AfterEach
    void resetSettings() {
        settingsRepository.deleteAll();
    }

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("settings-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Set");
        customer.setLastName("Tings");
        return customerRepository.save(customer);
    }

    private Invoice newInvoice(Customer customer) {
        Product product = new Product();
        product.setName("Settings Product " + System.nanoTime());
        product.setPrice(BigDecimal.valueOf(500));
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        subscription.setOwnerCustomerId(customer.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        return invoiceService.generateForSubscription(subscriptionRepository.save(subscription).getId());
    }

    private SaveBusinessProfileRequest profile(String prefix, int terms) {
        return new SaveBusinessProfileRequest("eVyoog Technologies Pvt Ltd", "eVyoog", "29abcde1234f1z5", "abcde1234f",
            "U72900KA2020PTC123456", "12 MG Road", null, "Bengaluru", "Karnataka", "560001", "India",
            "billing@evyoog.example", "+91 80 1234 5678", "https://www.evyoog.example", prefix, terms, "Thank you for your business.");
    }

    @Test
    void defaultsKeepTheBehaviourBeforeC60() {
        assertThat(billingSettingsService.businessProfile().invoicePrefix()).isEqualTo("INV");
        assertThat(billingSettingsService.businessProfile().paymentTermsDays()).isZero();
        assertThat(billingSettingsService.enabledOnlineMethods()).containsExactly("card", "upi", "netbanking", "wallet");
        assertThat(billingSettingsService.payByInvoiceEnabled()).isTrue();
        assertThat(offlinePaymentService.bankDetails().chequeEnabled()).isTrue();
    }

    @Test
    void theBusinessProfileIsSavedUpperCasedAndSetsThePrefixAndDueDateOfNewInvoices() {
        Customer admin = newCustomer();
        var saved = billingSettingsService.saveBusinessProfile(admin.getId(), profile("evy", 15));

        assertThat(saved.gstin()).isEqualTo("29ABCDE1234F1Z5");
        assertThat(saved.pan()).isEqualTo("ABCDE1234F");
        assertThat(saved.invoicePrefix()).isEqualTo("EVY");

        Invoice invoice = newInvoice(newCustomer());
        assertThat(invoice.getInvoiceNumber()).startsWith("EVY-");
        assertThat(Duration.between(invoice.getIssuedAt(), invoice.getDueAt()).toDays()).isEqualTo(15);
    }

    @Test
    void invalidRegistrationNumbersAndPrefixesAreRefused() {
        var bad = new SaveBusinessProfileRequest("X", null, "12345", "ABC", "NOPE", "a", null, "c", "s", "1", "In",
            "not-an-email", "abc", "ftp://x", "a", 400, null);
        assertThat(validator.validate(bad)).extracting(v -> v.getPropertyPath().toString())
            .contains("gstin", "pan", "cin", "email", "phone", "website", "invoicePrefix", "paymentTermsDays");
        assertThat(validator.validate(profile("INV", 30))).isEmpty();
    }

    @Test
    void switchingMethodsOffChangesTheCheckoutAndRefusesPayByInvoice() {
        Customer admin = newCustomer();
        billingSettingsService.savePaymentMethods(admin.getId(),
            new SavePaymentMethodSettingsRequest(true, false, true, false, false, "eVyoog Store", "Subscriptions", "#4c63ff"));
        Customer customer = newCustomer();
        Invoice invoice = newInvoice(customer);

        CheckoutSummaryDto summary = checkoutService.summaryForInvoice(customer.getId(), null, invoice.getId());

        assertThat(summary.enabledMethods()).containsExactly("card", "netbanking");
        assertThat(summary.payByInvoiceAllowed()).isFalse();
        assertThat(summary.checkoutName()).isEqualTo("eVyoog Store");
        assertThat(summary.checkoutThemeColor()).isEqualTo("#4C63FF");
        assertThatThrownBy(() -> checkoutService.chooseOffline(customer.getId(), null, customer.getId(), invoice.getId()))
            .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> billingSettingsService.requireOnlineMethodEnabled("upi"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> billingSettingsService.savePaymentMethods(admin.getId(),
            new SavePaymentMethodSettingsRequest(false, false, false, false, false, null, null, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fullOfflineDetailsAreSavedAndAnOfflineMethodThatIsOffIsRefused() {
        Customer admin = newCustomer();
        var saved = offlinePaymentService.saveBankDetails(admin.getId(), new SaveOfflineBankDetailsRequest(
            "eVyoog Technologies Pvt Ltd", "HDFC Bank", "MG Road", "50200012345678", "CURRENT", "hdfc0001234", "hdfcinbb",
            null, "560240002", "evyoog@hdfcbank", "eVyoog Technologies Pvt Ltd", "12 MG Road, Bengaluru 560001",
            "Email the remittance advice to billing@evyoog.example.", true, true, false));

        assertThat(saved.branchName()).isEqualTo("MG Road");
        assertThat(saved.swiftBic()).isEqualTo("HDFCINBB");
        assertThat(saved.upiId()).isEqualTo("evyoog@hdfcbank");
        assertThat(saved.chequeEnabled()).isFalse();

        Customer customer = newCustomer();
        Invoice invoice = newInvoice(customer);
        checkoutService.chooseOffline(customer.getId(), null, customer.getId(), invoice.getId());
        assertThatThrownBy(() -> offlinePaymentService.recordOfflinePayment(admin.getId(), invoice.getId(),
            new RecordOfflinePaymentRequest(invoice.getTotal(), LocalDate.now(), OfflinePaymentMethod.CHEQUE, "CHQ1", null)))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> offlinePaymentService.saveBankDetails(admin.getId(), new SaveOfflineBankDetailsRequest(
            "A", "B", null, "1", null, null, null, null, null, null, null, null, null, false, false, false)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
