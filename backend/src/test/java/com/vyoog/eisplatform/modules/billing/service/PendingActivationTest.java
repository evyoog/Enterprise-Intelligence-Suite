package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** C97 BI-1: a card/UPI purchase starts after payment; an approved organization's pay-by-invoice purchase starts at once with a due
 * date; the activation happens once however many times the event arrives. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TaxTestSupport.class)
class PendingActivationTest {

    @Autowired private TaxTestSupport tax;
    @Autowired private InvoiceService invoiceService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @MockBean private RazorpayClient razorpayClient;

    private Customer customer() {
        Customer c = new Customer();
        c.setEmail("pending-" + System.nanoTime() + "@example.com");
        c.setFirstName("Pen");
        c.setLastName("Ding");
        c = customerRepository.save(c);
        tax.readyToInvoiceInIndia(c);
        return c;
    }

    private ProductSubscription pending(RegistrationOwnerType type, Long customerId, Long organizationId) {
        Product product = new Product();
        product.setName("Pending Product " + System.nanoTime());
        product.setPrice(BigDecimal.valueOf(999));
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductSubscription s = new ProductSubscription();
        s.setProductId(product.getId());
        s.setOwnerType(type);
        s.setOwnerCustomerId(customerId);
        s.setOwnerOrganizationId(organizationId);
        s.setStatus(SubscriptionStatus.PENDING_SUBSCRIPTION);
        return subscriptionRepository.save(s);
    }

    private SubscriptionStatus statusOf(ProductSubscription s) {
        return subscriptionRepository.findById(s.getId()).orElseThrow().getStatus();
    }

    @Test
    void anOnlinePurchaseStaysPendingUntilTheInvoiceIsPaidThenStartsOnce() {
        Customer customer = customer();
        ProductSubscription s = pending(RegistrationOwnerType.INDIVIDUAL, customer.getId(), null);
        Invoice invoice = invoiceService.generateForSubscription(s.getId());

        assertThat(statusOf(s)).isEqualTo(SubscriptionStatus.PENDING_SUBSCRIPTION);

        invoiceService.markPaid(invoice);
        ProductSubscription active = subscriptionRepository.findById(s.getId()).orElseThrow();
        assertThat(active.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(active.getStartedAt()).isNotNull();

        var startedAt = active.getStartedAt();
        invoiceService.markPaid(invoice);   // the same webhook delivered twice
        assertThat(subscriptionRepository.findById(s.getId()).orElseThrow().getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    void payByInvoiceStartsAnApprovedOrganizationsPurchaseAtOnceWithADueDate() {
        Customer admin = customer();
        var org = tax.organizationReadyToInvoice(admin, true);
        ProductSubscription s = pending(RegistrationOwnerType.ORGANIZATION, null, org.getId());
        Invoice invoice = invoiceService.generateForSubscription(s.getId());
        assertThat(statusOf(s)).isEqualTo(SubscriptionStatus.PENDING_SUBSCRIPTION);

        var result = checkoutService.chooseOffline(null, org.getId(), admin.getId(), invoice.getId());

        assertThat(statusOf(s)).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.dueAt()).isNotNull();
    }

    @Test
    void anOrganizationNotEnabledForPayByInvoiceStaysPendingUntilItPays() {
        Customer admin = customer();
        var org = tax.organizationReadyToInvoice(admin, false);
        ProductSubscription s = pending(RegistrationOwnerType.ORGANIZATION, null, org.getId());
        Invoice invoice = invoiceService.generateForSubscription(s.getId());

        try {
            checkoutService.chooseOffline(null, org.getId(), admin.getId(), invoice.getId());
        } catch (com.vyoog.eisplatform.common.exception.ForbiddenException expected) {
            // refused
        }
        assertThat(statusOf(s)).isEqualTo(SubscriptionStatus.PENDING_SUBSCRIPTION);
    }
}
