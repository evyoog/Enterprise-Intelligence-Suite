package com.vyoog.eisplatform.modules.billing.service.gateway;

import java.util.List;

/** What the provider registry knows about a payment provider (C98, REQ-BIL-003.2). The Payment Gateways page is built from this and from the
 * database only; adding a provider adds no UI code. {@code implementation} says whether a working backend adapter exists. */
public record GatewayDescriptor(
    String code,
    String name,
    String description,
    /** A key the frontend maps to a logo or icon; unknown keys get a generic icon. */
    String logo,
    List<String> countries,
    List<String> currencies,
    /** Payment methods the provider can take, from card, upi, netbanking, wallet, bank_transfer, paypal. */
    List<String> methods,
    List<String> transactionTypes,
    List<String> environments,
    boolean webhookSupport,
    boolean refundSupport,
    boolean recurringSupport,
    Implementation implementation,
    List<ConfigField> fields,
    List<String> setupSteps,
    List<String> webhookEvents
) {
    /** IMPLEMENTED: a working adapter exists. CONFIGURATION_ONLY: an administrator added the gateway and its credentials are stored, but EIS has no
     * adapter for it, so it can never be tested, activated or routed to. */
    public enum Implementation { IMPLEMENTED, CONFIGURATION_ONLY }

    public boolean implemented() {
        return implementation == Implementation.IMPLEMENTED;
    }

    /** True for a gateway an administrator added (no adapter). */
    public boolean custom() {
        return implementation == Implementation.CONFIGURATION_ONLY;
    }

    public String webhookPath() {
        return "/webhooks/" + code.toLowerCase(java.util.Locale.ROOT);
    }
}
