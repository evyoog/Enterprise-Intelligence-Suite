package com.vyoog.eisplatform.modules.federation.controller;

import com.vyoog.eisplatform.modules.federation.dto.*;
import com.vyoog.eisplatform.modules.federation.service.SamlProviderService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 4 (2026.3.3): ORG_ADMIN self-service Identity Federation management
 * — gated by MANAGE_ORGANIZATION (same permission the MFA policy toggle
 * uses, and for the same reason: "how my organization's own members
 * authenticate" is one class of setting), always scoped to the caller's own
 * organization via {@link OrganizationSelfService#requireOrganizationManagement},
 * never a client-supplied organization id.
 */
@RestController
@RequestMapping("/organization/me/saml-providers")
@RequiredArgsConstructor
public class OrganizationSamlProviderController {

    private final SamlProviderService samlProviderService;
    private final OrganizationSelfService organizationSelfService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public List<SamlProviderDto> list(@AuthenticationPrincipal Jwt jwt) {
        Long organizationId = requireOrgId(jwt);
        return samlProviderService.listForOrganization(organizationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SamlProviderDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateSamlProviderRequest request) {
        Long organizationId = requireOrgId(jwt);
        return samlProviderService.create(organizationId, request);
    }

    @PutMapping("/{id}")
    public SamlProviderDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody UpdateSamlProviderRequest request) {
        Long organizationId = requireOrgId(jwt);
        return samlProviderService.update(organizationId, id, request);
    }

    @PostMapping("/{id}/enable")
    public SamlProviderDto enable(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long organizationId = requireOrgId(jwt);
        return samlProviderService.setEnabled(organizationId, id, true);
    }

    @PostMapping("/{id}/disable")
    public SamlProviderDto disable(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long organizationId = requireOrgId(jwt);
        return samlProviderService.setEnabled(organizationId, id, false);
    }

    @PostMapping("/{id}/test")
    public SamlProviderTestResultDto test(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long organizationId = requireOrgId(jwt);
        return samlProviderService.test(organizationId, id);
    }

    /** REQ-IAM-007 (C28). */
    @PutMapping("/{id}/claim-mapping")
    public SamlProviderDto updateClaimMapping(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                              @Valid @RequestBody ClaimMappingDto request) {
        return samlProviderService.updateClaimMapping(requireOrgId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long organizationId = requireOrgId(jwt);
        samlProviderService.delete(organizationId, id);
    }

    private Long requireOrgId(Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.requireOrganizationManagement(customer.getId());
    }
}
