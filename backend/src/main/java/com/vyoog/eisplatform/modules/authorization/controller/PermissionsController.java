package com.vyoog.eisplatform.modules.authorization.controller;

import com.vyoog.eisplatform.modules.authorization.dto.MyPermissionsDto;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Screen-level permission data for the frontend (this phase's own
 * requirement) — any authenticated caller, deliberately more permissive than
 * every other endpoint in this app, since knowing what you're NOT allowed to
 * do can't itself require permission. Never throws for a caller with no
 * linked {@code Customer} row (a real, normal case for a platform admin —
 * see CurrentCustomerResolver's own javadoc) — that caller just gets an empty
 * {@code organization} list alongside their real platform permissions.
 */
@RestController
@RequiredArgsConstructor
public class PermissionsController {

    private final AuthorizationService authorizationService;
    private final CurrentCustomerResolver currentCustomerResolver;
    private final OrganizationSelfService organizationSelfService;

    @GetMapping("/me/permissions")
    public MyPermissionsDto myPermissions(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        List<String> authorityNames = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        List<String> platform = authorizationService.listPlatformPermissions(authorityNames);

        List<String> organization = currentCustomerResolver.resolveOptional(jwt)
            .map(customer -> organizationSelfService.listMyOrganizationPermissions(customer.getId()))
            .orElse(List.of());

        return new MyPermissionsDto(platform, organization);
    }
}
