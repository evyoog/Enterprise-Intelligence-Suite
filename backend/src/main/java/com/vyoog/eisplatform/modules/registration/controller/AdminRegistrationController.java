package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.registration.dto.CustomerAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.LinkKeycloakUserRequest;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationLifecycleRequest;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationLifecycleResultDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationSummaryDto;
import com.vyoog.eisplatform.modules.registration.dto.PendingProvisioningDto;
import com.vyoog.eisplatform.modules.registration.dto.ResetMfaRequest;
import com.vyoog.eisplatform.modules.registration.dto.UpdateOrganizationRequest;
import com.vyoog.eisplatform.modules.registration.dto.UpdateSeatsRequest;
import com.vyoog.eisplatform.modules.registration.service.AdminRegistrationService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Platform-admin-only (hasRole("ADMIN") — see SecurityConfig; per this
 * phase's decision, the existing catalog-management ADMIN role doubles as
 * PLATFORM_ADMIN rather than a new Keycloak role being introduced). Also
 * where GET /register/organization/parents from the API list lives — the
 * ability to browse organizations for setting a parent link is admin-only,
 * never exposed to public registration.
 */
@RestController
@RequestMapping("/admin/registrations")
@RequiredArgsConstructor
public class AdminRegistrationController {

    private final AdminRegistrationService adminRegistrationService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping("/pending-provisioning")
    public List<PendingProvisioningDto> pendingProvisioning() {
        return adminRegistrationService.listPendingProvisioning();
    }

    /** Every registered organization, full company details — the general
     * admin directory, not just the ones still awaiting a Keycloak link. */
    @GetMapping("/organizations")
    public List<OrganizationAdminDto> allOrganizations() {
        return adminRegistrationService.listAllOrganizations();
    }

    /** Every individual customer who never joined an organization (an org's
     * own admin/members are shown via #allOrganizations instead). */
    @GetMapping("/individuals")
    public List<CustomerAdminDto> allIndividuals() {
        return adminRegistrationService.listAllIndividuals();
    }

    @PostMapping("/{customerId}/link-keycloak-user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkKeycloakUser(@PathVariable Long customerId, @Valid @RequestBody LinkKeycloakUserRequest request) {
        adminRegistrationService.linkKeycloakUser(customerId, request.keycloakSub());
    }

    /** GET /register/organization/parents from the spec's API list — kept
     * under /admin since it's admin/authorized-flows only, never reachable
     * from public registration (see Organization's own javadoc). */
    @GetMapping("/organizations/parents")
    public List<OrganizationSummaryDto> organizationParents() {
        return adminRegistrationService.listOrganizationsForParentSelection();
    }

    @PatchMapping("/organizations/{organizationId}/seats")
    public OrganizationDto updateSeats(@PathVariable Long organizationId, @Valid @RequestBody UpdateSeatsRequest request) {
        return adminRegistrationService.updateSeats(organizationId, request.licensedSeats());
    }

    /** REQ-TEN-001 / 05.01.01.02 Update organization. */
    @PutMapping("/organizations/{organizationId}")
    public OrganizationAdminDto updateOrganization(@PathVariable Long organizationId,
                                                   @Valid @RequestBody UpdateOrganizationRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return adminRegistrationService.updateOrganization(organizationId, request, actor(jwt));
    }

    /** REQ-TEN-001 / 05.01.01.03 Suspend organization. */
    @PostMapping("/organizations/{organizationId}/suspend")
    public OrganizationLifecycleResultDto suspendOrganization(@PathVariable Long organizationId,
                                                              @Valid @RequestBody(required = false) OrganizationLifecycleRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return adminRegistrationService.suspendOrganization(organizationId, reason(request), actor(jwt));
    }

    /** REQ-TEN-001 / 05.01.01.04 Activate organization. */
    @PostMapping("/organizations/{organizationId}/activate")
    public OrganizationLifecycleResultDto activateOrganization(@PathVariable Long organizationId,
                                                               @Valid @RequestBody(required = false) OrganizationLifecycleRequest request,
                                                               @AuthenticationPrincipal Jwt jwt) {
        return adminRegistrationService.activateOrganization(organizationId, reason(request), actor(jwt));
    }

    /** REQ-TEN-001 / 05.01.01.05 Close organization (soft; nothing is deleted). */
    @PostMapping("/organizations/{organizationId}/close")
    public OrganizationLifecycleResultDto closeOrganization(@PathVariable Long organizationId,
                                                            @Valid @RequestBody(required = false) OrganizationLifecycleRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {
        return adminRegistrationService.closeOrganization(organizationId, reason(request), actor(jwt));
    }

    /** C30: reset any account's two-factor authentication, by email. */
    @PostMapping("/mfa-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetMfa(@Valid @RequestBody ResetMfaRequest request, @AuthenticationPrincipal Jwt jwt) {
        adminRegistrationService.resetMfaByEmail(request.email(), actor(jwt));
    }

    private AdminRegistrationService.Actor actor(Jwt jwt) {
        Long customerId = currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null);
        return new AdminRegistrationService.Actor(jwt.getSubject(), customerId, jwt.getClaimAsString("email"));
    }

    private static String reason(OrganizationLifecycleRequest request) {
        return request == null ? null : request.reason();
    }
}
