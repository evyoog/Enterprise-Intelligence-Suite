package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.modules.billing.tax.calc.BlockCode;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationRequest;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxCalculationResult;
import com.vyoog.eisplatform.modules.billing.tax.calc.TaxModeSnapshot;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.RuleApprovalStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.SupplyType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxEngineType;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxCalculationStatus;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxExemption;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxIdCondition;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRule;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxRuleComponent;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxTreatment;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxExemptionRepository;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxRuleRepository;
import com.vyoog.eisplatform.modules.billing.tax.support.CountryResolver;
import com.vyoog.eisplatform.modules.billing.tax.support.StateCatalog;
import com.vyoog.eisplatform.modules.billing.tax.support.TaxIdValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** REQ-BIL-002.10 to .20: the EIS internal engine. Uses only rules that are approved, active, in force on the calculation date
 * and matching the transaction (BR-11, BR-12); picks the most specific one (BR-15); blocks when none matches or when two tie
 * (BR-18). No rate is written here (BR-10). */
@Service
@RequiredArgsConstructor
public class InternalTaxEngine {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TaxRuleRepository ruleRepository;
    private final TaxExemptionRepository exemptionRepository;

    public TaxCalculationResult calculate(TaxCalculationRequest request, TaxModeSnapshot mode) {
        TaxCalculationRequest.Customer customer = request.customer();
        TaxCalculationRequest.Supplier supplier = request.supplier();
        if (customer.countryCode() == null) {
            return TaxCalculationResult.blocked(TaxEngineType.INTERNAL, mode.configVersion(), BlockCode.MISSING_CUSTOMER_COUNTRY,
                "The customer's billing country" + (customer.rawCountry() == null || customer.rawCountry().isBlank() ? " is missing."
                    : " \"" + customer.rawCountry() + "\" is not recognised."), null, 1);
        }
        if (supplier.countryCode() == null) {
            return TaxCalculationResult.blocked(TaxEngineType.INTERNAL, mode.configVersion(), BlockCode.MISSING_SUPPLIER_PROFILE, null, null, 1);
        }

        // BR-13/BR-14: a tax ID counts as provided only when it is acceptable (an Indian one must be a valid GSTIN).
        boolean taxIdProvided = customer.hasTaxId() && TaxIdValidator.check(customer.countryCode(), customer.taxId()).isEmpty();
        final String placeStateKey = "IN".equals(customer.countryCode()) && taxIdProvided
            ? TaxIdValidator.gstinState(customer.taxId()).map(StateCatalog.State::code).orElse(customer.stateKey())
            : customer.stateKey();
        SupplyType supply = supplyType(supplier, customer, placeStateKey);
        String placeOfSupply = StateCatalog.hasCatalog(customer.countryCode()) && placeStateKey != null
            ? StateCatalog.displayName(customer.countryCode(), placeStateKey) + ", " + CountryResolver.nameOf(customer.countryCode())
            : CountryResolver.nameOf(customer.countryCode());

        // BR-19: an approved exemption in force makes the result an explicit Exempt.
        TaxExemption exemption = findExemption(customer, request);
        if (exemption != null) {
            List<TaxCalculationResult.LineResult> lines = new ArrayList<>();
            for (TaxCalculationRequest.Line line : request.lines()) {
                lines.add(new TaxCalculationResult.LineResult(line.lineRef(), line.amount(), List.of(new TaxCalculationResult.Component(
                    "EXEMPT", "Exempt", null, null, TaxTreatment.EXEMPT, BigDecimal.ZERO, line.amount(), 0, null, null,
                    "Exemption: " + exemption.getReason() + (exemption.getCertificateNumber() == null ? "" : " (certificate " + exemption.getCertificateNumber() + ")")))));
            }
            return new TaxCalculationResult(TaxCalculationStatus.CALCULATED, TaxEngineType.INTERNAL, mode.configVersion(), null, null,
                placeOfSupply, null, null, 1, lines, null);
        }

        List<TaxRule> inForce = ruleRepository.findInForce(customer.countryCode(), RuleApprovalStatus.APPROVED, request.date());
        List<TaxCalculationResult.LineResult> results = new ArrayList<>();
        for (TaxCalculationRequest.Line line : request.lines()) {
            List<TaxRule> matches = latestVersions(inForce.stream()
                .filter(r -> matches(r, line, customer, placeStateKey, supply, taxIdProvided)).toList());
            if (matches.isEmpty()) {
                return TaxCalculationResult.blocked(TaxEngineType.INTERNAL, mode.configVersion(), BlockCode.NO_MATCHING_RULE,
                    noMatchMessage(request, customer, line, supply, placeStateKey), null, 1);
            }
            int best = matches.stream().mapToInt(TaxRule::specificity).max().orElse(0);
            List<TaxRule> top = matches.stream().filter(r -> r.specificity() == best).toList();
            if (top.size() > 1) {
                return TaxCalculationResult.blocked(TaxEngineType.INTERNAL, mode.configVersion(), BlockCode.AMBIGUOUS_RULES,
                    "These rules match equally well: " + top.stream().map(r -> r.getName() + " (v" + r.getVersion() + ")").toList() + ". Deactivate or change one of them.", null, 1);
            }
            results.add(applyRule(top.get(0), line));
        }
        return new TaxCalculationResult(TaxCalculationStatus.CALCULATED, TaxEngineType.INTERNAL, mode.configVersion(), null, null,
            placeOfSupply, null, null, 1, results, null);
    }

    /** BR-14. Null = cannot be decided (a state is missing). */
    private SupplyType supplyType(TaxCalculationRequest.Supplier supplier, TaxCalculationRequest.Customer customer, String placeStateKey) {
        if (!Objects.equals(customer.countryCode(), supplier.countryCode())) {
            return SupplyType.CROSS_BORDER;
        }
        if (placeStateKey == null || supplier.stateKey() == null) {
            return null;
        }
        return placeStateKey.equals(supplier.stateKey()) ? SupplyType.INTRA_STATE : SupplyType.INTER_STATE;
    }

    private boolean matches(TaxRule r, TaxCalculationRequest.Line line, TaxCalculationRequest.Customer customer, String placeStateKey,
                            SupplyType supply, boolean taxIdProvided) {
        if (r.getStateCode() != null && !r.getStateCode().equals(placeStateKey)) {
            return false;
        }
        if (r.getProductClassCode() != null && !r.getProductClassCode().equals(line.taxClassCode())) {
            return false;
        }
        if (r.getCustomerType() != CustomerTypeCondition.ANY && r.getCustomerType() != customer.kind()) {
            return false;
        }
        if (r.getTaxIdCondition() == TaxIdCondition.PROVIDED && !taxIdProvided) {
            return false;
        }
        if (r.getTaxIdCondition() == TaxIdCondition.NOT_PROVIDED && taxIdProvided) {
            return false;
        }
        return r.getSupplyType() == SupplyType.ANY || r.getSupplyType() == supply;
    }

    /** Two versions of one family are never both in force, but if they are, only the newest version counts. */
    private List<TaxRule> latestVersions(List<TaxRule> rules) {
        Map<String, TaxRule> newest = new HashMap<>();
        for (TaxRule r : rules) {
            newest.merge(r.getRuleKey(), r, (a, b) -> a.getVersion() >= b.getVersion() ? a : b);
        }
        return newest.values().stream().sorted(Comparator.comparing(TaxRule::getId)).toList();
    }

    private TaxCalculationResult.LineResult applyRule(TaxRule rule, TaxCalculationRequest.Line line) {
        List<TaxCalculationResult.Component> components = new ArrayList<>();
        if (rule.getTreatment() != TaxTreatment.TAXABLE) {
            components.add(new TaxCalculationResult.Component(rule.getTreatment().name(), treatmentLabel(rule.getTreatment()), rule.getTaxType(),
                rule.getJurisdiction(), rule.getTreatment(), BigDecimal.ZERO, line.amount(), 0, rule.getId(), rule.getVersion(), rule.getName()));
            return new TaxCalculationResult.LineResult(line.lineRef(), line.amount(), components);
        }
        long running = 0;
        for (TaxRuleComponent c : rule.orderedComponents()) {
            long base = c.isCompound() ? line.amount() + running : line.amount();
            long amount = percentOf(base, c.getRate());
            running += amount;
            components.add(new TaxCalculationResult.Component(c.getCode(), c.getName(), rule.getTaxType(), rule.getJurisdiction(), TaxTreatment.TAXABLE,
                c.getRate(), base, amount, rule.getId(), rule.getVersion(), null));
        }
        return new TaxCalculationResult.LineResult(line.lineRef(), line.amount(), components);
    }

    /** BR-17: half up, to the smallest currency unit. */
    static long percentOf(long base, BigDecimal ratePercent) {
        return BigDecimal.valueOf(base).multiply(ratePercent).divide(HUNDRED, 0, RoundingMode.HALF_UP).longValueExact();
    }

    private TaxExemption findExemption(TaxCalculationRequest.Customer customer, TaxCalculationRequest request) {
        List<TaxExemption> candidates = customer.ownerOrganizationId() != null
            ? exemptionRepository.findByOwnerOrganizationIdAndCountryCode(customer.ownerOrganizationId(), customer.countryCode())
            : customer.ownerCustomerId() != null
                ? exemptionRepository.findByOwnerCustomerIdAndCountryCode(customer.ownerCustomerId(), customer.countryCode()) : List.of();
        return candidates.stream().filter(e -> e.isInForce(request.date())).findFirst().orElse(null);
    }

    private static String treatmentLabel(TaxTreatment t) {
        return switch (t) {
            case EXEMPT -> "Exempt";
            case ZERO_RATED -> "Zero-rated";
            case NIL_RATED -> "Nil-rated";
            case REVERSE_CHARGE -> "Reverse charge";
            case OUT_OF_SCOPE -> "Out of scope";
            case TAXABLE -> "Taxable";
        };
    }

    private String noMatchMessage(TaxCalculationRequest request, TaxCalculationRequest.Customer customer, TaxCalculationRequest.Line line,
                                  SupplyType supply, String placeStateKey) {
        StringBuilder sb = new StringBuilder("No approved, active tax rule in force on ").append(request.date()).append(" matches: country ")
            .append(customer.countryCode());
        if (placeStateKey != null) {
            sb.append(", state ").append(StateCatalog.displayName(customer.countryCode(), placeStateKey));
        }
        sb.append(", ").append(line.taxClassCode() == null ? "no tax class" : "tax class " + line.taxClassCode())
            .append(", ").append(customer.kind() == CustomerTypeCondition.BUSINESS ? "business" : "individual").append(" customer");
        if (supply != null) {
            sb.append(", supply ").append(supply.name().toLowerCase().replace('_', ' '));
        } else {
            sb.append(". The place of supply could not be decided because the customer's or the seller's state is missing");
        }
        return sb.append('.').toString();
    }
}
