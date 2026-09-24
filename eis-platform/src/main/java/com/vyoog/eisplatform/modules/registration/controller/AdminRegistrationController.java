package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.registration.dto.CustomerAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.LinkKeycloakUserRequest;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationSummaryDto;
import com.vyoog.eisplatform.modules.registration.dto.PendingProvisioningDto;
import com.vyoog.eisplatform.modules.registration.dto.UpdateSeatsRequest;
import com.vyoog.eisplatform.modules.registration.service.AdminRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}
