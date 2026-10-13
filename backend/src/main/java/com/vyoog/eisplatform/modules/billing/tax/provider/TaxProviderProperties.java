package com.vyoog.eisplatform.modules.billing.tax.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

/** REQ-BIL-002.32, BR-30: the external tax provider's credentials, read from the backend environment (BR-SEC-001,
 * config/secrets.env via vyoog.tax.provider.* in application.yml). Empty means "not configured". There is deliberately no
 * getter that a controller can reach: only a provider adapter reads these values, and screens see {@link #masked()} status. */
@Component
public class TaxProviderProperties {

    private final String baseUrl;
    private final String apiKey;
    private final String apiSecret;
    private final boolean allowInsecureHttp;

    public TaxProviderProperties(
            @Value("${vyoog.tax.provider.base-url:}") String baseUrl,
            @Value("${vyoog.tax.provider.api-key:}") String apiKey,
            @Value("${vyoog.tax.provider.api-secret:}") String apiSecret,
            @Value("${vyoog.tax.provider.allow-insecure-http:false}") boolean allowInsecureHttp) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.apiSecret = apiSecret == null ? "" : apiSecret.trim();
        this.allowInsecureHttp = allowInsecureHttp;
    }

    String baseUrl() {
        return baseUrl;
    }

    String apiKey() {
        return apiKey;
    }

    String apiSecret() {
        return apiSecret;
    }

    public boolean hasBaseUrl() {
        return !baseUrl.isBlank() && isAcceptableUrl();
    }

    public boolean hasApiKey() {
        return !apiKey.isBlank();
    }

    public boolean hasApiSecret() {
        return !apiSecret.isBlank();
    }

    public boolean isConfigured() {
        return hasBaseUrl() && hasApiKey();
    }

    /** The host only, for example {@code tax.example.com}. */
    public String masked() {
        if (!hasBaseUrl()) {
            return null;
        }
        try {
            return URI.create(baseUrl).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isAcceptableUrl() {
        try {
            URI uri = URI.create(baseUrl);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            return uri.getHost() != null && ("https".equals(scheme) || (allowInsecureHttp && "http".equals(scheme)));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
