package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.modules.product.service.ProductUsageGuard;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Blocks deleting a product that any individual or organization has ever
 * subscribed to, or that's been assigned as org member access — deleting the
 * row out from under real subscription/entitlement history would silently
 * corrupt it. */
@Component
@RequiredArgsConstructor
public class CatalogProductUsageGuard implements ProductUsageGuard {

    private final ProductSubscriptionRepository productSubscriptionRepository;
    private final OrganizationProductAccessRepository organizationProductAccessRepository;

    @Override
    public Optional<String> blockingReason(Long productId) {
        List<String> reasons = new ArrayList<>();
        if (productSubscriptionRepository.existsByProductId(productId)) {
            reasons.add("an existing subscription");
        }
        if (organizationProductAccessRepository.existsByProductId(productId)) {
            reasons.add("an existing organization access grant");
        }
        return reasons.isEmpty() ? Optional.empty() : Optional.of(String.join(" and ", reasons));
    }
}
