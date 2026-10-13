package com.vyoog.eisplatform.modules.billing.service.gateway;

import com.vyoog.eisplatform.modules.billing.dto.GatewayStatusDto;

import java.util.Set;

/** C97 BI-8: a payment gateway the platform can offer at checkout. The payment screens and the admin "Payment gateways" panel only
 * know this interface, never a vendor, so another gateway is linked by adding one implementation (a Spring bean) with its credentials
 * read from the backend environment (BR-SEC-001); no screen changes. Razorpay is the first implementation. Creating and confirming a
 * payment is still done by {@code PaymentService} against Razorpay; a second gateway extends that when it is chosen. */
public interface PaymentGateway {

    /** Stable upper-case code stored in billing settings, for example RAZORPAY. */
    String code();

    /** Name shown to administrators and customers. */
    String displayName();

    /** Payment methods the gateway can take, from card, upi, netbanking, wallet. */
    Set<String> supportedMethods();

    boolean isConfigured();

    /** Read-only status: which credentials are set (never their values), webhook URL, last webhook. */
    GatewayStatusDto status();

    /** A cheap authenticated call to the gateway; throws when it cannot be reached or the credentials are refused. */
    void testConnection();
}
