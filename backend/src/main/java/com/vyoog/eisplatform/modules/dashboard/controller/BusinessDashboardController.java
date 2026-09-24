package com.vyoog.eisplatform.modules.dashboard.controller;

import com.vyoog.eisplatform.modules.dashboard.dto.BusinessDashboardDto;
import com.vyoog.eisplatform.modules.dashboard.service.BusinessDashboardService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 19: covered by SecurityConfig's existing {@code /organization/me/**}
 * authenticated() rule — the real access boundary is inside
 * BusinessDashboardService (requireBusinessDashboardAccess, MANAGE_ORGANIZATION
 * only), same pattern as every other org-scoped write already gated inside
 * OrganizationSelfService rather than at the URL level.
 */
@RestController
@RequestMapping("/organization/me/business-dashboard")
@RequiredArgsConstructor
public class BusinessDashboardController {

    private final BusinessDashboardService businessDashboardService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public BusinessDashboardDto getBusinessDashboard(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return businessDashboardService.getBusinessDashboard(customer.getId());
    }
}
