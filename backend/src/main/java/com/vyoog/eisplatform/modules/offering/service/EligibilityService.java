package com.vyoog.eisplatform.modules.offering.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.offering.dto.OfferingDtos.*;
import com.vyoog.eisplatform.modules.offering.model.*;
import com.vyoog.eisplatform.modules.offering.repository.*;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.product.service.ProductDeleteListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REQ-CAT-005: who may buy a product (audience), and which products it works with. A product with no
 * rule is open to everyone, so existing behavior does not change. "Required product" is the existing
 * product dependency (OF-6); there is no regional rule (OF-3).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EligibilityService implements ProductDeleteListener {

    private final ProductEligibilityRepository eligibilityRepository;
    private final ProductCompatibilityRepository compatibilityRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public ProductAudience audienceOf(Long productId) {
        return eligibilityRepository.findById(productId).map(ProductEligibility::getAudience).orElse(ProductAudience.BOTH);
    }

    /** BR-OFR-004: may a buyer of this kind (an organization member, or an individual) buy the product? */
    public boolean isEligible(Long productId, boolean buyerIsOrganization) {
        ProductAudience audience = audienceOf(productId);
        return audience == ProductAudience.BOTH
            || (audience == ProductAudience.ORGANIZATION) == buyerIsOrganization;
    }

    public void assertEligible(Long productId, boolean buyerIsOrganization) {
        if (!isEligible(productId, buyerIsOrganization)) {
            String name = productRepository.findById(productId).map(Product::getName).orElse("This product");
            throw new InvalidStateException(name + " is available to "
                + (buyerIsOrganization ? "individuals" : "organizations") + " only.");
        }
    }

    /** Public: the active products this one works with. */
    public List<WorksWithDto> worksWith(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        return compatibilityRepository.findByProductId(productId).stream()
            .map(c -> productRepository.findById(c.getWorksWithProductId()).orElse(null))
            .filter(p -> p != null && p.getStatus() == ProductStatus.ACTIVE)
            .map(p -> new WorksWithDto(p.getId(), p.getName()))
            .sorted(Comparator.comparing(WorksWithDto::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public List<ProductRuleDto> rules() {
        Map<Long, List<Long>> worksWith = compatibilityRepository.findAll().stream()
            .collect(Collectors.groupingBy(ProductCompatibility::getProductId,
                Collectors.mapping(ProductCompatibility::getWorksWithProductId, Collectors.toList())));
        return productRepository.findAll().stream()
            .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
            .map(p -> new ProductRuleDto(p.getId(), p.getName(), p.getStatus().name(), audienceOf(p.getId()).name(),
                worksWith.getOrDefault(p.getId(), List.of()).stream().sorted().toList())).toList();
    }

    @Transactional
    public ProductRuleDto setRules(String actorSub, String actorEmail, Long productId, ProductRuleRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        ProductAudience audience = parseAudience(request.audience());
        Set<Long> ids = new LinkedHashSet<>(request.worksWithProductIds() == null ? List.of() : request.worksWithProductIds());
        if (ids.contains(productId)) {
            throw new IllegalArgumentException("A product cannot work with itself.");
        }
        if (!ids.isEmpty() && productRepository.findAllById(ids).size() != ids.size()) {
            throw new IllegalArgumentException("One of the products does not exist.");
        }
        if (audience == ProductAudience.BOTH) {
            eligibilityRepository.deleteById(productId);
        } else {
            ProductEligibility e = eligibilityRepository.findById(productId).orElseGet(ProductEligibility::new);
            e.setProductId(productId);
            e.setAudience(audience);
            eligibilityRepository.save(e);
        }
        compatibilityRepository.deleteByProductId(productId);
        compatibilityRepository.flush();
        for (Long id : ids) {
            ProductCompatibility c = new ProductCompatibility();
            c.setProductId(productId);
            c.setWorksWithProductId(id);
            compatibilityRepository.save(c);
        }
        auditService.recordSuccess("PRODUCT_RULES_CHANGED", actorSub, null, actorEmail, "Product", productId.toString(), null,
            "Audience " + audience + ", works with " + ids.size() + " product(s) for " + product.getName());
        return new ProductRuleDto(productId, product.getName(), product.getStatus().name(), audience.name(), ids.stream().sorted().toList());
    }

    /** Rows of the deleted product, and "works with" rows pointing at it, go with it. */
    @Override
    @Transactional
    public void onProductDeleted(Long productId) {
        eligibilityRepository.deleteById(productId);
        compatibilityRepository.deleteByProductId(productId);
        compatibilityRepository.deleteByWorksWithProductId(productId);
    }

    private static ProductAudience parseAudience(String value) {
        try {
            return ProductAudience.valueOf(value == null ? "BOTH" : value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Audience must be BOTH, INDIVIDUAL or ORGANIZATION.");
        }
    }
}
