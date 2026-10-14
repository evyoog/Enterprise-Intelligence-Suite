package com.vyoog.eisplatform.modules.billing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.InvalidPaymentSignatureException;
import com.vyoog.eisplatform.common.exception.PaymentGatewayNotConfiguredException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.dto.ConfirmPaymentRequest;
import com.vyoog.eisplatform.modules.billing.dto.CreatePaymentResponse;
import com.vyoog.eisplatform.modules.billing.dto.GatewayDtos;
import com.vyoog.eisplatform.modules.billing.dto.RefundRequest;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.Payment;
import com.vyoog.eisplatform.modules.billing.model.PaymentGatewayConfig;
import com.vyoog.eisplatform.modules.billing.model.PaymentStatus;
import com.vyoog.eisplatform.modules.billing.repository.PaymentGatewayConfigRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentWebhookEventRepository;
import com.vyoog.eisplatform.modules.billing.service.gateway.ConnectionStatus;
import com.vyoog.eisplatform.modules.billing.service.gateway.FakeGateway;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * C98 / REQ-BIL-003: the provider-independent gateway module. Razorpay is the only real adapter; {@link FakeGateway} is a test-only second
 * provider so that configuration, activation, rotation, routing, per-provider payments and webhooks are tested as generic behaviour.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TaxTestSupport.class, GatewayManagementServiceTest.Config.class})
class GatewayManagementServiceTest {

    @TestConfiguration
    static class Config {
        @Bean
        FakeGateway fakeGateway() {
            return new FakeGateway();
        }
    }

    private static final com.vyoog.eisplatform.modules.billing.service.gateway.GatewayActor ADMIN =
        new com.vyoog.eisplatform.modules.billing.service.gateway.GatewayActor("admin-sub", 1L);

    @Autowired private GatewayManagementService gateways;
    @Autowired private GatewayRoutingService routing;
    @Autowired private PaymentService paymentService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private PaymentGatewayConfigRepository configRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentWebhookEventRepository webhookEventRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private TaxTestSupport tax;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private RazorpayClient razorpayClient;

    @BeforeEach
    @AfterEach
    void cleanState() {
        configRepository.deleteAll();   // Razorpay is then recreated on first use as active and default, the legacy state
        FakeGateway.TEST_RESULT.set(ConnectionStatus.CONNECTION_SUCCESSFUL);
    }

    private GatewayDtos.Gateway row(String code) {
        return gateways.list().stream().filter(g -> g.code().equals(code)).findFirst().orElseThrow();
    }

    private void configureFake(String secret) {
        gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration("TEST",
            Map.of("apiKey", "fake_key_0001", "apiSecret", secret, "hookSecret", "hook-" + secret)));
    }

    // ------------------------------------------------------------------ registry and states

    @Test
    void theRegistryListsRazorpayAsOperationalAndTheOthersAsPlannedWithNoActions() {
        var all = gateways.list();

        assertThat(all).extracting(GatewayDtos.Gateway::code).contains("RAZORPAY", "STRIPE", "PAYPAL", "PAYU", "CASHFREE");
        var razorpay = row("RAZORPAY");
        assertThat(razorpay.implementation()).isEqualTo("IMPLEMENTED");
        assertThat(razorpay.configurationStatus()).isEqualTo("CONFIGURED");   // the test profile supplies the keys through the environment
        assertThat(razorpay.activated()).as("an installation that predates the screen keeps taking payments").isTrue();
        assertThat(razorpay.defaultGateway()).isTrue();
        assertThat(razorpay.connectionStatus()).isEqualTo("NOT_TESTED");
        for (String planned : new String[] {"STRIPE", "PAYPAL", "PAYU", "CASHFREE"}) {
            var g = row(planned);
            assertThat(g.implementation()).isEqualTo("PLANNED");
            assertThat(g.configurationStatus()).isEqualTo("NOT_AVAILABLE");
            assertThat(g.activated()).isFalse();
            assertThat(g.actions().configure() || g.actions().test() || g.actions().activate()).as(planned).isFalse();
            assertThatThrownBy(() -> gateways.test(ADMIN, g.code())).isInstanceOf(BillingConflictException.class);
            assertThatThrownBy(() -> gateways.activate(ADMIN, g.code())).isInstanceOf(BillingConflictException.class);
            assertThatThrownBy(() -> gateways.saveConfiguration(ADMIN, g.code(), new GatewayDtos.SaveConfiguration("TEST", Map.of())))
                .isInstanceOf(BillingConflictException.class);
        }
        var summary = gateways.summary();
        assertThat(summary.registered()).isEqualTo(all.size());
        assertThat(summary.implemented()).isEqualTo(2);   // Razorpay and the test provider
        assertThat(summary.active()).isEqualTo(1);
        assertThat(gateways.detail("razorpay").gateway().code()).isEqualTo("RAZORPAY");
        assertThatThrownBy(() -> gateways.detail("NOPE")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void configuredConnectedActiveAndDefaultAreSeparateStatuses() {
        var fake = row("FAKEPAY");
        assertThat(fake.configurationStatus()).isEqualTo("NOT_CONFIGURED");
        assertThat(fake.connectionStatus()).isEqualTo("NOT_CONFIGURED");

        gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration("TEST", Map.of("apiKey", "fake_key_0001")));
        assertThat(row("FAKEPAY").configurationStatus()).isEqualTo("CONFIGURATION_INCOMPLETE");
        assertThat(row("FAKEPAY").connectionStatus()).isEqualTo("CONFIGURATION_INCOMPLETE");
        assertThat(gateways.test(ADMIN, "FAKEPAY").status()).as("a test is not run on an incomplete configuration").isEqualTo("CONFIGURATION_INCOMPLETE");

        configureFake("s1");
        var configured = row("FAKEPAY");
        assertThat(configured.configurationStatus()).isEqualTo("CONFIGURED");
        assertThat(configured.connectionStatus()).isEqualTo("NOT_TESTED");
        assertThat(configured.activated()).isFalse();
        assertThat(configured.actions().activate()).isTrue();

        assertThat(gateways.test(ADMIN, "FAKEPAY").successful()).isTrue();
        assertThat(row("FAKEPAY").connectionStatus()).isEqualTo("CONNECTION_SUCCESSFUL");
        assertThat(row("FAKEPAY").activated()).as("connected is not active").isFalse();

        gateways.activate(ADMIN, "FAKEPAY");
        assertThat(row("FAKEPAY").activated()).isTrue();
        assertThat(row("FAKEPAY").defaultGateway()).as("Razorpay is still the default").isFalse();
        assertThat(row("RAZORPAY").activated()).as("several gateways can be active at once").isTrue();

        gateways.setDefault(ADMIN, "FAKEPAY");
        assertThat(row("FAKEPAY").defaultGateway()).isTrue();
        assertThat(row("RAZORPAY").defaultGateway()).isFalse();
        assertThat(row("RAZORPAY").activated()).isTrue();

        // Changing the credentials makes the earlier test stale.
        gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration(null, Map.of("apiKey", "fake_key_0002")));
        assertThat(row("FAKEPAY").connectionStatus()).isEqualTo("CONNECTION_SUCCESSFUL");   // an active gateway is re-tested in the same request
    }

    // ------------------------------------------------------------------ credentials

    @Test
    void credentialsAreEncryptedAtRestAndNeverReturned() throws Exception {
        configureFake("top-secret-value");

        PaymentGatewayConfig stored = configRepository.findByProviderCode("FAKEPAY").orElseThrow();
        assertThat(stored.getCredentialsEncrypted()).isNotBlank().doesNotContain("top-secret-value").doesNotContain("fake_key_0001");

        GatewayDtos.Detail detail = gateways.detail("FAKEPAY");
        String json = objectMapper.writeValueAsString(detail);
        assertThat(json).doesNotContain("top-secret-value").doesNotContain("hook-top-secret-value");
        var secret = detail.fields().stream().filter(f -> f.key().equals("apiSecret")).findFirst().orElseThrow();
        assertThat(secret.configured()).isTrue();
        assertThat(secret.source()).isEqualTo("STORED");
        assertThat(secret.maskedValue()).as("a secret is never shown, not even masked").isNull();
        var key = detail.fields().stream().filter(f -> f.key().equals("apiKey")).findFirst().orElseThrow();
        assertThat(key.maskedValue()).contains("••••••").doesNotContain("fake_key_0001");
        assertThat(detail.encryptionAvailable()).isTrue();
        assertThat(detail.webhook().secretConfigured()).isTrue();
    }

    @Test
    void aBlankFieldKeepsTheStoredValueAndBadInputIsRefused() {
        configureFake("keep-me");
        gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration("LIVE", Map.of("apiSecret", "   ")));

        assertThat(gateways.detail("FAKEPAY").fields().stream().filter(f -> f.key().equals("apiSecret")).findFirst().orElseThrow().configured()).isTrue();
        assertThat(row("FAKEPAY").environment()).isEqualTo("LIVE");
        assertThatThrownBy(() -> gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration("TEST", Map.of("nope", "x"))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown field");
        assertThatThrownBy(() -> gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration("STAGING", Map.of())))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Environment");
        assertThatThrownBy(() -> gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration("TEST", Map.of("apiKey", "bad\nvalue"))))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void razorpayKeysMustMatchTheChosenEnvironment() {
        assertThatThrownBy(() -> gateways.saveConfiguration(ADMIN, "RAZORPAY", new GatewayDtos.SaveConfiguration("LIVE", Map.of())))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Test key but the environment is Live");
        assertThatThrownBy(() -> gateways.saveConfiguration(ADMIN, "RAZORPAY", new GatewayDtos.SaveConfiguration("TEST", Map.of("keyId", "abc_123"))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("rzp_test_ or rzp_live_");
    }

    @Test
    void rotatingTheCredentialsOfAnActiveGatewayKeepsTheOldOnesWhenTheNewOnesFail() {
        configureFake("original");
        gateways.activate(ADMIN, "FAKEPAY");
        int version = gateways.detail("FAKEPAY").configVersion();

        FakeGateway.TEST_RESULT.set(ConnectionStatus.AUTHENTICATION_FAILED);
        assertThatThrownBy(() -> gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration(null, Map.of("apiSecret", "typo"))))
            .isInstanceOf(BillingConflictException.class).hasMessageContaining("Not saved").hasMessageContaining("unchanged");

        assertThat(gateways.detail("FAKEPAY").configVersion()).isEqualTo(version);
        assertThat(row("FAKEPAY").activated()).isTrue();
        PaymentGatewayConfig stored = configRepository.findByProviderCode("FAKEPAY").orElseThrow();
        assertThat(stored.getCredentialsUpdatedAt()).isNotNull();

        FakeGateway.TEST_RESULT.set(ConnectionStatus.CONNECTION_SUCCESSFUL);
        gateways.saveConfiguration(ADMIN, "FAKEPAY", new GatewayDtos.SaveConfiguration(null, Map.of("apiSecret", "rotated")));
        assertThat(gateways.detail("FAKEPAY").configVersion()).isGreaterThan(version);
        assertThat(row("FAKEPAY").connectionStatus()).isEqualTo("CONNECTION_SUCCESSFUL");
        assertThat(gateways.detail("FAKEPAY").history()).extracting(GatewayDtos.HistoryEntry::action).contains("PAYMENT_GATEWAY_CONFIGURED", "PAYMENT_GATEWAY_ACTIVATED");
    }

    // ------------------------------------------------------------------ activation and testing

    @Test
    void activationNeedsCompleteConfigurationAndAPassingConnectionTest() {
        assertThatThrownBy(() -> gateways.activate(ADMIN, "FAKEPAY")).isInstanceOf(BillingConflictException.class).hasMessageContaining("not fully configured");

        configureFake("s");
        for (ConnectionStatus failing : new ConnectionStatus[] {ConnectionStatus.AUTHENTICATION_FAILED, ConnectionStatus.PROVIDER_UNAVAILABLE,
                ConnectionStatus.UNSUPPORTED_CONFIGURATION, ConnectionStatus.TEST_FAILED}) {
            FakeGateway.TEST_RESULT.set(failing);
            assertThatThrownBy(() -> gateways.activate(ADMIN, "FAKEPAY")).as(failing.name()).isInstanceOf(BillingConflictException.class);
            assertThat(row("FAKEPAY").activated()).isFalse();
            assertThat(row("FAKEPAY").connectionStatus()).isEqualTo(failing.name());
        }
        FakeGateway.TEST_RESULT.set(ConnectionStatus.CONNECTION_SUCCESSFUL);
        gateways.activate(ADMIN, "FAKEPAY");
        gateways.deactivate(ADMIN, "FAKEPAY");
        assertThat(row("FAKEPAY").activated()).isFalse();
        assertThatThrownBy(() -> gateways.setDefault(ADMIN, "FAKEPAY")).isInstanceOf(BillingConflictException.class);
    }

    @Test
    void theRazorpayConnectionTestReportsWhatRazorpayActuallyAnswered() {
        doThrow(new com.vyoog.eisplatform.common.exception.RazorpayApiException("Razorpay returned an error (HTTP 401).", 401)).when(razorpayClient).testConnection();
        assertThat(gateways.test(ADMIN, "RAZORPAY").status()).isEqualTo("AUTHENTICATION_FAILED");

        doThrow(new com.vyoog.eisplatform.common.exception.RazorpayApiException("Could not reach Razorpay.", new RuntimeException())).when(razorpayClient).testConnection();
        assertThat(gateways.test(ADMIN, "RAZORPAY").status()).isEqualTo("PROVIDER_UNAVAILABLE");

        doThrow(new com.vyoog.eisplatform.common.exception.RazorpayApiException("Razorpay returned an error (HTTP 400).", 400)).when(razorpayClient).testConnection();
        assertThat(gateways.test(ADMIN, "RAZORPAY").status()).isEqualTo("TEST_FAILED");

        org.mockito.Mockito.doNothing().when(razorpayClient).testConnection();
        var ok = gateways.test(ADMIN, "RAZORPAY");
        assertThat(ok.successful()).isTrue();
        assertThat(row("RAZORPAY").connectionStatus()).isEqualTo("CONNECTION_SUCCESSFUL");
        assertThat(gateways.detail("RAZORPAY").evidence().authenticated()).isTrue();
        assertThat(gateways.detail("RAZORPAY").evidence().paymentCaptured()).as("a passing test says nothing about payments").isFalse();
    }

    @Test
    void paymentMethodsCanBeSwitchedOffButOnlyOnesTheProviderSupports() {
        gateways.setMethods(ADMIN, "RAZORPAY", java.util.List.of("card", "upi"));
        assertThat(row("RAZORPAY").enabledMethods()).containsExactly("card", "upi");
        assertThat(routing.offeredMethods("INR")).contains("card", "upi").doesNotContain("wallet", "netbanking");
        assertThatThrownBy(() -> gateways.setMethods(ADMIN, "RAZORPAY", java.util.List.of("paypal"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> gateways.setMethods(ADMIN, "RAZORPAY", java.util.List.of())).isInstanceOf(IllegalArgumentException.class);
        gateways.setMethods(ADMIN, "RAZORPAY", java.util.List.of("card", "upi", "netbanking", "wallet"));
        assertThat(row("RAZORPAY").enabledMethods()).hasSize(4);
    }

    // ------------------------------------------------------------------ routing

    @Test
    void theBackendRoutesByActivationCurrencyMethodAndDefault() {
        assertThat(routing.select("USD", null).code()).isEqualTo("RAZORPAY");

        configureFake("s");
        gateways.activate(ADMIN, "FAKEPAY");
        assertThat(routing.select("INR", "card").code()).as("Razorpay is still the default").isEqualTo("RAZORPAY");
        gateways.setDefault(ADMIN, "FAKEPAY");
        assertThat(routing.select("INR", "card").code()).isEqualTo("FAKEPAY");
        assertThat(routing.select("EUR", "card").code()).as("FakePay does not take EUR").isEqualTo("RAZORPAY");
        assertThat(routing.select("INR", "wallet").code()).as("FakePay has no wallet").isEqualTo("RAZORPAY");

        gateways.deactivate(ADMIN, "RAZORPAY");
        assertThat(routing.select("INR", "upi").code()).isEqualTo("FAKEPAY");
        assertThatThrownBy(() -> routing.select("EUR", null)).isInstanceOf(PaymentGatewayNotConfiguredException.class).hasMessageContaining("EUR");
        gateways.deactivate(ADMIN, "FAKEPAY");
        assertThat(routing.available("INR")).isFalse();
        assertThatThrownBy(() -> paymentFor("USD")).isInstanceOf(PaymentGatewayNotConfiguredException.class).hasMessageContaining("No payment gateway is available");
    }

    // ------------------------------------------------------------------ payments and webhooks

    private Invoice invoice() {
        Customer c = new Customer();
        c.setEmail("gw-" + System.nanoTime() + "@example.com");
        c.setFirstName("Gate");
        c.setLastName("Way");
        c = customerRepository.save(c);
        tax.readyToInvoiceInIndia(c);
        Product p = new Product();
        p.setName("Gateway Product " + System.nanoTime());
        p.setPrice(BigDecimal.valueOf(500));
        p.setStatus(ProductStatus.ACTIVE);
        p = productRepository.save(p);
        ProductSubscription s = new ProductSubscription();
        s.setProductId(p.getId());
        s.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        s.setOwnerCustomerId(c.getId());
        s.setStatus(SubscriptionStatus.PENDING_SUBSCRIPTION);
        return invoiceService.generateForSubscription(subscriptionRepository.save(s).getId());
    }

    private CreatePaymentResponse paymentFor(String ignoredCurrency) {
        Invoice invoice = invoice();
        return paymentService.createPaymentForInvoice(invoice.getOwnerCustomerId(), null, invoice.getId(), null);
    }

    @Test
    void aPaymentKeepsTheProviderThatCreatedItWhateverIsDefaultLater() {
        configureFake("s");
        gateways.activate(ADMIN, "FAKEPAY");
        gateways.setDefault(ADMIN, "FAKEPAY");
        Invoice invoice = invoice();

        CreatePaymentResponse created = paymentService.createPaymentForInvoice(invoice.getOwnerCustomerId(), null, invoice.getId(), "card");

        assertThat(created.provider()).isEqualTo("FAKEPAY");
        assertThat(created.providerOrderId()).startsWith("fake_order_");
        assertThat(created.clientConfig()).containsEntry("keyId", "fake_key_0001").doesNotContainKey("apiSecret");
        assertThat(paymentRepository.findById(created.paymentId()).orElseThrow().getProvider()).isEqualTo("FAKEPAY");

        gateways.setDefault(ADMIN, "RAZORPAY");
        gateways.deactivate(ADMIN, "FAKEPAY");   // deactivated, but its payment can still be completed and refunded

        var confirmed = paymentService.confirmPayment(invoice.getOwnerCustomerId(), null, created.paymentId(),
            new ConfirmPaymentRequest(created.providerOrderId(), "fake_pay_1", "good"));
        assertThat(confirmed.status()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(invoiceService.getById(invoice.getId()).getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(gateways.detail("FAKEPAY").evidence().paymentCaptured()).isTrue();

        var refunded = paymentService.refundPayment(1L, created.paymentId(), new RefundRequest(10000L, "test"));
        assertThat(refunded.refundedAmount()).isEqualTo(10000L);
        assertThat(gateways.detail("FAKEPAY").evidence().refundMade()).isTrue();
        org.mockito.Mockito.verify(razorpayClient, org.mockito.Mockito.never()).createRefund(anyString(), anyLong(), anyString());

        Invoice second = invoice();
        var secondPayment = paymentService.createPaymentForInvoice(second.getOwnerCustomerId(), null, second.getId(), "card");
        assertThat(secondPayment.provider()).as("new payments follow the new default").isEqualTo("RAZORPAY");
    }

    @Test
    void aBadSignatureOrAnInactiveRouteNeverCapturesAPayment() {
        configureFake("s");
        gateways.activate(ADMIN, "FAKEPAY");
        gateways.setDefault(ADMIN, "FAKEPAY");
        Invoice invoice = invoice();
        CreatePaymentResponse created = paymentService.createPaymentForInvoice(invoice.getOwnerCustomerId(), null, invoice.getId(), "card");

        assertThatThrownBy(() -> paymentService.confirmPayment(invoice.getOwnerCustomerId(), null, created.paymentId(),
            new ConfirmPaymentRequest(created.providerOrderId(), "fake_pay_1", "forged"))).isInstanceOf(InvalidPaymentSignatureException.class);

        assertThat(paymentRepository.findById(created.paymentId()).orElseThrow().getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(invoiceService.getById(invoice.getId()).getStatus()).isEqualTo(InvoiceStatus.OPEN);
    }

    @Test
    void webhooksAreVerifiedPerProviderProcessedOnceAndTrackedWithoutTrustingAnUnsignedCaller() {
        configureFake("whsecret");
        gateways.activate(ADMIN, "FAKEPAY");
        gateways.setDefault(ADMIN, "FAKEPAY");
        Invoice invoice = invoice();
        CreatePaymentResponse created = paymentService.createPaymentForInvoice(invoice.getOwnerCustomerId(), null, invoice.getId(), "card");
        String body = "evt-1,captured," + created.providerOrderId() + ",fake_pay_9";
        Map<String, String> good = Map.of("X-Fake-Signature", "sig-hook-whsecret");

        assertThatThrownBy(() -> paymentService.processWebhook("fakepay", body, Map.of("X-Fake-Signature", "sig-wrong")))
            .isInstanceOf(InvalidPaymentSignatureException.class);
        assertThatThrownBy(() -> paymentService.processWebhook("fakepay", body, Map.of())).isInstanceOf(InvalidPaymentSignatureException.class);
        assertThatThrownBy(() -> paymentService.processWebhook("fakepay", "garbage", good)).isInstanceOf(InvalidPaymentSignatureException.class);
        assertThat(paymentRepository.findById(created.paymentId()).orElseThrow().getStatus()).as("nothing happened yet").isEqualTo(PaymentStatus.CREATED);
        var afterFailures = gateways.detail("FAKEPAY").webhook();
        assertThat(afterFailures.failureCount()).isEqualTo(3);
        assertThat(afterFailures.lastFailureReason()).isNotBlank();
        assertThat(afterFailures.signatureStatus()).isEqualTo("NOT_SEEN");

        paymentService.processWebhook("FAKEPAY", body, good);
        paymentService.processWebhook("fakepay", body, good);   // the same event delivered again

        Payment payment = paymentRepository.findById(created.paymentId()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(payment.getProviderPaymentId()).isEqualTo("fake_pay_9");
        assertThat(invoiceService.getById(invoice.getId()).getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(webhookEventRepository.findTop20ByProviderOrderByReceivedAtDesc("FAKEPAY")).hasSize(1);
        var webhook = gateways.detail("FAKEPAY").webhook();
        assertThat(webhook.signatureStatus()).isEqualTo("VERIFIED");
        assertThat(webhook.lastEventType()).isEqualTo("captured");
        assertThat(webhook.url()).endsWith("/webhooks/fakepay");
        assertThat(gateways.detail("FAKEPAY").recentEvents()).hasSize(1);
    }

    @Test
    void aWebhookOfAnUnknownOrPlannedProviderIsRefusedAndEventIdsAreUniquePerProviderOnly() {
        assertThatThrownBy(() -> paymentService.processWebhook("stripe", "{}", Map.of())).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> paymentService.processWebhook("nope", "{}", Map.of())).isInstanceOf(ResourceNotFoundException.class);

        when(razorpayClient.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        String razorpayBody = "{\"id\":\"evt-1\",\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_x\",\"order_id\":\"order_none\"}}}}";
        paymentService.processWebhook("razorpay", razorpayBody, Map.of("x-razorpay-signature", "sig"));
        configureFake("w");
        paymentService.processWebhook("fakepay", "evt-1,other,o,p", Map.of("X-Fake-Signature", "sig-hook-w"));   // the same event id, another provider

        assertThat(webhookEventRepository.findByProviderAndProviderEventId("RAZORPAY", "evt-1")).isPresent();
        assertThat(webhookEventRepository.findByProviderAndProviderEventId("FAKEPAY", "evt-1")).isPresent();
        when(razorpayClient.verifyWebhookSignature(anyString(), any())).thenReturn(false);
        assertThatThrownBy(() -> paymentService.processWebhook("razorpay", razorpayBody, Map.of("x-razorpay-signature", "bad")))
            .isInstanceOf(InvalidPaymentSignatureException.class);
        assertThat(row("RAZORPAY").activated()).isTrue();
    }
}
