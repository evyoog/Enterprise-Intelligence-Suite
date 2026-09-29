package com.vyoog.eisplatform.modules.administration.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.administration.dto.*;
import com.vyoog.eisplatform.modules.administration.model.PlatformCurrency;
import com.vyoog.eisplatform.modules.administration.model.PlatformFeatureFlag;
import com.vyoog.eisplatform.modules.administration.model.PlatformRegion;
import com.vyoog.eisplatform.modules.administration.repository.PlatformCurrencyRepository;
import com.vyoog.eisplatform.modules.administration.repository.PlatformFeatureFlagRepository;
import com.vyoog.eisplatform.modules.administration.repository.PlatformRegionRepository;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 15.01 Platform Administration (sprint 2026.4.2): currencies, regions and
 * feature flags. Every write here is ADMIN-only (MANAGE_PLATFORM_SETTINGS,
 * enforced by SecurityConfig, not this class — same split as every other
 * admin service in this codebase).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformAdministrationService {

    private final PlatformCurrencyRepository currencyRepository;
    private final PlatformRegionRepository regionRepository;
    private final PlatformFeatureFlagRepository flagRepository;
    private final AuditService auditService;
    private final List<PlatformRegionUsageGuard> regionUsageGuards;

    private static PlatformCurrencyDto toDto(PlatformCurrency c) {
        return new PlatformCurrencyDto(c.getCode(), c.getName(), c.isEnabled());
    }

    private static PlatformRegionDto toDto(PlatformRegion r) {
        return new PlatformRegionDto(r.getId(), r.getCode(), r.getName(), r.isEnabled());
    }

    private static PlatformFeatureFlagDto toDto(PlatformFeatureFlag f) {
        return new PlatformFeatureFlagDto(f.getFlagKey(), f.isEnabled(), f.getDescription());
    }

    // ------------------------------------------------------------------
    // Currencies — seeded from Currency (see PlatformAdministrationSeeder);
    // enable/disable only, no create/delete (the underlying enum is fixed).
    // ------------------------------------------------------------------

    public List<PlatformCurrencyDto> listCurrencies() {
        return currencyRepository.findAll().stream().sorted(Comparator.comparing(PlatformCurrency::getCode))
            .map(PlatformAdministrationService::toDto).toList();
    }

    @Transactional
    public PlatformCurrencyDto updateCurrency(String code, boolean enabled, String actorKeycloakSub) {
        PlatformCurrency currency = currencyRepository.findById(code)
            .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + code));
        currency.setEnabled(enabled);
        currency = currencyRepository.save(currency);
        auditService.recordSuccess("PLATFORM_CURRENCY_UPDATED", actorKeycloakSub, null, null,
            "PlatformCurrency", code, null, "Set enabled=" + enabled + " for currency " + code);
        return toDto(currency);
    }

    // ------------------------------------------------------------------
    // Regions — freely admin-defined (05.02.01.03 Assign region reads this list).
    // ------------------------------------------------------------------

    public List<PlatformRegionDto> listRegions() {
        return regionRepository.findAll().stream().sorted(Comparator.comparing(PlatformRegion::getName))
            .map(PlatformAdministrationService::toDto).toList();
    }

    @Transactional
    public PlatformRegionDto createRegion(CreateRegionRequest request, String actorKeycloakSub) {
        if (regionRepository.existsByCode(request.code().trim())) {
            throw new DuplicateResourceException("A region with this code already exists.");
        }
        PlatformRegion region = new PlatformRegion();
        region.setCode(request.code().trim());
        region.setName(request.name().trim());
        region = regionRepository.save(region);
        auditService.recordSuccess("PLATFORM_REGION_CREATED", actorKeycloakSub, null, null,
            "PlatformRegion", region.getId().toString(), null, "Created region " + region.getCode());
        return toDto(region);
    }

    @Transactional
    public PlatformRegionDto updateRegion(Long id, UpdateRegionRequest request, String actorKeycloakSub) {
        PlatformRegion region = regionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Region not found: " + id));
        region.setName(request.name().trim());
        region.setEnabled(request.enabled());
        region = regionRepository.save(region);
        auditService.recordSuccess("PLATFORM_REGION_UPDATED", actorKeycloakSub, null, null,
            "PlatformRegion", id.toString(), null, "Updated region " + region.getCode());
        return toDto(region);
    }

    /** Regions referenced by an organization block deletion — see
     * {@code OrganizationRegionUsageGuard} in the registration module,
     * collected here the same way ProductUsageGuard beans are. */
    @Transactional
    public void deleteRegion(Long id, String actorKeycloakSub) {
        PlatformRegion region = regionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Region not found: " + id));
        var reasons = regionUsageGuards.stream().map(g -> g.blockingReason(id)).flatMap(java.util.Optional::stream).toList();
        if (!reasons.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete this region: " + String.join(", ", reasons) + ".");
        }
        regionRepository.delete(region);
        auditService.recordSuccess("PLATFORM_REGION_DELETED", actorKeycloakSub, null, null,
            "PlatformRegion", id.toString(), null, "Deleted region " + region.getCode());
    }

    // ------------------------------------------------------------------
    // Feature flags
    // ------------------------------------------------------------------

    public List<PlatformFeatureFlagDto> listFeatureFlags() {
        return flagRepository.findAll().stream().sorted(Comparator.comparing(PlatformFeatureFlag::getFlagKey))
            .map(PlatformAdministrationService::toDto).toList();
    }

    @Transactional
    public PlatformFeatureFlagDto createFeatureFlag(CreateFeatureFlagRequest request, String actorKeycloakSub) {
        if (flagRepository.existsById(request.flagKey().trim())) {
            throw new DuplicateResourceException("A feature flag with this key already exists.");
        }
        PlatformFeatureFlag flag = new PlatformFeatureFlag();
        flag.setFlagKey(request.flagKey().trim());
        flag.setEnabled(request.enabled());
        flag.setDescription(request.description());
        flag = flagRepository.save(flag);
        auditService.recordSuccess("PLATFORM_FEATURE_FLAG_CREATED", actorKeycloakSub, null, null,
            "PlatformFeatureFlag", flag.getFlagKey(), null, "Created feature flag " + flag.getFlagKey());
        return toDto(flag);
    }

    @Transactional
    public PlatformFeatureFlagDto updateFeatureFlag(String flagKey, UpdateFeatureFlagRequest request, String actorKeycloakSub) {
        PlatformFeatureFlag flag = flagRepository.findById(flagKey)
            .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found: " + flagKey));
        flag.setEnabled(request.enabled());
        flag.setDescription(request.description());
        flag = flagRepository.save(flag);
        auditService.recordSuccess("PLATFORM_FEATURE_FLAG_UPDATED", actorKeycloakSub, null, null,
            "PlatformFeatureFlag", flagKey, null, "Set enabled=" + request.enabled() + " for flag " + flagKey);
        return toDto(flag);
    }

    @Transactional
    public void deleteFeatureFlag(String flagKey, String actorKeycloakSub) {
        if (!flagRepository.existsById(flagKey)) {
            throw new ResourceNotFoundException("Feature flag not found: " + flagKey);
        }
        flagRepository.deleteById(flagKey);
        auditService.recordSuccess("PLATFORM_FEATURE_FLAG_DELETED", actorKeycloakSub, null, null,
            "PlatformFeatureFlag", flagKey, null, "Deleted feature flag " + flagKey);
    }

    // ------------------------------------------------------------------
    // Languages — read-only, see SupportedLanguageDto's own javadoc.
    // ------------------------------------------------------------------

    public List<SupportedLanguageDto> listSupportedLanguages() {
        return List.of(new SupportedLanguageDto("en", "English"), new SupportedLanguageDto("es", "Español"));
    }
}
