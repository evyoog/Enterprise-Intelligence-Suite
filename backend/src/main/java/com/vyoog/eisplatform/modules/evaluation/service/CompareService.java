package com.vyoog.eisplatform.modules.evaluation.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.dto.BillingDetailsDto;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxPreviewRequest;
import com.vyoog.eisplatform.modules.billing.tax.model.CustomerTypeCondition;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxRuleService;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxSettingsService;
import com.vyoog.eisplatform.modules.evaluation.dto.EvaluationDtos;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.dto.ProductPlanDto;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * C102 / REQ-MKT-005 (TR-2 A; 03.02.02.01 to .03): compares 2 to 4 products, or products with chosen plans, side by side with a cost estimate of price x seats
 * plus tax. Public; the tax line needs a signed-in customer with billing details, and comes from EIS's own tax rules (with the external tax provider on, or
 * when the rules cannot tax the sale, the page says tax is worked out at checkout). Prices stay in each item's own currency.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompareService {

    public static final int MIN_ITEMS = 2;
    public static final int MAX_ITEMS = 4;
    public static final int MAX_SEATS = 100_000;

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final OrganizationMemberRepository memberRepository;
    private final BillingDetailsService billingDetailsService;
    private final TaxRuleService taxRuleService;
    private final TaxSettingsService taxSettingsService;

    /** {@code items}: {@code productId} or {@code productId:planId}, comma-separated. {@code customerId} is null when nobody is signed in. */
    public EvaluationDtos.Comparison compare(String items, Integer seats, Long customerId) {
        List<long[]> refs = parse(items);
        int seatCount = seats == null ? 1 : seats;
        if (seatCount < 1 || seatCount > MAX_SEATS) {
            throw new IllegalArgumentException("Seats must be between 1 and " + MAX_SEATS + ".");
        }
        Billing billing = billing(customerId);
        List<EvaluationDtos.CompareItem> result = new ArrayList<>();
        for (long[] ref : refs) {
            result.add(item(ref[0], ref[1] == 0 ? null : ref[1], seatCount, billing));
        }
        return new EvaluationDtos.Comparison(seatCount, result);
    }

    static List<long[]> parse(String items) {
        if (items == null || items.isBlank()) {
            throw new IllegalArgumentException("Choose " + MIN_ITEMS + " to " + MAX_ITEMS + " products or plans to compare.");
        }
        List<long[]> refs = new ArrayList<>();
        for (String part : items.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            String[] pair = p.split(":");
            try {
                long product = Long.parseLong(pair[0].trim());
                long plan = pair.length > 1 ? Long.parseLong(pair[1].trim()) : 0;
                if (pair.length > 2 || product < 1 || plan < 0) {
                    throw new NumberFormatException();
                }
                refs.add(new long[] {product, plan});
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("\"" + p + "\" is not a product or a product:plan pair.");
            }
        }
        if (refs.size() < MIN_ITEMS || refs.size() > MAX_ITEMS) {
            throw new IllegalArgumentException("Choose " + MIN_ITEMS + " to " + MAX_ITEMS + " products or plans to compare.");
        }
        for (int i = 0; i < refs.size(); i++) {
            for (int j = i + 1; j < refs.size(); j++) {
                if (refs.get(i)[0] == refs.get(j)[0] && refs.get(i)[1] == refs.get(j)[1]) {
                    throw new IllegalArgumentException("The same item is chosen twice.");
                }
            }
        }
        return refs;
    }

    private EvaluationDtos.CompareItem item(Long productId, Long planId, int seats, Billing billing) {
        ProductDto product = productService.getProduct(productId);
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        List<ProductPlanDto> plans = product.getPlans() == null ? List.of() : product.getPlans();
        ProductPlanDto plan;
        if (planId != null) {
            plan = plans.stream().filter(p -> planId.equals(p.getId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Plan " + planId + " does not belong to " + product.getName() + "."));
        } else {
            plan = plans.stream().min(Comparator.comparing(ProductPlanDto::getPrice)).orElse(null);   // a product on its own: its entry plan
        }
        BigDecimal unit = plan != null ? plan.getPrice() : product.getPrice();
        String currency = plan != null && plan.getCurrency() != null ? plan.getCurrency().name() : "USD";
        BigDecimal subtotal = unit.multiply(BigDecimal.valueOf(seats)).setScale(2, RoundingMode.HALF_UP);
        EvaluationDtos.TaxEstimate tax = tax(product, subtotal, currency, billing);
        return new EvaluationDtos.CompareItem(product, plan, currency, unit, plan == null || plan.getBillingPeriod() == null ? null : plan.getBillingPeriod().name(),
            seats, subtotal, tax, features(plan, product), product.getTrialDays());
    }

    private static List<String> features(ProductPlanDto plan, ProductDto product) {
        if (plan != null && plan.getIncludedFeatures() != null && !plan.getIncludedFeatures().isBlank()) {
            return Arrays.stream(plan.getIncludedFeatures().split("[\\r\\n,;]+")).map(String::trim).filter(f -> !f.isEmpty()).toList();
        }
        return product.getFeatureTags() == null ? List.of() : product.getFeatureTags();
    }

    // ------------------------------------------------------------------ tax

    private record Billing(String status, String country, String state, String taxId, CustomerTypeCondition type) {
    }

    private Billing billing(Long customerId) {
        if (customerId == null) {
            return new Billing("NOT_SIGNED_IN", null, null, null, null);
        }
        var member = memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE);
        try {
            BillingDetailsDto details = member.isPresent() ? billingDetailsService.getForOrganization(member.get().getOrganizationId())
                : billingDetailsService.getForCustomer(customerId);
            if (details.country() == null || details.country().isBlank()) {
                return new Billing("NEEDS_BILLING_DETAILS", null, null, null, null);
            }
            return new Billing("OK", details.country(), details.state(), details.taxId(), member.isPresent() ? CustomerTypeCondition.BUSINESS : CustomerTypeCondition.INDIVIDUAL);
        } catch (ResourceNotFoundException e) {
            return new Billing("NEEDS_BILLING_DETAILS", null, null, null, null);
        }
    }

    private EvaluationDtos.TaxEstimate tax(ProductDto product, BigDecimal subtotal, String currency, Billing billing) {
        if ("NOT_SIGNED_IN".equals(billing.status())) {
            return new EvaluationDtos.TaxEstimate("NOT_SIGNED_IN", null, null, "Sign in to see the tax estimate.");
        }
        if ("NEEDS_BILLING_DETAILS".equals(billing.status())) {
            return new EvaluationDtos.TaxEstimate("NEEDS_BILLING_DETAILS", null, null, "Add your billing details to see the tax estimate.");
        }
        if (subtotal.signum() <= 0) {
            return new EvaluationDtos.TaxEstimate("ESTIMATED", BigDecimal.ZERO.setScale(2), subtotal, null);
        }
        if (taxSettingsService.current().isExternalEngineEnabled()) {
            return new EvaluationDtos.TaxEstimate("AT_CHECKOUT", null, null, "Tax is worked out at checkout.");
        }
        long minor = subtotal.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
        String classCode = productRepository.findById(product.getId()).map(p -> p.getTaxClassCode()).orElse(null);
        TaxPreviewDto preview = taxRuleService.preview(new TaxPreviewRequest(minor, currency, billing.country(), billing.state(), classCode, billing.type(), billing.taxId(), null));
        if (!"CALCULATED".equals(preview.status())) {
            return new EvaluationDtos.TaxEstimate("AT_CHECKOUT", null, null, "Tax is worked out at checkout.");
        }
        BigDecimal tax = BigDecimal.valueOf(preview.totalTax(), 2);
        return new EvaluationDtos.TaxEstimate("ESTIMATED", tax, subtotal.add(tax), null);
    }
}
