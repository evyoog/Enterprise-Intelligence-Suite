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
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class CheckoutOfflinePaymentTest {

    @Autowired private CheckoutService checkoutService;
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
        return customerRepository.save(customer);
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
        assertThat(summary.taxLines()).isEmpty();
        assertThat(summary.billingEmail()).isEqualTo(customer.getEmail());
        assertThat(summary.payByInvoiceAllowed()).isTrue();
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
        Invoice invoice = newOpenInvoice(customer);

        OfflineInvoiceResultDto result = checkoutService.chooseOffline(customer.getId(), null, customer.getId(), invoice.getId());

        Invoice reloaded = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(reloaded.getPaymentRoute()).isEqualTo(PaymentRoute.OFFLINE);
        assertThat(reloaded.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(result.invoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
        assertThat(result.total()).isEqualTo(122080L);
        assertThat(result.billingEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void recordingTheFullAmountMarksTheInvoicePaidWithAnOfflinePayment() {
        Customer customer = newCustomer();
        Customer admin = newCustomer();
        Invoice invoice = newOpenInvoice(customer);
        checkoutService.chooseOffline(customer.getId(), null, customer.getId(), invoice.getId());

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
        Invoice invoice = newOpenInvoice(customer);

        assertThatThrownBy(() -> offlinePaymentService.recordOfflinePayment(1L, invoice.getId(),
            new RecordOfflinePaymentRequest(122080L, LocalDate.now(), OfflinePaymentMethod.CHEQUE, "CHQ1", null)))
            .isInstanceOf(BillingConflictException.class);

        checkoutService.chooseOffline(customer.getId(), null, customer.getId(), invoice.getId());
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
            new SaveOfflineBankDetailsRequest("eVyoog Pvt Ltd", "HDFC Bank", "50200012345678", "hdfc0001234", ""));

        Customer customer = newCustomer();
        Invoice invoice = newOpenInvoice(customer);
        OfflineInvoiceResultDto result = checkoutService.chooseOffline(customer.getId(), null, customer.getId(), invoice.getId());

        assertThat(result.bankDetails().bankName()).isEqualTo("HDFC Bank");
        assertThat(result.bankDetails().ifsc()).isEqualTo("HDFC0001234");
        assertThat(result.bankDetails().swiftBic()).isNull();
    }
}
