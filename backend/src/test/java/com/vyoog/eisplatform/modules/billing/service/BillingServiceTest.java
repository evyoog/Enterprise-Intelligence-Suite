package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.InvalidPaymentSignatureException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.dto.*;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentMethodType;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentWebhookEventRepository;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayPaymentInfo;
import com.vyoog.eisplatform.modules.product.model.Currency;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** 08 Billing & Payments (sprint 2026.4.3, REQ-BIL-001, C46). Never calls
 * the real Razorpay API — {@link RazorpayClient} is mocked; every other
 * layer (entities, repositories, services, currency-minor-unit math,
 * idempotency) is real. */
@SpringBootTest
@ActiveProfiles("test")
class BillingServiceTest {

    @Autowired private BillingDetailsService billingDetailsService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private PaymentService paymentService;
    @Autowired private PaymentMethodService paymentMethodService;
    @Autowired private GatewayStatusService gatewayStatusService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentWebhookEventRepository webhookEventRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;

    @MockBean private RazorpayClient razorpayClient;

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("billing-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Bill");
        customer.setLastName("Payer");
        return customerRepository.save(customer);
    }

    private Product newPaidProduct() {
        Product product = new Product();
        product.setName("Paid Product " + System.nanoTime());
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private ProductSubscription newSubscription(Customer customer, Product product) {
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        subscription.setOwnerCustomerId(customer.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        return subscriptionRepository.save(subscription);
    }

    @Test
    void generatingAnInvoiceForAZeroAmountPlanDoesNothing() {
        Customer customer = newCustomer();
        Product product = new Product();
        product.setName("Free Product " + System.nanoTime());
        product.setPrice(BigDecimal.ZERO);
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductSubscription subscription = newSubscription(customer, product);

        assertThat(invoiceService.generateForSubscription(subscription.getId())).isNull();
    }

    @Test
    void generatingAnInvoiceForAPaidPlanCreatesAnOpenInvoiceInMinorUnits() {
        Customer customer = newCustomer();
        Product product = newPaidProduct();
        ProductSubscription subscription = newSubscription(customer, product);

        Invoice invoice = invoiceService.generateForSubscription(subscription.getId());

        assertThat(invoice).isNotNull();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(invoice.getTotal()).isEqualTo(1999L);
        assertThat(invoice.getCurrency()).isEqualTo(Currency.USD);
        assertThat(invoice.getInvoiceNumber()).startsWith("INV-");
        assertThat(invoiceService.resolveOwn(customer.getId(), null, invoice.getId()).getId()).isEqualTo(invoice.getId());
    }

    @Test
    void aCustomerCannotSeeAnotherCustomersInvoice() {
        Customer owner = newCustomer();
        Customer other = newCustomer();
        Invoice invoice = invoiceService.generateForSubscription(newSubscription(owner, newPaidProduct()).getId());

        assertThatThrownBy(() -> invoiceService.resolveOwn(other.getId(), null, invoice.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void payingAnInvoiceWithAVerifiedCapturedPaymentMarksItPaid() {
        Customer customer = newCustomer();
        Invoice invoice = invoiceService.generateForSubscription(newSubscription(customer, newPaidProduct()).getId());

        when(razorpayClient.createOrder(anyLong(), anyString(), anyString())).thenReturn("order_abc");
        CreatePaymentResponse created = paymentService.createPaymentForInvoice(customer.getId(), null, invoice.getId());
        assertThat(created.providerOrderId()).isEqualTo("order_abc");

        when(razorpayClient.verifyPaymentSignature("order_abc", "pay_abc", "sig_abc")).thenReturn(true);
        when(razorpayClient.fetchPayment("pay_abc")).thenReturn(
            new RazorpayPaymentInfo("pay_abc", "captured", "card", "Visa", "1234", null));

        PaymentDto payment = paymentService.confirmPayment(customer.getId(), null, created.paymentId(),
            new ConfirmPaymentRequest("order_abc", "pay_abc", "sig_abc"));

        assertThat(payment.status().name()).isEqualTo("CAPTURED");
        assertThat(invoiceRepository.findById(invoice.getId()).orElseThrow().getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void anInvalidSignatureChangesNothing() {
        Customer customer = newCustomer();
        Invoice invoice = invoiceService.generateForSubscription(newSubscription(customer, newPaidProduct()).getId());
        when(razorpayClient.createOrder(anyLong(), anyString(), anyString())).thenReturn("order_bad");
        CreatePaymentResponse created = paymentService.createPaymentForInvoice(customer.getId(), null, invoice.getId());
        when(razorpayClient.verifyPaymentSignature(anyString(), anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> paymentService.confirmPayment(customer.getId(), null, created.paymentId(),
            new ConfirmPaymentRequest("order_bad", "pay_bad", "wrong_sig")))
            .isInstanceOf(InvalidPaymentSignatureException.class);
        assertThat(invoiceRepository.findById(invoice.getId()).orElseThrow().getStatus()).isEqualTo(InvoiceStatus.OPEN);
    }

    @Test
    void refundingMoreThanCapturedIsRefused() {
        Customer customer = newCustomer();
        Invoice invoice = invoiceService.generateForSubscription(newSubscription(customer, newPaidProduct()).getId());
        when(razorpayClient.createOrder(anyLong(), anyString(), anyString())).thenReturn("order_r");
        CreatePaymentResponse created = paymentService.createPaymentForInvoice(customer.getId(), null, invoice.getId());
        when(razorpayClient.verifyPaymentSignature(anyString(), anyString(), anyString())).thenReturn(true);
        when(razorpayClient.fetchPayment("pay_r")).thenReturn(new RazorpayPaymentInfo("pay_r", "captured", "card", "Visa", "1234", null));
        paymentService.confirmPayment(customer.getId(), null, created.paymentId(), new ConfirmPaymentRequest("order_r", "pay_r", "sig"));

        assertThatThrownBy(() -> paymentService.refundPayment(customer.getId(), created.paymentId(), new RefundRequest(999999, "too much")))
            .isInstanceOf(BillingConflictException.class);

        when(razorpayClient.createRefund(anyString(), anyLong(), anyString())).thenReturn("rfnd_1");
        PaymentDto refunded = paymentService.refundPayment(customer.getId(), created.paymentId(), new RefundRequest(500, "partial refund"));
        assertThat(refunded.status().name()).isEqualTo("PARTIALLY_REFUNDED");
        assertThat(invoiceRepository.findById(invoice.getId()).orElseThrow().getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_REFUNDED);
    }

    @Test
    void aWebhookEventIsProcessedAtMostOnce() {
        when(razorpayClient.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        String body = "{\"id\":\"evt_dup_1\",\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_x\",\"order_id\":\"order_none\"}}}}";

        paymentService.processWebhook(body, "sig");
        paymentService.processWebhook(body, "sig");

        assertThat(webhookEventRepository.findByProviderEventId("evt_dup_1")).isPresent();
        assertThat(webhookEventRepository.findAll().stream().filter(e -> "evt_dup_1".equals(e.getProviderEventId())).count()).isEqualTo(1);
    }

    @Test
    void aWebhookWithAnInvalidSignatureIsRejected() {
        when(razorpayClient.verifyWebhookSignature(anyString(), anyString())).thenReturn(false);
        assertThatThrownBy(() -> paymentService.processWebhook("{}", "bad-sig")).isInstanceOf(InvalidPaymentSignatureException.class);
    }

    @Test
    void savingACardWithoutConsentIsRefused() {
        assertThatThrownBy(() -> paymentMethodService.startSetup(new SetupPaymentMethodRequest(PaymentMethodType.CARD, false, false)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addingAPaymentMethodAndSettingItDefaultLeavesOnlyOneDefault() {
        Customer customer = newCustomer();
        when(razorpayClient.createOrder(anyLong(), anyString(), anyString())).thenReturn("order_pm1", "order_pm2");
        paymentMethodService.startSetup(new SetupPaymentMethodRequest(PaymentMethodType.CARD, true, false));

        when(razorpayClient.verifyPaymentSignature(anyString(), anyString(), anyString())).thenReturn(true);
        when(razorpayClient.fetchPayment("pay_pm1")).thenReturn(new RazorpayPaymentInfo("pay_pm1", "captured", "card", "Visa", "1111", null));
        when(razorpayClient.fetchPayment("pay_pm2")).thenReturn(new RazorpayPaymentInfo("pay_pm2", "captured", "card", "Visa", "2222", null));

        PaymentMethodDto first = paymentMethodService.confirmSetup(customer.getId(), null,
            new ConfirmPaymentMethodSetupRequest("order_pm1", "pay_pm1", "sig", PaymentMethodType.CARD, true, true));
        PaymentMethodDto second = paymentMethodService.confirmSetup(customer.getId(), null,
            new ConfirmPaymentMethodSetupRequest("order_pm2", "pay_pm2", "sig", PaymentMethodType.CARD, true, true));

        var methods = paymentMethodService.listForCustomer(customer.getId());
        assertThat(methods).hasSize(2);
        assertThat(methods.stream().filter(PaymentMethodDto::isDefault).count()).isEqualTo(1);
        assertThat(methods.stream().filter(PaymentMethodDto::isDefault).findFirst().orElseThrow().id()).isEqualTo(second.id());

        paymentMethodService.remove(customer.getId(), null, first.id());
        assertThat(paymentMethodService.listForCustomer(customer.getId())).hasSize(1);
    }

    @Test
    void billingDetailsCanBeSavedAndReadBack() {
        Customer customer = newCustomer();
        assertThat(billingDetailsService.getForCustomer(customer.getId())).isNull();

        BillingDetailsDto saved = billingDetailsService.saveForCustomer(customer.getId(), new SaveBillingDetailsRequest(
            "Bill Payer", "billing@example.com", "1 Main St", null, "Chennai", "TN", "600001", "India", "GSTIN123"));

        assertThat(saved.billingName()).isEqualTo("Bill Payer");
        assertThat(billingDetailsService.getForCustomer(customer.getId()).billingEmail()).isEqualTo("billing@example.com");
    }

    @Test
    void gatewayStatusMasksTheKeyIdAndNeverReturnsSecrets() {
        GatewayStatusDto status = gatewayStatusService.getStatus();
        assertThat(status.configured()).isTrue();
        assertThat(status.maskedKeyId()).contains("••••••").doesNotContain("1234567890");
        assertThat(status.keySecretSet()).isTrue();
        assertThat(status.webhookSecretSet()).isTrue();
    }
}
