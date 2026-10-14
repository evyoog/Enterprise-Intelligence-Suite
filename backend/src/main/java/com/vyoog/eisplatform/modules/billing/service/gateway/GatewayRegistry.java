package com.vyoog.eisplatform.modules.billing.service.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.billing.model.PaymentGatewayConfig;
import com.vyoog.eisplatform.modules.billing.repository.PaymentGatewayConfigRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** C98 (REQ-BIL-003.2) and C99: every provider EIS knows about: the adapters that exist (operational) and the gateways an administrator added from
 * the screen (configuration only: credentials are stored, but there is no adapter, so they are never tested, activated or routed to). */
@Component
public class GatewayRegistry {

    private final List<PaymentGateway> adapters;
    private final PaymentGatewayConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    public GatewayRegistry(List<PaymentGateway> adapters, PaymentGatewayConfigRepository configRepository, ObjectMapper objectMapper) {
        this.adapters = adapters.stream().sorted(Comparator.comparing(PaymentGateway::code)).toList();
        this.configRepository = configRepository;
        this.objectMapper = objectMapper;
    }

    /** Adapters first, then added gateways in code order. Read from the database each time so an added gateway appears at once. */
    public List<GatewayDescriptor> descriptors() {
        List<GatewayDescriptor> all = new ArrayList<>(adapters.stream().map(PaymentGateway::descriptor).toList());
        for (PaymentGatewayConfig config : configRepository.findByCustomTrueOrderByProviderCode()) {
            if (all.stream().noneMatch(d -> d.code().equals(config.getProviderCode()))) {   // an adapter, once written, takes over its code
                custom(config).ifPresent(all::add);
            }
        }
        return List.copyOf(all);
    }

    public Optional<GatewayDescriptor> descriptor(String code) {
        String wanted = normalise(code);
        Optional<GatewayDescriptor> builtIn = adapter(wanted).map(PaymentGateway::descriptor);
        if (builtIn.isPresent()) {
            return builtIn;
        }
        return configRepository.findByProviderCode(wanted).filter(PaymentGatewayConfig::isCustom).flatMap(this::custom);
    }

    /** The working adapter for a provider, or empty when it is configuration-only or unknown. */
    public Optional<PaymentGateway> adapter(String code) {
        String wanted = normalise(code);
        return adapters.stream().filter(a -> a.code().equals(wanted)).findFirst();
    }

    /** The codes that are taken by an adapter and so cannot be reused by an added gateway. */
    public boolean isAdapterCode(String code) {
        return adapter(code).isPresent();
    }

    public static String normalise(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    private Optional<GatewayDescriptor> custom(PaymentGatewayConfig config) {
        try {
            CustomGatewayDefinition definition = objectMapper.readValue(config.getDefinition(), CustomGatewayDefinition.class);
            return Optional.of(definition.toDescriptor(config.getProviderCode(),
                config.getDisplayName() == null || config.getDisplayName().isBlank() ? config.getProviderCode() : config.getDisplayName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
