package com.vyoog.eisplatform.modules.billing.service.gateway;

import java.util.Map;

/** What an adapter is given for one call: the provider code, the environment (TEST or LIVE, may be null for a legacy configuration) and the resolved
 * credentials (stored values over the environment's). Never logged; never leaves the backend. */
public record GatewayContext(String code, String environment, Map<String, String> credentials) {
    public String credential(String key) {
        String v = credentials.get(key);
        return v == null ? "" : v;
    }

    @Override
    public String toString() {
        return "GatewayContext[" + code + ", " + environment + "]";
    }
}
