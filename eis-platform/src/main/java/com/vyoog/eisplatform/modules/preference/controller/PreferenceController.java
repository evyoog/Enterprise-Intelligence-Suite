package com.vyoog.eisplatform.modules.preference.controller;

import com.vyoog.eisplatform.modules.preference.dto.CustomerPreferenceDto;
import com.vyoog.eisplatform.modules.preference.dto.UpdateCustomerPreferenceRequest;
import com.vyoog.eisplatform.modules.preference.service.PreferenceService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 6 (2026.3.3): always the caller's own preferences, resolved from
 * their JWT the same way every other {@code /me/**} endpoint does — already
 * covered by SecurityConfig's existing {@code /me/**} authenticated() rule,
 * no new security rule needed.
 */
@RestController
@RequestMapping("/me/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public CustomerPreferenceDto get(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return preferenceService.getPreferences(customer.getId());
    }

    @PutMapping
    public CustomerPreferenceDto update(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateCustomerPreferenceRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return preferenceService.updatePreferences(customer.getId(), request.language(), request.region(),
            request.timeZone(), request.themeMode(), request.reducedMotion());
    }
}
