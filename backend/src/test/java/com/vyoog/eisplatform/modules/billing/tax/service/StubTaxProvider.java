package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderCapabilities;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderContext;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderException;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderTaxRequest;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderTaxResponse;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProvider;
import org.springframework.boot.test.context.TestComponent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A provider adapter for integration tests, registered like any real adapter. By default it answers every transaction with a
 * single IGST component at a rate the TEST chooses (the platform itself contains no rate). */
@TestComponent
public class StubTaxProvider implements TaxProvider {

    public boolean configured = true;
    public boolean connectionWorks = true;
    public Set<String> countries = Set.of("IN", "US");
    public Set<String> currencies = Set.of("INR", "USD", "EUR", "GBP");
    public int ratePercent = 18;
    public final List<ProviderTaxRequest> calls = new ArrayList<>();
    public final List<ProviderException> failures = new ArrayList<>();

    public void reset() {
        configured = true;
        connectionWorks = true;
        countries = Set.of("IN", "US");
        currencies = Set.of("INR", "USD", "EUR", "GBP");
        ratePercent = 18;
        calls.clear();
        failures.clear();
    }

    @Override public String code() { return "STUB"; }
    @Override public String displayName() { return "Stub provider (tests)"; }
    @Override public boolean credentialsPresent() { return configured; }
    @Override public Map<String, Boolean> credentialStatus() { return Map.of("STUB_KEY", configured); }
    @Override public String maskedEndpoint() { return configured ? "stub.example.com" : null; }

    @Override
    public ProviderCapabilities testConnection(ProviderContext context) throws ProviderException {
        if (!connectionWorks) {
            throw ProviderException.permanent("The provider refused the credentials (HTTP 401).");
        }
        return new ProviderCapabilities("Stub", countries, currencies, Set.of("SALE"));
    }

    @Override
    public ProviderTaxResponse calculate(ProviderContext context, ProviderTaxRequest request) throws ProviderException {
        calls.add(request);
        if (!failures.isEmpty()) {
            throw failures.remove(0);
        }
        List<ProviderTaxResponse.LineResult> lines = request.lines().stream().map(l -> new ProviderTaxResponse.LineResult(l.lineRef(), l.amount(),
            List.of(new ProviderTaxResponse.Tax("IGST", "Integrated GST", "GST", "India", BigDecimal.valueOf(ratePercent), l.amount() * ratePercent / 100)))).toList();
        long total = lines.stream().flatMap(l -> l.taxes().stream()).mapToLong(ProviderTaxResponse.Tax::amount).sum();
        return new ProviderTaxResponse("SUCCESS", request.transactionRef(), "stub-" + request.transactionRef(), request.currency(), total, lines, "{\"stub\":true}");
    }
}
