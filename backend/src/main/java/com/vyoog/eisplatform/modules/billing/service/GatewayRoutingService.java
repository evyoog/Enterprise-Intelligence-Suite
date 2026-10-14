package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.PaymentGatewayNotConfiguredException;
import com.vyoog.eisplatform.modules.billing.model.PaymentGatewayConfig;
import com.vyoog.eisplatform.modules.billing.repository.PaymentGatewayConfigRepository;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayContext;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayDescriptor;
import com.vyoog.eisplatform.modules.billing.service.gateway.GatewayRegistry;
import com.vyoog.eisplatform.modules.billing.service.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * C98 (REQ-BIL-003.12, .13): the backend chooses the gateway for every new payment, never the browser. A gateway is eligible when it is
 * implemented, active, fully configured, supports the invoice currency and, if one is named, the payment method (and that method is switched on
 * for it). The default gateway goes first, then the others in code order. There is no automatic fail-over to another gateway after a failure.
 * A payment that already exists is handled by the provider that created it ({@link #forProvider}), whatever is active now.
 */
@Service
@RequiredArgsConstructor
public class GatewayRoutingService {

    /** The gateway chosen for a payment: its adapter and the credentials in force. */
    public record Route(PaymentGateway adapter, GatewayContext context) {
        public String code() {
            return adapter.code();
        }
    }

    private final GatewayRegistry registry;
    private final GatewayManagementService management;
    private final PaymentGatewayConfigRepository configRepository;

    public Optional<Route> find(String currency, String method) {
        return eligible(currency, method).stream().findFirst();
    }

    /** @throws PaymentGatewayNotConfiguredException with a clear message when no gateway can take this payment. */
    public Route select(String currency, String method) {
        return find(currency, method).orElseThrow(() -> new PaymentGatewayNotConfiguredException(
            "No payment gateway is available for " + currency + (method == null ? "" : " " + method) + " payments."));
    }

    /** True when at least one gateway could take a payment in this currency. */
    public boolean available(String currency) {
        return find(currency, null).isPresent();
    }

    /** The payment methods that some eligible gateway offers for this currency (used to decide which checkout tiles to show). */
    public Set<String> offeredMethods(String currency) {
        Set<String> methods = new LinkedHashSet<>();
        for (Route route : eligible(currency, null)) {
            methods.addAll(management.enabledMethods(route.code()));
        }
        return methods;
    }

    /** The gateway that would be used for a payment in this currency, for display. */
    public Optional<GatewayDescriptor> preferred(String currency) {
        return find(currency, null).flatMap(r -> registry.descriptor(r.code()));
    }

    /** The provider that created a payment, regardless of activation: needed to confirm, reconcile and refund it. It must still be configured. */
    public Route forProvider(String providerCode) {
        PaymentGateway adapter = registry.adapter(providerCode).orElseThrow(() -> new PaymentGatewayNotConfiguredException(
            "The payment provider " + providerCode + " is not available."));
        if (!management.configured(adapter.code())) {
            throw new PaymentGatewayNotConfiguredException();
        }
        return new Route(adapter, management.context(adapter.code()));
    }

    private List<Route> eligible(String currency, String method) {
        List<PaymentGatewayConfig> configs = configRepository.findAll();
        return registry.descriptors().stream()
            .filter(GatewayDescriptor::implemented)
            .filter(d -> management.operational(d.code()))
            .filter(d -> currency == null || d.currencies().contains(currency))
            .filter(d -> method == null || management.enabledMethods(d.code()).contains(method))
            .sorted(Comparator.comparing((GatewayDescriptor d) -> !isDefault(configs, d.code())).thenComparing(GatewayDescriptor::code))
            .map(d -> new Route(registry.adapter(d.code()).orElseThrow(), management.context(d.code())))
            .toList();
    }

    private static boolean isDefault(List<PaymentGatewayConfig> configs, String code) {
        return configs.stream().anyMatch(c -> c.getProviderCode().equals(code) && c.isDefaultGateway());
    }
}
