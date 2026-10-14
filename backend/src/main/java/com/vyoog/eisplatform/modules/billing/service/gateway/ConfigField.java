package com.vyoog.eisplatform.modules.billing.service.gateway;

/** One configuration field a provider needs (C98). {@code secret} fields are write-only: the API never returns their value.
 * {@code webhook} marks the fields used to verify that provider's webhooks. */
public record ConfigField(String key, String label, boolean secret, boolean required, boolean webhook, String help, String placeholder) {

    public static ConfigField of(String key, String label, boolean secret, boolean required, String help) {
        return new ConfigField(key, label, secret, required, false, help, null);
    }

    public static ConfigField webhookSecret(String key, String label, boolean required, String help) {
        return new ConfigField(key, label, true, required, true, help, null);
    }
}
