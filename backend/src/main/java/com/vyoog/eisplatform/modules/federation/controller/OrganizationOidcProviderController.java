package com.vyoog.eisplatform.modules.federation.controller;

import com.vyoog.eisplatform.modules.federation.dto.ClaimMappingDto;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderDto;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderRequest;
import com.vyoog.eisplatform.modules.federation.dto.OidcProviderTestResultDto;
import com.vyoog.eisplatform.modules.federation.service.OidcProviderService;
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
 * REQ-IAM-006 (C27): the organization's OIDC providers, mirroring
 * OrganizationSamlProviderController — MANAGE_ORGANIZATION, always the
 * caller's own organization.
 */
@RestController
@RequestMapping("/organization/me/oidc-providers")
@RequiredArgsConstructor
public class OrganizationOidcProviderController {

    private final OidcProviderService oidcProviderService;
    private final OrganizationSelfService organizationSelfService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public List<OidcProviderDto> list(@AuthenticationPrincipal Jwt jwt) {
        return oidcProviderService.listForOrganization(requireOrgId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OidcProviderDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OidcProviderRequest request) {
        return oidcProviderService.create(requireOrgId(jwt), request);
    }

    @PutMapping("/{id}")
    public OidcProviderDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody OidcProviderRequest request) {
        return oidcProviderService.update(requireOrgId(jwt), id, request);
    }

    @PostMapping("/{id}/enable")
    public OidcProviderDto enable(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return oidcProviderService.setEnabled(requireOrgId(jwt), id, true);
    }

    @PostMapping("/{id}/disable")
    public OidcProviderDto disable(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return oidcProviderService.setEnabled(requireOrgId(jwt), id, false);
    }

    @PostMapping("/{id}/test")
    public OidcProviderTestResultDto test(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return oidcProviderService.test(requireOrgId(jwt), id);
    }

    /** REQ-IAM-007 (C28). */
    @PutMapping("/{id}/claim-mapping")
    public OidcProviderDto updateClaimMapping(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                              @Valid @RequestBody ClaimMappingDto request) {
        return oidcProviderService.updateClaimMapping(requireOrgId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        oidcProviderService.delete(requireOrgId(jwt), id);
    }

    private Long requireOrgId(Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.requireOrganizationManagement(customer.getId());
    }
}
