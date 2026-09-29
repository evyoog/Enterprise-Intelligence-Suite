package com.vyoog.eisplatform.modules.administration.controller;

import com.vyoog.eisplatform.modules.administration.dto.*;
import com.vyoog.eisplatform.modules.administration.service.PlatformAdministrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 15.01 Platform Administration (sprint 2026.4.2) — ADMIN-only everywhere,
 * enforced by SecurityConfig (MANAGE_PLATFORM_SETTINGS), not here.
 */
@RestController
@RequestMapping("/admin/platform-settings")
@RequiredArgsConstructor
public class PlatformAdministrationController {

    private final PlatformAdministrationService service;

    @GetMapping("/currencies")
    public List<PlatformCurrencyDto> listCurrencies() {
        return service.listCurrencies();
    }

    @PatchMapping("/currencies/{code}")
    public PlatformCurrencyDto updateCurrency(
            @AuthenticationPrincipal Jwt jwt, @PathVariable String code, @Valid @RequestBody UpdateCurrencyRequest request) {
        return service.updateCurrency(code, request.enabled(), jwt.getSubject());
    }

    @GetMapping("/regions")
    public List<PlatformRegionDto> listRegions() {
        return service.listRegions();
    }

    @PostMapping("/regions")
    public ResponseEntity<PlatformRegionDto> createRegion(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateRegionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRegion(request, jwt.getSubject()));
    }

    @PutMapping("/regions/{id}")
    public PlatformRegionDto updateRegion(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody UpdateRegionRequest request) {
        return service.updateRegion(id, request, jwt.getSubject());
    }

    @DeleteMapping("/regions/{id}")
    public ResponseEntity<Void> deleteRegion(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.deleteRegion(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feature-flags")
    public List<PlatformFeatureFlagDto> listFeatureFlags() {
        return service.listFeatureFlags();
    }

    @PostMapping("/feature-flags")
    public ResponseEntity<PlatformFeatureFlagDto> createFeatureFlag(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateFeatureFlagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFeatureFlag(request, jwt.getSubject()));
    }

    @PatchMapping("/feature-flags/{flagKey}")
    public PlatformFeatureFlagDto updateFeatureFlag(
            @AuthenticationPrincipal Jwt jwt, @PathVariable String flagKey, @Valid @RequestBody UpdateFeatureFlagRequest request) {
        return service.updateFeatureFlag(flagKey, request, jwt.getSubject());
    }

    @DeleteMapping("/feature-flags/{flagKey}")
    public ResponseEntity<Void> deleteFeatureFlag(@AuthenticationPrincipal Jwt jwt, @PathVariable String flagKey) {
        service.deleteFeatureFlag(flagKey, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/languages")
    public List<SupportedLanguageDto> listSupportedLanguages() {
        return service.listSupportedLanguages();
    }
}
