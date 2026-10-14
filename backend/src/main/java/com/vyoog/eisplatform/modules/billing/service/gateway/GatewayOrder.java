package com.vyoog.eisplatform.modules.billing.service.gateway;

import java.util.Map;

/** A payment order created at the provider. {@code clientConfig} holds only what the browser needs to open that provider's payment window
 * (for Razorpay, the public key id), never a secret. */
public record GatewayOrder(String orderId, Map<String, String> clientConfig) {
}
