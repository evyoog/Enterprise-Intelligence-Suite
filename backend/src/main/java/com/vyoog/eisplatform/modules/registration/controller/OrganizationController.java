package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessDecisionRequest;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestDto;
import com.vyoog.eisplatform.modules.registration.dto.AssignProductAccessRequest;
import com.vyoog.eisplatform.modules.registration.dto.ChangeMemberRoleRequest;
import com.vyoog.eisplatform.modules.registration.dto.OrgMemberDto;
import com.vyoog.eisplatform.modules.registration.dto.OrgProductAccessDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.dto.UpdateMfaPolicyRequest;
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
 * Organization self-service, always scoped to the CALLER's own organization
 * (resolved from their own customer id, itself resolved from their JWT —
 * never from an org id in the URL or body, so there is no way to reach
 * another organization's data through this controller). Listing users is
 * further restricted to ORG_ADMIN inside OrganizationSelfService; everything
 * else here just requires being an active member of some organization.
 */
@RestController
@RequestMapping("/organization/me")
@RequiredArgsConstructor
public class OrganizationController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final OrganizationSelfService organizationSelfService;

    @GetMapping
    public OrganizationDto myOrganization(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.getMyOrganization(customer.getId());
    }

    /** Phase 7 — requires MANAGE_ORGANIZATION (enforced inside the service).
     * Toggling this never signs anyone out; it takes effect on each
     * member's next fresh login (see AuthController#login). */
    @PatchMapping("/mfa-policy")
    public OrganizationDto updateMfaPolicy(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateMfaPolicyRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.updateMfaPolicy(customer.getId(), request.mfaRequired());
    }

    @GetMapping("/users")
    public List<OrgMemberDto> myOrgUsers(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.listMyOrgUsers(customer.getId());
    }

    /** Phase 3 (2026.3.3) — requires MANAGE_USERS, same-organization-only
     * (enforced inside the service). */
    @PatchMapping("/members/{memberId}/role")
    public OrgMemberDto changeMemberRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long memberId,
            @Valid @RequestBody ChangeMemberRoleRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.changeMemberRole(customer.getId(), memberId, request.orgRole());
    }

    /** C30: reset a member's two-factor authentication (MANAGE_USERS, same organization). */
    @PostMapping("/members/{memberId}/mfa/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetMemberMfa(@AuthenticationPrincipal Jwt jwt, @PathVariable Long memberId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        organizationSelfService.resetMemberMfa(customer.getId(), memberId);
    }

    @GetMapping("/products")
    public List<OrgProductAccessDto> myOrgProducts(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.listMyOrgProducts(customer.getId());
    }

    @GetMapping("/subscription")
    public List<SubscriptionDto> myOrgSubscription(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.listMyOrgSubscriptions(customer.getId());
    }

    /** Requires the MANAGE_PRODUCT_ACCESS permission (enforced inside the
     * service, not here — see OrganizationSelfService#requirePermission) —
     * view of one teammate's own per-product access, alongside what the org
     * has subscribed to. */
    @GetMapping("/members/{memberId}/products")
    public List<OrgProductAccessDto> memberProducts(@AuthenticationPrincipal Jwt jwt, @PathVariable Long memberId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.listMemberProducts(customer.getId(), memberId);
    }

    /** Grants/updates a teammate's access to one product the organization has
     * already subscribed to — ORG_ADMIN only, same-organization only. */
    @PostMapping("/members/{memberId}/products")
    public OrgProductAccessDto assignMemberProductAccess(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long memberId,
            @Valid @RequestBody AssignProductAccessRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.assignProductAccess(customer.getId(), memberId, request.productId(), request.productRole());
    }

    /** Revokes a teammate's access to one product — ORG_ADMIN only,
     * same-organization only. Never deletes the underlying row, see
     * OrganizationSelfService#revokeProductAccess. */
    @DeleteMapping("/members/{memberId}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeMemberProductAccess(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long memberId,
            @PathVariable Long productId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        organizationSelfService.revokeProductAccess(customer.getId(), memberId, productId);
    }

    /** Phase 6 (PAM), ORGANIZATION scope — the PLATFORM-scope equivalent
     * lives in PlatformPrivilegedAccessController instead. All three require
     * MANAGE_PRIVILEGED_ACCESS (enforced inside the service). */
    @GetMapping("/privileged-access/pending")
    public List<PrivilegedAccessRequestDto> pendingPrivilegedAccess(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.listPendingPrivilegedAccess(customer.getId());
    }

    /** REQ-IAM-004.7: active grants, so an organization admin can revoke one early. */
    @GetMapping("/privileged-access/active")
    public List<PrivilegedAccessRequestDto> activePrivilegedAccess(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.listActivePrivilegedAccess(customer.getId());
    }

    @PostMapping("/privileged-access/{id}/approve")
    public PrivilegedAccessRequestDto approvePrivilegedAccess(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @RequestBody(required = false) PrivilegedAccessDecisionRequest body) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.approvePrivilegedAccess(customer.getId(), jwt.getSubject(), id, body == null ? null : body.note());
    }

    @PostMapping("/privileged-access/{id}/reject")
    public PrivilegedAccessRequestDto rejectPrivilegedAccess(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @RequestBody(required = false) PrivilegedAccessDecisionRequest body) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.rejectPrivilegedAccess(customer.getId(), jwt.getSubject(), id, body == null ? null : body.note());
    }

    @PostMapping("/privileged-access/{id}/revoke")
    public PrivilegedAccessRequestDto revokePrivilegedAccess(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @RequestBody(required = false) PrivilegedAccessDecisionRequest body) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return organizationSelfService.revokePrivilegedAccess(customer.getId(), jwt.getSubject(), id, body == null ? null : body.note());
    }
}
