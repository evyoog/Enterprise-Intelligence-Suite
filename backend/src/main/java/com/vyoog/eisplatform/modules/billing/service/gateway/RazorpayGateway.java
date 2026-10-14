package com.vyoog.eisplatform.modules.billing.service.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.common.exception.RazorpayApiException;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayPaymentInfo;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** The Razorpay adapter (C98): the only implemented provider. Razorpay calls go through {@link RazorpayClient}, whose credentials come from
 * {@link RazorpayProperties} (stored values over the secrets file), so this adapter does not repeat them. Everything Razorpay-specific lives
 * here: the signature rules, the payload shapes and the status words. */
@Component
@RequiredArgsConstructor
public class RazorpayGateway implements PaymentGateway {

    public static final String CODE = "RAZORPAY";

    private final RazorpayClient client;
    private final RazorpayProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public GatewayDescriptor descriptor() {
        return new GatewayDescriptor(CODE, "Razorpay", "Cards, UPI, netbanking and wallets for payments in India and, where enabled on your account, abroad.",
            "razorpay", List.of("IN"),
            java.util.Arrays.stream(com.vyoog.eisplatform.modules.product.model.Currency.values()).map(Enum::name).toList(),
            List.of("card", "upi", "netbanking", "wallet"), List.of("INVOICE_PAYMENT"), List.of("TEST", "LIVE"), true, true, false,
            GatewayDescriptor.Implementation.IMPLEMENTED,
            List.of(ConfigField.of("keyId", "Key ID", false, true, "Starts with rzp_test_ for the Test environment or rzp_live_ for Live."),
                ConfigField.of("keySecret", "Key secret", true, true, "Shown once in the Razorpay Dashboard when the key is created."),
                ConfigField.webhookSecret("webhookSecret", "Webhook secret", true, "The secret you enter when you add the webhook in the Razorpay Dashboard.")),
            List.of("In the Razorpay Dashboard, create a Test key (or a Live key when you go live).",
                "Enter the Key ID, Key secret and the environment here, then choose Test connection.",
                "In the Dashboard, add a webhook for the URL shown here, choose a secret and enter the same secret here.",
                "Subscribe the webhook to payment.captured, payment.failed and refund.processed.",
                "Activate the gateway once the test passes."),
            List.of("payment.captured", "payment.failed", "refund.processed"));
    }

    @Override
    public Map<String, String> environmentCredentials() {
        return properties.environmentValues();
    }

    @Override
    public String validateConfiguration(GatewayContext ctx) {
        String keyId = ctx.credential("keyId");
        if (keyId.isBlank()) {
            return null;
        }
        boolean test = keyId.startsWith("rzp_test_");
        boolean live = keyId.startsWith("rzp_live_");
        if (!test && !live) {
            return "The Key ID must start with rzp_test_ or rzp_live_.";
        }
        if (ctx.environment() != null && test && "LIVE".equals(ctx.environment())) {
            return "This is a Test key but the environment is Live.";
        }
        if (ctx.environment() != null && live && "TEST".equals(ctx.environment())) {
            return "This is a Live key but the environment is Test.";
        }
        return null;
    }

    @Override
    public String mask(String fieldKey, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        int prefixLen = value.indexOf('_', value.indexOf('_') + 1) + 1;
        if (prefixLen <= 0 || value.length() <= prefixLen + 4) {
            return "••••••";
        }
        return value.substring(0, prefixLen) + "••••••" + value.substring(value.length() - 4);
    }

    @Override
    public ConnectionResult testConnection(GatewayContext ctx) {
        try {
            client.testConnection();
            return ConnectionResult.of(ConnectionStatus.CONNECTION_SUCCESSFUL, "Razorpay accepted the credentials.");
        } catch (RazorpayApiException e) {
            Integer status = e.getHttpStatus();
            if (status != null && (status == 401 || status == 403)) {
                return ConnectionResult.of(ConnectionStatus.AUTHENTICATION_FAILED, "Razorpay refused the credentials (HTTP " + status + ").");
            }
            if (status == null) {
                return ConnectionResult.of(ConnectionStatus.PROVIDER_UNAVAILABLE, "Could not reach Razorpay.");
            }
            if (status >= 500) {
                return ConnectionResult.of(ConnectionStatus.PROVIDER_UNAVAILABLE, "Razorpay is not available right now (HTTP " + status + ").");
            }
            return ConnectionResult.of(ConnectionStatus.TEST_FAILED, "Razorpay rejected the test request (HTTP " + status + ").");
        } catch (RuntimeException e) {
            return ConnectionResult.of(ConnectionStatus.TEST_FAILED, "The connection test failed.");
        }
    }

    @Override
    public GatewayOrder createOrder(GatewayContext ctx, long amountMinorUnits, String currencyCode, String receipt) {
        String orderId = client.createOrder(amountMinorUnits, currencyCode, receipt);
        return new GatewayOrder(orderId, Map.of("keyId", properties.getKeyId()));
    }

    @Override
    public boolean verifyPayment(GatewayContext ctx, String providerOrderId, String providerPaymentId, String signature) {
        return client.verifyPaymentSignature(providerOrderId, providerPaymentId, signature);
    }

    @Override
    public GatewayPaymentInfo fetchPayment(GatewayContext ctx, String providerPaymentId) {
        RazorpayPaymentInfo info = client.fetchPayment(providerPaymentId);
        boolean captured = "captured".equalsIgnoreCase(info.status()) || "authorized".equalsIgnoreCase(info.status());
        return new GatewayPaymentInfo(captured ? GatewayPaymentInfo.Status.CAPTURED : GatewayPaymentInfo.Status.FAILED,
            info.methodType(), info.methodNetwork(), info.methodLast4(), info.failureReason());
    }

    @Override
    public String refund(GatewayContext ctx, String providerPaymentId, long amountMinorUnits, String reason) {
        return client.createRefund(providerPaymentId, amountMinorUnits, reason);
    }

    @Override
    public boolean verifyWebhook(GatewayContext ctx, String rawBody, Map<String, String> headers) {
        String signature = headers.entrySet().stream().filter(e -> "x-razorpay-signature".equalsIgnoreCase(e.getKey()))
            .map(Map.Entry::getValue).findFirst().orElse(null);
        return signature != null && client.verifyWebhookSignature(rawBody, signature);
    }

    @Override
    public GatewayWebhookEvent parseWebhook(String rawBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new IllegalArgumentException("The webhook body is not JSON.");
        }
        String eventType = root.path("event").asText("unknown");
        JsonNode payment = root.path("payload").path("payment").path("entity");
        JsonNode refund = root.path("payload").path("refund").path("entity");
        String eventId = root.hasNonNull("id") ? root.path("id").asText()
            : eventType + ":" + payment.path("id").asText(refund.path("id").asText("unknown"));
        return switch (eventType) {
            case "payment.captured" -> new GatewayWebhookEvent(eventId, GatewayWebhookEvent.Type.PAYMENT_CAPTURED, eventType,
                payment.path("order_id").asText(null), payment.path("id").asText(null), payment.path("method").asText(null),
                payment.path("card").path("network").asText(null), payment.path("card").path("last4").asText(null), null);
            case "payment.failed" -> new GatewayWebhookEvent(eventId, GatewayWebhookEvent.Type.PAYMENT_FAILED, eventType,
                payment.path("order_id").asText(null), payment.path("id").asText(null), null, null, null,
                payment.path("error_description").asText(null));
            case "refund.processed" -> new GatewayWebhookEvent(eventId, GatewayWebhookEvent.Type.REFUND_PROCESSED, eventType,
                null, refund.path("payment_id").asText(null), null, null, null, null);
            default -> new GatewayWebhookEvent(eventId, GatewayWebhookEvent.Type.IGNORED, eventType, null, null, null, null, null, null);
        };
    }
}
