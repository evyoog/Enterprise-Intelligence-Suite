package com.vyoog.eisplatform.modules.billing.service.gateway;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** A test-only second provider, so the generic behaviour (configuration, activation, routing, webhooks) is tested against something other than
 * Razorpay. It makes no network call: tests set what {@link #TEST_RESULT} the next connection test returns. */
public class FakeGateway implements PaymentGateway {

    public static final AtomicReference<ConnectionStatus> TEST_RESULT = new AtomicReference<>(ConnectionStatus.CONNECTION_SUCCESSFUL);
    public static final AtomicInteger ORDERS = new AtomicInteger();
    public static final AtomicInteger REFUNDS = new AtomicInteger();

    @Override
    public GatewayDescriptor descriptor() {
        return new GatewayDescriptor("FAKEPAY", "FakePay", "Test-only provider.", "fakepay", List.of("IN"), List.of("INR", "USD"), List.of("card", "upi"),
            List.of("INVOICE_PAYMENT"), List.of("TEST", "LIVE"), true, true, false, GatewayDescriptor.Implementation.IMPLEMENTED,
            List.of(ConfigField.of("apiKey", "API key", false, true, null), ConfigField.of("apiSecret", "API secret", true, true, null),
                ConfigField.webhookSecret("hookSecret", "Hook secret", true, null)),
            List.of("Step one."), List.of("captured"));
    }

    @Override
    public ConnectionResult testConnection(GatewayContext ctx) {
        ConnectionStatus status = TEST_RESULT.get();
        return ConnectionResult.of(status, status == ConnectionStatus.CONNECTION_SUCCESSFUL ? "ok" : "FakePay said " + status);
    }

    @Override
    public GatewayOrder createOrder(GatewayContext ctx, long amountMinorUnits, String currencyCode, String receipt) {
        return new GatewayOrder("fake_order_" + ORDERS.incrementAndGet(), Map.of("keyId", ctx.credential("apiKey")));
    }

    @Override
    public boolean verifyPayment(GatewayContext ctx, String providerOrderId, String providerPaymentId, String signature) {
        return "good".equals(signature);
    }

    @Override
    public GatewayPaymentInfo fetchPayment(GatewayContext ctx, String providerPaymentId) {
        return new GatewayPaymentInfo(GatewayPaymentInfo.Status.CAPTURED, "card", "Visa", "4242", null);
    }

    @Override
    public String refund(GatewayContext ctx, String providerPaymentId, long amountMinorUnits, String reason) {
        return "fake_refund_" + REFUNDS.incrementAndGet();
    }

    @Override
    public boolean verifyWebhook(GatewayContext ctx, String rawBody, Map<String, String> headers) {
        return ("sig-" + ctx.credential("hookSecret")).equals(headers.get("X-Fake-Signature"));
    }

    @Override
    public GatewayWebhookEvent parseWebhook(String rawBody) {
        String[] parts = rawBody.split(",");   // "eventId,type,orderId,paymentId"
        if (parts.length < 4) {
            throw new IllegalArgumentException("bad body");
        }
        return new GatewayWebhookEvent(parts[0], "captured".equals(parts[1]) ? GatewayWebhookEvent.Type.PAYMENT_CAPTURED : GatewayWebhookEvent.Type.IGNORED,
            parts[1], parts[2], parts[3], "card", null, null, null);
    }
}
