package com.vyoog.eisplatform.modules.billing.service.razorpay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Reads Razorpay credentials from the common secrets file (BR-SEC-001,
 * config/secrets.env) via the vyoog.billing.razorpay.* properties in
 * application.yml — deliberately empty defaults, unlike this file's other
 * committed dev-convenience secrets, since "empty" is a real, intended mode
 * for this integration (BR-10 "Payment gateway not configured"), not a
 * placeholder waiting to be filled in for local dev. */
@Component
public class RazorpayProperties {

    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;
    /** C98: credentials an administrator stored through the Payment Gateways page, which win over the environment's. Absent in plain unit tests. */
    private com.vyoog.eisplatform.modules.billing.service.gateway.GatewayCredentialStore store;

    public RazorpayProperties(
            @Value("${vyoog.billing.razorpay.key-id:}") String keyId,
            @Value("${vyoog.billing.razorpay.key-secret:}") String keySecret,
            @Value("${vyoog.billing.razorpay.webhook-secret:}") String webhookSecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setStore(com.vyoog.eisplatform.modules.billing.service.gateway.GatewayCredentialStore store) {
        this.store = store;
    }

    private String resolve(String field, String environmentValue) {
        if (store == null) {
            return environmentValue;
        }
        String stored = store.stored("RAZORPAY").get(field);
        return stored == null || stored.isBlank() ? environmentValue : stored;
    }

    public String getKeyId() {
        return resolve("keyId", keyId);
    }

    public String getKeySecret() {
        return resolve("keySecret", keySecret);
    }

    public String getWebhookSecret() {
        return resolve("webhookSecret", webhookSecret);
    }

    /** What the environment or secrets file supplies, before any stored value (C98). Empty strings when not set. */
    public java.util.Map<String, String> environmentValues() {
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        if (!keyId.isBlank()) values.put("keyId", keyId);
        if (!keySecret.isBlank()) values.put("keySecret", keySecret);
        if (!webhookSecret.isBlank()) values.put("webhookSecret", webhookSecret);
        return values;
    }

    /** BR-10: the platform runs in "Payment gateway not configured" mode
     * unless every credential is present. */
    public boolean isConfigured() {
        return !getKeyId().isBlank() && !getKeySecret().isBlank() && !getWebhookSecret().isBlank();
    }

    /** Derived from the key ID prefix, not a separate setting — Razorpay's
     * own convention (REQ-BIL-001.13). */
    public boolean isLiveMode() {
        return getKeyId().startsWith("rzp_live_");
    }

    /** For example {@code rzp_live_••••••1234} — the {@code rzp_live_}/
     * {@code rzp_test_} prefix and last 4 characters are kept, the rest
     * masked (REQ-BIL-001.13: "the key ID masked"). */
    public String maskedKeyId() {
        String keyId = getKeyId();
        if (keyId.isBlank()) {
            return null;
        }
        int prefixLen = keyId.indexOf('_', keyId.indexOf('_') + 1) + 1;
        if (prefixLen <= 0 || keyId.length() <= prefixLen + 4) {
            return "••••••";
        }
        String prefix = keyId.substring(0, prefixLen);
        String last4 = keyId.substring(keyId.length() - 4);
        return prefix + "••••••" + last4;
    }
}
