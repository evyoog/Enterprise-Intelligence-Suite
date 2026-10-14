package com.vyoog.eisplatform.modules.billing.service.gateway;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** C98 (REQ-BIL-003.2): every provider EIS knows about: the adapters that exist (operational) and the planned ones (listed only). */
@Component
public class GatewayRegistry {

    private final List<PaymentGateway> adapters;
    private final List<GatewayDescriptor> descriptors;

    public GatewayRegistry(List<PaymentGateway> adapters, PlannedProviders planned) {
        this.adapters = adapters.stream().sorted(Comparator.comparing(PaymentGateway::code)).toList();
        List<GatewayDescriptor> all = new ArrayList<>(this.adapters.stream().map(PaymentGateway::descriptor).toList());
        for (GatewayDescriptor p : planned.all()) {
            if (all.stream().noneMatch(d -> d.code().equals(p.code()))) {   // an adapter, once written, replaces its planned entry
                all.add(p);
            }
        }
        this.descriptors = List.copyOf(all);
    }

    public List<GatewayDescriptor> descriptors() {
        return descriptors;
    }

    public Optional<GatewayDescriptor> descriptor(String code) {
        String wanted = normalise(code);
        return descriptors.stream().filter(d -> d.code().equals(wanted)).findFirst();
    }

    /** The working adapter for a provider, or empty when it is only planned or unknown. */
    public Optional<PaymentGateway> adapter(String code) {
        String wanted = normalise(code);
        return adapters.stream().filter(a -> a.code().equals(wanted)).findFirst();
    }

    public static String normalise(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
