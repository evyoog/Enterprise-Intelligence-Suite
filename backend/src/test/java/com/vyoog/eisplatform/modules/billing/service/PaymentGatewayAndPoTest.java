package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C97 BI-8 (gateway-neutral payment screens) and BI-9 (optional purchase-order number). */
@SpringBootTest
@ActiveProfiles("test")
@Import(TaxTestSupport.class)
class PaymentGatewayAndPoTest {

    @Autowired private TaxTestSupport tax;
    @Autowired private PaymentGatewayService gateways;
    @Autowired private InvoiceService invoiceService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @MockBean private RazorpayClient razorpayClient;

    @Test
    void razorpayIsTheFirstGatewayAndTheListNeverCarriesASecret() {
        var list = gateways.list();

        assertThat(list).extracting(g -> g.code()).contains("RAZORPAY");
        var razorpay = list.stream().filter(g -> g.code().equals("RAZORPAY")).findFirst().orElseThrow();
        assertThat(razorpay.active()).isTrue();
        assertThat(razorpay.methods()).contains("card", "upi", "netbanking", "wallet");
        assertThat(razorpay.status().toString()).doesNotContain("secret=");
        assertThat(gateways.activeName()).isEqualTo("Razorpay");
        assertThatThrownBy(() -> gateways.test("NOPE")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aConfiguredGatewayCanBeMadeActiveAndAnUnknownOneIsRefused() {
        var list = gateways.setActive(1L, "razorpay");
        assertThat(list).filteredOn(g -> g.active()).extracting(g -> g.code()).containsExactly("RAZORPAY");
        assertThatThrownBy(() -> gateways.setActive(1L, "STRIPE")).isInstanceOf(ResourceNotFoundException.class);
    }

    private Invoice invoiceFor(Customer c) {
        Product p = new Product();
        p.setName("PO Product " + System.nanoTime());
        p.setPrice(BigDecimal.valueOf(700));
        p.setStatus(ProductStatus.ACTIVE);
        p = productRepository.save(p);
        ProductSubscription s = new ProductSubscription();
        s.setProductId(p.getId());
        s.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        s.setOwnerCustomerId(c.getId());
        s.setStatus(SubscriptionStatus.ACTIVE);
        return invoiceService.generateForSubscription(subscriptionRepository.save(s).getId());
    }

    @Test
    void aPurchaseOrderNumberCanBeAddedClearedAndOnlyBeforePayment() {
        Customer c = new Customer();
        c.setEmail("po-" + System.nanoTime() + "@example.com");
        c.setFirstName("Po");
        c.setLastName("Number");
        c = customerRepository.save(c);
        tax.readyToInvoiceInIndia(c);
        Invoice invoice = invoiceFor(c);
        Long customerId = c.getId();

        InvoiceDto saved = invoiceService.setPurchaseOrder(customerId, null, invoice.getId(), " PO-2026/17 ");
        assertThat(saved.purchaseOrderNumber()).isEqualTo("PO-2026/17");
        assertThat(checkoutService.summaryForInvoice(customerId, null, invoice.getId()).purchaseOrderNumber()).isEqualTo("PO-2026/17");
        assertThat(invoiceService.setPurchaseOrder(customerId, null, invoice.getId(), "").purchaseOrderNumber()).isNull();

        invoiceService.setPurchaseOrder(customerId, null, invoice.getId(), "PO-9");
        invoiceService.markPaid(invoice);
        assertThatThrownBy(() -> invoiceService.setPurchaseOrder(customerId, null, invoice.getId(), "PO-10"))
            .isInstanceOf(BillingConflictException.class);
        Customer other = new Customer();
        other.setEmail("other-" + System.nanoTime() + "@example.com");
        other.setFirstName("O");
        other.setLastName("T");
        Customer savedOther = customerRepository.save(other);
        assertThatThrownBy(() -> invoiceService.setPurchaseOrder(savedOther.getId(), null, invoice.getId(), "PO-11"))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
