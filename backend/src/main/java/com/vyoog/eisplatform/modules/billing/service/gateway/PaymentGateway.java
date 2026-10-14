package com.vyoog.eisplatform.modules.billing.service.gateway;

import java.util.Map;

/**
 * C98 (REQ-BIL-003): a working integration with one payment provider. The billing module and the Payment Gateways page only know this interface
 * and {@link GatewayDescriptor}; every provider-specific thing (credentials, signatures, API calls, response formats) stays inside the adapter.
 * An adapter is a Spring bean; it appears in the registry and on the page when it exists. A provider without an adapter can only be listed as
 * configuration-only (an administrator may add it and store its credentials) and is never shown as operational.
 *
 * <p>Adapters are stateless: everything that varies (credentials, environment) arrives in the {@link GatewayContext}. They translate provider
 * statuses into EIS's own ({@link GatewayPaymentInfo.Status}) and never put provider details into the billing model.
 */
public interface PaymentGateway {

    GatewayDescriptor descriptor();

    default String code() {
        return descriptor().code();
    }

    /** Credentials the deployment already supplies through its environment or secrets file, by field key. Used when nothing is stored. */
    default Map<String, String> environmentCredentials() {
        return Map.of();
    }

    /** A message when the configuration cannot work (for example a key that does not match the chosen environment), or null. No network call. */
    default String validateConfiguration(GatewayContext ctx) {
        return null;
    }

    /** How a non-secret identifier is shown on screen. Never called for a secret field. */
    default String mask(String fieldKey, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= 8 ? "••••••" : value.substring(0, 4) + "••••••" + value.substring(value.length() - 4);
    }

    /** A real, authenticated call to the provider. Never simulated. */
    ConnectionResult testConnection(GatewayContext ctx);

    GatewayOrder createOrder(GatewayContext ctx, long amountMinorUnits, String currencyCode, String receipt);

    boolean verifyPayment(GatewayContext ctx, String providerOrderId, String providerPaymentId, String signature);

    GatewayPaymentInfo fetchPayment(GatewayContext ctx, String providerPaymentId);

    /** Returns the provider's refund id. */
    String refund(GatewayContext ctx, String providerPaymentId, long amountMinorUnits, String reason);

    /** Verifies the webhook the way the provider documents it, against the raw body. */
    boolean verifyWebhook(GatewayContext ctx, String rawBody, Map<String, String> headers);

    /** Parses a webhook whose signature has been verified. Throws IllegalArgumentException when the body is not understood. */
    GatewayWebhookEvent parseWebhook(String rawBody);
}
