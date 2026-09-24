package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.dashboard.repository.FavoriteProductRepository;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.product.service.ProductUsageGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Blocks deleting a product that some customer has favorited or actually
 * launched — silently dropping those rows would erase real personalization
 * history out from under that customer, even though neither is as
 * consequential as a real subscription. */
@Component
@RequiredArgsConstructor
public class DashboardProductUsageGuard implements ProductUsageGuard {

    private final FavoriteProductRepository favoriteProductRepository;
    private final ProductUsageRepository productUsageRepository;

    @Override
    public Optional<String> blockingReason(Long productId) {
        List<String> reasons = new ArrayList<>();
        if (favoriteProductRepository.existsByProductId(productId)) {
            reasons.add("a customer favorite");
        }
        if (productUsageRepository.existsByProductId(productId)) {
            reasons.add("recorded launch history");
        }
        return reasons.isEmpty() ? Optional.empty() : Optional.of(String.join(" and ", reasons));
    }
}
