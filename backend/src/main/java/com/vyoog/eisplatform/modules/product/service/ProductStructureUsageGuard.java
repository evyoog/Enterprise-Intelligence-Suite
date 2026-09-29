package com.vyoog.eisplatform.modules.product.service;

import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 02.01.02 Product Structure (sprint 2026.4.1): blocks deleting a product
 * that another product still lists as its parent (hierarchy) or as a
 * dependency — deleting it out from under either relationship would leave a
 * dangling reference the frontend would have to guess how to show. Unlike
 * {@link com.vyoog.eisplatform.modules.registration.service.CatalogProductUsageGuard},
 * this lives in the product module itself since both facts it checks
 * (parentProductId, dependsOn) are this module's own data.
 */
@Component
@RequiredArgsConstructor
public class ProductStructureUsageGuard implements ProductUsageGuard {

    private final ProductRepository productRepository;

    @Override
    public Optional<String> blockingReason(Long productId) {
        List<String> reasons = new ArrayList<>();
        if (productRepository.existsByParentProductId(productId)) {
            reasons.add("a product that lists it as a parent");
        }
        if (productRepository.existsAsDependencyOf(productId)) {
            reasons.add("a product that depends on it");
        }
        return reasons.isEmpty() ? Optional.empty() : Optional.of(String.join(" and ", reasons));
    }
}
