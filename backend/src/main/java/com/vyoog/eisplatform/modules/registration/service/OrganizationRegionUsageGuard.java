package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.modules.administration.service.PlatformRegionUsageGuard;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Blocks deleting a region that is still assigned to an organization
 * (05.02.01.03 Assign region, sprint 2026.4.2) — same pattern as
 * {@code CatalogProductUsageGuard} for products. */
@Component
@RequiredArgsConstructor
public class OrganizationRegionUsageGuard implements PlatformRegionUsageGuard {

    private final OrganizationRepository organizationRepository;

    @Override
    public Optional<String> blockingReason(Long regionId) {
        return organizationRepository.existsByRegionId(regionId)
            ? Optional.of("an organization is assigned to it")
            : Optional.empty();
    }
}
