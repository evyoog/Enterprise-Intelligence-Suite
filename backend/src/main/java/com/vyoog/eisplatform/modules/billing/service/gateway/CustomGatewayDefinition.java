package com.vyoog.eisplatform.modules.billing.service.gateway;

import java.util.List;

/** C99: the non-secret definition of a gateway an administrator added from the screen, stored as JSON next to its configuration. It never holds a
 * credential value; those are encrypted separately. {@code fields} says which credentials the gateway needs, so the configuration form is built
 * from it like for any provider. */
public record CustomGatewayDefinition(
    String description,
    String logo,
    List<String> countries,
    List<String> currencies,
    List<String> methods,
    List<String> environments,
    List<ConfigField> fields
) {
    public GatewayDescriptor toDescriptor(String code, String name) {
        return new GatewayDescriptor(code, name, description == null ? "" : description, logo == null || logo.isBlank() ? code.toLowerCase(java.util.Locale.ROOT) : logo,
            countries == null ? List.of() : countries, currencies == null ? List.of() : currencies, methods == null ? List.of() : methods,
            List.of("INVOICE_PAYMENT"), environments == null || environments.isEmpty() ? List.of("TEST", "LIVE") : environments,
            false, false, false, GatewayDescriptor.Implementation.CONFIGURATION_ONLY,
            fields == null ? List.of() : fields, List.of(), List.of());
    }
}
