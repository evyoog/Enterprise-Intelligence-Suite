package com.vyoog.eisplatform.modules.administration.controller;

import com.vyoog.eisplatform.modules.administration.dto.PlatformDefaultsDto;
import com.vyoog.eisplatform.modules.administration.dto.SavePlatformDefaultsRequest;
import com.vyoog.eisplatform.modules.administration.service.PlatformDefaultsService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** REQ-GOV-002 (C92, TM-2 A): platform defaults for new users. Gated by {@code MANAGE_PLATFORM_SETTINGS} through the
 * {@code /admin/platform-settings/**} rule in SecurityConfig. */
@RestController
@RequestMapping("/admin/platform-settings/defaults")
@RequiredArgsConstructor
public class AdminPlatformDefaultsController {

    private final PlatformDefaultsService service;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public PlatformDefaultsDto get() {
        return service.get();
    }

    @PutMapping
    public PlatformDefaultsDto save(@RequestBody SavePlatformDefaultsRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.save(request, currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null), jwt.getSubject());
    }
}
