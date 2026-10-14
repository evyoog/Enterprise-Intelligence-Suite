package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.registration.dto.EntitlementDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.registration.service.EntitlementService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REQ-SUB-002 (C97 EN-1, EN-3): read-only entitlements for the signed-in customer, their organization, and a support lookup for platform admins. */
@RestController
@RequiredArgsConstructor
public class EntitlementController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final EntitlementService entitlementService;
    private final OrganizationSelfService organizationSelfService;

    @GetMapping("/me/entitlements")
    public List<EntitlementDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return entitlementService.forCustomer(currentCustomerResolver.resolve(jwt).getId());
    }

    @GetMapping("/organization/me/entitlements")
    public List<EntitlementDto> myOrganization(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return entitlementService.forOrganization(organizationSelfService.myOrganizationId(customer.getId()));
    }

    /** {@code VIEW_ENTITLEMENTS} is enforced in SecurityConfig for every path under /admin/entitlements. */
    @GetMapping("/admin/entitlements/customers/{customerId}")
    public List<EntitlementDto> adminCustomer(@AuthenticationPrincipal Jwt jwt, @PathVariable Long customerId) {
        return entitlementService.adminLookupCustomer(currentCustomerResolver.resolve(jwt).getId(), customerId);
    }

    @GetMapping("/admin/entitlements/organizations/{organizationId}")
    public List<EntitlementDto> adminOrganization(@AuthenticationPrincipal Jwt jwt, @PathVariable Long organizationId) {
        return entitlementService.adminLookupOrganization(currentCustomerResolver.resolve(jwt).getId(), organizationId);
    }
}
