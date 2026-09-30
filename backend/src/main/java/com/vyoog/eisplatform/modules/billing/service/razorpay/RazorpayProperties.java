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

    public RazorpayProperties(
            @Value("${vyoog.billing.razorpay.key-id:}") String keyId,
            @Value("${vyoog.billing.razorpay.key-secret:}") String keySecret,
            @Value("${vyoog.billing.razorpay.webhook-secret:}") String webhookSecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    /** BR-10: the platform runs in "Payment gateway not configured" mode
     * unless every credential is present. */
    public boolean isConfigured() {
        return !keyId.isBlank() && !keySecret.isBlank() && !webhookSecret.isBlank();
    }

    /** Derived from the key ID prefix, not a separate setting — Razorpay's
     * own convention (REQ-BIL-001.13). */
    public boolean isLiveMode() {
        return keyId.startsWith("rzp_live_");
    }

    /** For example {@code rzp_live_••••••1234} — the {@code rzp_live_}/
     * {@code rzp_test_} prefix and last 4 characters are kept, the rest
     * masked (REQ-BIL-001.13: "the key ID masked"). */
    public String maskedKeyId() {
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
