package com.vyoog.eisplatform.modules.billing.service.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayPaymentInfo;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** C98: what is Razorpay-specific stays in the adapter: statuses, signatures, payload shapes. */
class RazorpayGatewayTest {

    private final RazorpayClient client = mock(RazorpayClient.class);
    private final RazorpayGateway gateway = new RazorpayGateway(client, new RazorpayProperties("rzp_test_abcdef123456", "secret", "whsec"), new ObjectMapper());

    private GatewayContext ctx(String environment, String keyId) {
        return new GatewayContext("RAZORPAY", environment, Map.of("keyId", keyId, "keySecret", "s", "webhookSecret", "w"));
    }

    @Test
    void theDescriptorDescribesAnOperationalProviderWithItsFields() {
        GatewayDescriptor d = gateway.descriptor();
        assertThat(d.implemented()).isTrue();
        assertThat(d.methods()).containsExactlyInAnyOrder("card", "upi", "netbanking", "wallet");
        assertThat(d.currencies()).contains("INR");
        assertThat(d.fields()).extracting(ConfigField::key).containsExactly("keyId", "keySecret", "webhookSecret");
        assertThat(d.fields()).filteredOn(ConfigField::secret).extracting(ConfigField::key).containsExactly("keySecret", "webhookSecret");
        assertThat(d.webhookPath()).isEqualTo("/webhooks/razorpay");
        assertThat(d.webhookEvents()).contains("payment.captured", "payment.failed", "refund.processed");
    }

    @Test
    void theKeyAndTheEnvironmentMustAgree() {
        assertThat(gateway.validateConfiguration(ctx("TEST", "rzp_test_x"))).isNull();
        assertThat(gateway.validateConfiguration(ctx("LIVE", "rzp_live_x"))).isNull();
        assertThat(gateway.validateConfiguration(ctx(null, "rzp_live_x"))).as("a legacy configuration has no environment yet").isNull();
        assertThat(gateway.validateConfiguration(ctx("LIVE", "rzp_test_x"))).contains("Test key");
        assertThat(gateway.validateConfiguration(ctx("TEST", "rzp_live_x"))).contains("Live key");
        assertThat(gateway.validateConfiguration(ctx("TEST", "oops"))).contains("rzp_test_ or rzp_live_");
        assertThat(gateway.mask("keyId", "rzp_live_1234567890")).isEqualTo("rzp_live_••••••7890");
    }

    @Test
    void statusesFromRazorpayBecomeEisStatuses() {
        when(client.fetchPayment("pay_1")).thenReturn(new RazorpayPaymentInfo("pay_1", "captured", "card", "Visa", "4242", null));
        when(client.fetchPayment("pay_2")).thenReturn(new RazorpayPaymentInfo("pay_2", "authorized", "upi", null, null, null));
        when(client.fetchPayment("pay_3")).thenReturn(new RazorpayPaymentInfo("pay_3", "failed", "card", null, null, "declined"));

        assertThat(gateway.fetchPayment(ctx("TEST", "rzp_test_x"), "pay_1").status()).isEqualTo(GatewayPaymentInfo.Status.CAPTURED);
        assertThat(gateway.fetchPayment(ctx("TEST", "rzp_test_x"), "pay_2").status()).isEqualTo(GatewayPaymentInfo.Status.CAPTURED);
        var failed = gateway.fetchPayment(ctx("TEST", "rzp_test_x"), "pay_3");
        assertThat(failed.status()).isEqualTo(GatewayPaymentInfo.Status.FAILED);
        assertThat(failed.failureReason()).isEqualTo("declined");
    }

    @Test
    void webhookSignaturesAreReadFromRazorpaysHeaderWhateverItsCase() {
        when(client.verifyWebhookSignature("body", "sig")).thenReturn(true);
        assertThat(gateway.verifyWebhook(ctx("TEST", "rzp_test_x"), "body", Map.of("X-Razorpay-Signature", "sig"))).isTrue();
        assertThat(gateway.verifyWebhook(ctx("TEST", "rzp_test_x"), "body", Map.of("x-razorpay-signature", "sig"))).isTrue();
        assertThat(gateway.verifyWebhook(ctx("TEST", "rzp_test_x"), "body", Map.of("x-razorpay-signature", "other"))).isFalse();
        assertThat(gateway.verifyWebhook(ctx("TEST", "rzp_test_x"), "body", Map.of())).isFalse();
    }

    @Test
    void webhookPayloadsBecomeNormalisedEvents() {
        var captured = gateway.parseWebhook("{\"id\":\"evt_1\",\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":"
            + "{\"id\":\"pay_9\",\"order_id\":\"order_9\",\"method\":\"card\",\"card\":{\"network\":\"Visa\",\"last4\":\"4242\"}}}}}");
        assertThat(captured.type()).isEqualTo(GatewayWebhookEvent.Type.PAYMENT_CAPTURED);
        assertThat(captured.eventId()).isEqualTo("evt_1");
        assertThat(captured.providerOrderId()).isEqualTo("order_9");
        assertThat(captured.methodNetwork()).isEqualTo("Visa");

        var failed = gateway.parseWebhook("{\"event\":\"payment.failed\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_8\",\"order_id\":\"order_8\",\"error_description\":\"declined\"}}}}");
        assertThat(failed.type()).isEqualTo(GatewayWebhookEvent.Type.PAYMENT_FAILED);
        assertThat(failed.eventId()).as("no id in the body: derived from the event and the payment").isEqualTo("payment.failed:pay_8");
        assertThat(failed.failureReason()).isEqualTo("declined");

        var refund = gateway.parseWebhook("{\"id\":\"evt_3\",\"event\":\"refund.processed\",\"payload\":{\"refund\":{\"entity\":{\"id\":\"rfnd_1\",\"payment_id\":\"pay_9\"}}}}");
        assertThat(refund.type()).isEqualTo(GatewayWebhookEvent.Type.REFUND_PROCESSED);
        assertThat(refund.providerPaymentId()).isEqualTo("pay_9");

        assertThat(gateway.parseWebhook("{\"id\":\"evt_4\",\"event\":\"order.paid\"}").type()).isEqualTo(GatewayWebhookEvent.Type.IGNORED);
        assertThatThrownBy(() -> gateway.parseWebhook("not json")).isInstanceOf(IllegalArgumentException.class);
        when(client.createOrder(100L, "INR", "r1")).thenReturn("order_1");
        var order = gateway.createOrder(ctx("TEST", "rzp_test_x"), 100L, "INR", "r1");
        assertThat(order.orderId()).isEqualTo("order_1");
        assertThat(order.clientConfig()).containsOnlyKeys("keyId");
        assertThat(order.clientConfig().toString()).doesNotContain("secret");
    }
}
