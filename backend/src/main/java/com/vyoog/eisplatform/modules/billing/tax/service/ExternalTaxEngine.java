package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.tax.calc.BlockCode;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxModeSnapshot;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxCalculationStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxType;
import com.vyoog.eisplatform.modules.billing.tax.model.ValidationStatus;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderContext;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderException;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderTaxRequest;
import com.vyoog.eisplatform.modules.billing.tax.provider.ProviderTaxResponse;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.provider.TaxProviderRegistry;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import com.vyoog.eisplatform.modules.billing.tax.support.StateCatalog;
import com.vyoog.eisplatform.modules.billing.tax.support.TaxIdValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** REQ-BIL-002.30 to .36: the external engine. It knows only {@link TaxProvider}. Order of work: check the provider is ready
 * and supports the transaction, send it (retrying only transient failures with the same idempotency key), validate the answer,
 * return it. It never falls back to the internal engine (BR-3): anything that fails returns a BLOCKED result. */
@Service
@RequiredArgsConstructor
public class ExternalTaxEngine {

    private static final Logger log = LoggerFactory.getLogger(ExternalTaxEngine.class);
    private static final long BACKOFF_BASE_MILLIS = 250;

    private final TaxProviderRegistry registry;
    private final TaxRetrySleeper sleeper;

    public TaxCalculationResult calculate(TaxCalculationRequest request, TaxModeSnapshot mode) {
        TaxEngineType engine = TaxEngineType.EXTERNAL;
        TaxProvider provider = registry.find(mode.providerCode()).orElse(null);
        if (provider == null || !provider.credentialsPresent()) {
            return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.PROVIDER_NOT_CONFIGURED, null, mode.providerCode(), 0);
        }
        if (mode.validationStatus() != ValidationStatus.PASSED) {
            return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.PROVIDER_NOT_VALIDATED, null, provider.code(), 0);
        }
        TaxCalculationRequest.Customer customer = request.customer();
        if (customer.countryCode() == null) {
            return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.MISSING_CUSTOMER_COUNTRY, null, provider.code(), 0);
        }
        if (request.supplier().countryCode() == null) {
            return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.MISSING_SUPPLIER_PROFILE, null, provider.code(), 0);
        }
        // BR-31: the provider must support the country, the currency and the transaction type.
        if (!mode.providerCountries().contains(customer.countryCode()) || !mode.providerCurrencies().contains(request.currency())
                || !mode.providerTransactionTypes().contains(request.type().name())) {
            return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.PROVIDER_UNSUPPORTED,
                "The provider does not support " + request.type() + " in " + request.currency() + " for " + customer.countryCode() + ".", provider.code(), 0);
        }
        for (TaxCalculationRequest.Line line : request.lines()) {
            if (line.taxClassCode() == null || line.taxClassCode().isBlank()) {
                return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.MISSING_TAX_CLASS,
                    "\"" + line.description() + "\" has no tax class. Set it under Tax Configuration, Product classification.", provider.code(), 0);
            }
        }

        ProviderTaxRequest providerRequest = toProviderRequest(request, mode);
        ProviderContext context = new ProviderContext(mode.providerCompanyCode(), mode.providerEnvironment(), Duration.ofSeconds(mode.timeoutSeconds()));
        int attempts = 0;
        ProviderTaxResponse response = null;
        while (response == null) {
            attempts++;
            try {
                response = provider.calculate(context, providerRequest);
            } catch (ProviderException e) {
                if (e.isTransient() && attempts < mode.maxAttempts()) {
                    log.warn("Tax provider {} attempt {} failed transiently: {}", provider.code(), attempts, e.getMessage());
                    sleeper.sleep(BACKOFF_BASE_MILLIS * (1L << (attempts - 1)));
                    continue;
                }
                BlockCode code = e.isTransient() ? BlockCode.PROVIDER_UNAVAILABLE
                    : e.isInvalidResponse() ? BlockCode.PROVIDER_INVALID_RESPONSE : BlockCode.PROVIDER_REJECTED;
                return TaxCalculationResult.blocked(engine, mode.configVersion(), code, e.getMessage() + (e.isTransient() ? " (" + attempts + " attempt(s))" : ""),
                    provider.code(), attempts);
            } catch (RuntimeException e) {
                log.error("Tax provider {} adapter failed unexpectedly", provider.code(), e);
                return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.ENGINE_ERROR, "The provider adapter failed unexpectedly.", provider.code(), attempts);
            }
        }

        List<String> problems = validate(response, providerRequest);
        if (!problems.isEmpty()) {
            return TaxCalculationResult.blocked(engine, mode.configVersion(), BlockCode.PROVIDER_INVALID_RESPONSE,
                "The provider's calculation failed validation: " + String.join("; ", problems.subList(0, Math.min(3, problems.size()))) + ".", provider.code(), attempts);
        }
        return toResult(response, request, mode, provider.code(), attempts);
    }

    /** BR-33. Returns every problem found; empty means the response can be trusted. */
    List<String> validate(ProviderTaxResponse response, ProviderTaxRequest request) {
        List<String> problems = new ArrayList<>();
        if (response.status() == null || !"SUCCESS".equalsIgnoreCase(response.status())) {
            problems.add("status is " + response.status());
        }
        if (!request.transactionRef().equals(response.transactionRef())) {
            problems.add("the transaction reference does not match");
        }
        if (response.currency() == null || !request.currency().equalsIgnoreCase(response.currency())) {
            problems.add("the currency " + response.currency() + " is not " + request.currency());
        }
        if (response.providerReference() == null || response.providerReference().isBlank()) {
            problems.add("the provider reference is missing");
        }
        Map<String, ProviderTaxRequest.Line> requested = new HashMap<>();
        for (ProviderTaxRequest.Line l : request.lines()) {
            requested.put(l.lineRef(), l);
        }
        Set<String> seen = new HashSet<>();
        long taxSum = 0;
        long taxableSum = 0;
        if (response.lines() == null) {
            problems.add("the lines are missing");
        } else {
            for (ProviderTaxResponse.LineResult line : response.lines()) {
                ProviderTaxRequest.Line sent = line.lineRef() == null ? null : requested.get(line.lineRef());
                if (sent == null) {
                    problems.add("unknown line " + line.lineRef());
                    continue;
                }
                if (!seen.add(line.lineRef())) {
                    problems.add("line " + line.lineRef() + " appears twice");
                    continue;
                }
                if (line.taxableAmount() == null || line.taxableAmount() != sent.amount()) {
                    problems.add("the taxable amount of line " + line.lineRef() + " changed");
                } else {
                    taxableSum += line.taxableAmount();
                }
                if (line.taxes() == null) {
                    problems.add("line " + line.lineRef() + " has no taxes list");
                    continue;
                }
                for (ProviderTaxResponse.Tax tax : line.taxes()) {
                    if (tax.amount() == null || tax.amount() < 0) {
                        problems.add("a tax amount on line " + line.lineRef() + " is missing or negative");
                        continue;
                    }
                    if (tax.code() == null || tax.code().isBlank()) {
                        problems.add("a tax component on line " + line.lineRef() + " has no code");
                    }
                    if (tax.rate() != null && (tax.rate().signum() < 0 || tax.rate().compareTo(BigDecimal.valueOf(100)) > 0)) {
                        problems.add("a rate on line " + line.lineRef() + " is outside 0 to 100");
                    }
                    taxSum += tax.amount();
                }
            }
            for (String ref : requested.keySet()) {
                if (!seen.contains(ref)) {
                    problems.add("line " + ref + " is missing");
                }
            }
        }
        if (response.totalTax() == null || response.totalTax() != taxSum) {
            problems.add("the total tax " + response.totalTax() + " is not the sum of the lines (" + taxSum + ")");
        }
        if (taxSum > request.lines().stream().mapToLong(ProviderTaxRequest.Line::amount).sum()) {
            problems.add("the tax is more than the taxable amount");
        }
        return problems;
    }

    private TaxCalculationResult toResult(ProviderTaxResponse response, TaxCalculationRequest request, TaxModeSnapshot mode, String providerCode, int attempts) {
        Map<String, ProviderTaxResponse.LineResult> byRef = new HashMap<>();
        response.lines().forEach(l -> byRef.put(l.lineRef(), l));
        List<TaxCalculationResult.LineResult> lines = new ArrayList<>();
        for (TaxCalculationRequest.Line sent : request.lines()) {
            ProviderTaxResponse.LineResult line = byRef.get(sent.lineRef());
            List<TaxCalculationResult.Component> components = new ArrayList<>();
            for (ProviderTaxResponse.Tax tax : line.taxes()) {
                components.add(new TaxCalculationResult.Component(tax.code(), tax.name() == null || tax.name().isBlank() ? tax.code() : tax.name(),
                    taxType(tax.taxType()), tax.jurisdiction(), TaxTreatment.TAXABLE, tax.rate(), sent.amount(), tax.amount(), null, null, null));
            }
            lines.add(new TaxCalculationResult.LineResult(sent.lineRef(), sent.amount(), components));
        }
        String place = StateCatalog.hasCatalog(request.customer().countryCode()) && request.customer().stateKey() != null
            ? StateCatalog.displayName(request.customer().countryCode(), request.customer().stateKey()) + ", " + CountryResolver.nameOf(request.customer().countryCode())
            : CountryResolver.nameOf(request.customer().countryCode());
        return new TaxCalculationResult(TaxCalculationStatus.CALCULATED, TaxEngineType.EXTERNAL, mode.configVersion(), null, null, place, providerCode,
            response.providerReference(), attempts, lines, response.rawCopy());
    }

    private ProviderTaxRequest toProviderRequest(TaxCalculationRequest request, TaxModeSnapshot mode) {
        TaxCalculationRequest.Supplier s = request.supplier();
        List<ProviderTaxRequest.TaxIdentifier> supplierIds = new ArrayList<>();
        if (s.gstin() != null && !s.gstin().isBlank()) {
            supplierIds.add(new ProviderTaxRequest.TaxIdentifier("GST", s.gstin(), "IN", s.stateKey()));
        }
        for (TaxCalculationRequest.Registration r : s.registrations()) {
            supplierIds.add(new ProviderTaxRequest.TaxIdentifier(r.taxType().name(), r.number(), r.countryCode(), r.stateKey()));
        }
        TaxCalculationRequest.Customer c = request.customer();
        List<ProviderTaxRequest.TaxIdentifier> customerIds = new ArrayList<>();
        // An Indian tax ID that is not a valid GSTIN is not sent as a tax ID (it would be rejected or mis-taxed).
        if (c.hasTaxId() && TaxIdValidator.check(c.countryCode(), c.taxId()).isEmpty()) {
            customerIds.add(new ProviderTaxRequest.TaxIdentifier("IN".equals(c.countryCode()) ? "GST" : "OTHER", c.taxId().trim(), c.countryCode(), c.stateKey()));
        }
        List<ProviderTaxRequest.Line> lines = request.lines().stream().map(l -> new ProviderTaxRequest.Line(l.lineRef(), l.description(),
            l.taxClassCode(), l.hsnSacCode(), l.amount(), l.quantity())).toList();
        return new ProviderTaxRequest(request.transactionRef(), request.type().name(), mode.providerCompanyCode(), request.date(), request.currency(),
            new ProviderTaxRequest.Party(s.legalName(), s.addressLine1(), s.city(), s.stateName(), s.postalCode(), s.countryCode(), supplierIds),
            new ProviderTaxRequest.Party(c.name(), c.addressLine1(), c.city(), c.rawState(), c.postalCode(), c.countryCode(), customerIds), lines);
    }

    private static TaxType taxType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return TaxType.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return TaxType.OTHER;
        }
    }
}
