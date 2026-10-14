package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.dto.*;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.OfflinePaymentMethod;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentRepository;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C55 (REQ-BIL-001.18-.21): checkout summary, Pay by invoice, admin
 * Record offline payment, offline bank details. Nothing here needs
 * Razorpay; {@link RazorpayClient} is mocked so the context never reaches
 * the network. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TaxTestSupport.class)
class CheckoutOfflinePaymentTest {

    @Autowired private TaxTestSupport tax;

    @Autowired private CheckoutService checkoutService;
    @Autowired private BillingSettingsService billingSettingsService;
    @Autowired private OfflinePaymentService offlinePaymentService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;

    @MockBean private RazorpayClient razorpayClient;

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("checkout-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Check");
        customer.setLastName("Out");
        customer = customerRepository.save(customer);
        tax.readyToInvoiceInIndia(customer);   // an issued invoice needs a supplier, a rule and billing details
        return customer;
    }

    private ProductSubscription newSubscription(Customer customer, BigDecimal price) {
        Product product = new Product();
        product.setName("Checkout Product " + System.nanoTime());
        product.setPrice(price);
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        subscription.setOwnerCustomerId(customer.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        return subscriptionRepository.save(subscription);
    }

    private Invoice newOpenInvoice(Customer customer) {
        return invoiceService.generateForSubscription(newSubscription(customer, BigDecimal.valueOf(1220.80)).getId());
    }

    /** C97 BI-4: Pay by invoice is only for an organization the platform admin enabled. */
    private Invoice newOrgInvoice(Organization org) {
        Product product = new Product();
        product.setName("Org Product " + System.nanoTime());
        product.setPrice(BigDecimal.valueOf(1220.80));
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        subscription.setOwnerOrganizationId(org.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        return invoiceService.generateForSubscription(subscriptionRepository.save(subscription).getId());
    }

    @Test
    void checkoutSummaryBySubscriptionShowsTheOpenInvoiceAndItsItem() {
        Customer customer = newCustomer();
        ProductSubscription subscription = newSubscription(customer, BigDecimal.valueOf(19.99));
        Invoice invoice = invoiceService.generateForSubscription(subscription.getId());

        CheckoutSummaryDto summary = checkoutService.summaryForSubscription(customer.getId(), null, subscription.getId());

        assertThat(summary.invoiceId()).isEqualTo(invoice.getId());
        assertThat(summary.total()).isEqualTo(1999L);
        assertThat(summary.items()).hasSize(1);
        assertThat(summary.items().get(0).quantity()).isNull();
        // C96: the zero-rate rule set up for this test is shown as an explicit 0% line, never as a missing tax.
        assertThat(summary.taxLines()).singleElement().satisfies(t -> assertThat(t.amount()).isZero());
        assertThat(summary.billingEmail()).isEqualTo(customer.getEmail());
        assertThat(summary.payByInvoiceAllowed()).isFalse();   // BI-4: an individual customer always pays online
    }

    @Test
    void aFreePlanHasNoInvoiceToPay() {
        Customer customer = newCustomer();
        ProductSubscription subscription = newSubscription(customer, BigDecimal.ZERO);
        invoiceService.generateForSubscription(subscription.getId());

        CheckoutSummaryDto summary = checkoutService.summaryForSubscription(customer.getId(), null, subscription.getId());

        assertThat(summary.invoiceId()).isNull();
        assertThat(summary.total()).isZero();
    }

    @Test
    void anotherCustomersCheckoutIsNotFound() {
        Customer owner = newCustomer();
        Customer other = newCustomer();
        Invoice invoice = newOpenInvoice(owner);

        assertThatThrownBy(() -> checkoutService.summaryForInvoice(other.getId(), null, invoice.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void payByInvoiceSetsTheOfflineRouteAndKeepsTheInvoiceOpen() {
        Customer customer = newCustomer();
        Organization org = tax.organizationReadyToInvoice(customer, true);
        Invoice invoice = newOrgInvoice(org);

        OfflineInvoiceResultDto result = checkoutService.chooseOffline(null, org.getId(), customer.getId(), invoice.getId());

        Invoice reloaded = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(reloaded.getPaymentRoute()).isEqualTo(PaymentRoute.OFFLINE);
        assertThat(reloaded.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(result.invoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
        assertThat(result.total()).isEqualTo(122080L);
        assertThat(result.billingEmail()).isNotBlank();
    }

    @Test
    void recordingTheFullAmountMarksTheInvoicePaidWithAnOfflinePayment() {
        Customer customer = newCustomer();
        Customer admin = newCustomer();
        Organization org = tax.organizationReadyToInvoice(customer, true);
        Invoice invoice = newOrgInvoice(org);
        checkoutService.chooseOffline(null, org.getId(), customer.getId(), invoice.getId());

        InvoiceDto paid = offlinePaymentService.recordOfflinePayment(admin.getId(), invoice.getId(),
            new RecordOfflinePaymentRequest(122080L, LocalDate.now(), OfflinePaymentMethod.NEFT_RTGS, "UTR123", "Received"));

        assertThat(paid.status()).isEqualTo(InvoiceStatus.PAID);
        var payment = paymentRepository.findByInvoiceId(invoice.getId()).get(0);
        assertThat(payment.getMethodType()).isEqualTo("OFFLINE");
        assertThat(payment.getProvider()).isEqualTo("OFFLINE");
        assertThat(payment.getOfflineReference()).isEqualTo("UTR123");
        assertThat(payment.getRecordedByCustomerId()).isEqualTo(admin.getId());
    }

    @Test
    void aWrongAmountAFutureDateOrAnOnlineRouteInvoiceIsRefused() {
        Customer customer = newCustomer();
        Organization org = tax.organizationReadyToInvoice(customer, true);
        Invoice invoice = newOrgInvoice(org);

        assertThatThrownBy(() -> offlinePaymentService.recordOfflinePayment(1L, invoice.getId(),
            new RecordOfflinePaymentRequest(122080L, LocalDate.now(), OfflinePaymentMethod.CHEQUE, "CHQ1", null)))
            .isInstanceOf(BillingConflictException.class);

        checkoutService.chooseOffline(null, org.getId(), customer.getId(), invoice.getId());
        assertThatThrownBy(() -> offlinePaymentService.recordOfflinePayment(1L, invoice.getId(),
            new RecordOfflinePaymentRequest(100000L, LocalDate.now(), OfflinePaymentMethod.CHEQUE, "CHQ1", null)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> offlinePaymentService.recordOfflinePayment(1L, invoice.getId(),
            new RecordOfflinePaymentRequest(122080L, LocalDate.now().plusDays(1), OfflinePaymentMethod.CHEQUE, "CHQ1", null)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(invoiceRepository.findById(invoice.getId()).orElseThrow().getStatus()).isEqualTo(InvoiceStatus.OPEN);
    }

    @Test
    void bankDetailsAreSavedAndShownOnTheOfflineResult() {
        Customer admin = newCustomer();
        offlinePaymentService.saveBankDetails(admin.getId(),
            new SaveOfflineBankDetailsRequest("eVyoog Pvt Ltd", "HDFC Bank", null, "50200012345678", null, "hdfc0001234", "",
                null, null, null, null, null, null, null, null, null));

        Customer customer = newCustomer();
        Organization org = tax.organizationReadyToInvoice(customer, true);
        Invoice invoice = newOrgInvoice(org);
        OfflineInvoiceResultDto result = checkoutService.chooseOffline(null, org.getId(), customer.getId(), invoice.getId());

        assertThat(result.bankDetails().bankName()).isEqualTo("HDFC Bank");
        assertThat(result.bankDetails().ifsc()).isEqualTo("HDFC0001234");
        assertThat(result.bankDetails().swiftBic()).isNull();
    }

    @Test
    void payByInvoiceIsRefusedForAnIndividualAndForAnOrganizationThePlatformAdminHasNotEnabled() {
        Customer customer = newCustomer();
        Invoice individualInvoice = newOpenInvoice(customer);
        assertThatThrownBy(() -> checkoutService.chooseOffline(customer.getId(), null, customer.getId(), individualInvoice.getId()))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ForbiddenException.class);

        Organization org = tax.organizationReadyToInvoice(customer, false);
        Invoice orgInvoice = newOrgInvoice(org);
        assertThat(checkoutService.summaryForInvoice(null, org.getId(), orgInvoice.getId()).payByInvoiceAllowed()).isFalse();
        assertThatThrownBy(() -> checkoutService.chooseOffline(null, org.getId(), customer.getId(), orgInvoice.getId()))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ForbiddenException.class);

        billingSettingsService.setOrganizationPayByInvoice(customer.getId(), org.getId(), true);
        assertThat(checkoutService.summaryForInvoice(null, org.getId(), orgInvoice.getId()).payByInvoiceAllowed()).isTrue();
        assertThat(billingSettingsService.payByInvoiceOrganizations()).extracting(o -> o.id()).contains(org.getId());
        assertThat(billingSettingsService.searchOrganizations(org.getName().substring(0, 10))).extracting(o -> o.id()).contains(org.getId());

        billingSettingsService.setOrganizationPayByInvoice(customer.getId(), org.getId(), false);
        assertThat(checkoutService.summaryForInvoice(null, org.getId(), orgInvoice.getId()).payByInvoiceAllowed()).isFalse();
    }
}
