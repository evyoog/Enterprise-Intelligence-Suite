package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the caller's organization for the {@code /organization/me/billing/**}
 * scope, and enforces who may act on it (FRD Open question 5: decided in
 * C47 as organization admins only, via the existing {@code MANAGE_ORGANIZATION}
 * permission — no new org-scoped permission was added for this). Same shape
 * as {@code OrderService#resolveMembership}/{@code #requireManageOrders},
 * duplicated rather than shared per this codebase's own established
 * convention (see that class's own javadoc on the same point). */
@Component
@RequiredArgsConstructor
public class BillingOwnerResolver {

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthorizationService authorizationService;

    public OrganizationMember resolveMembership(Long customerId) {
        OrganizationMember member = memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("You are not a member of an organization"));
        Organization organization = organizationRepository.findById(member.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (!authorizationService.organizationInGoodStanding(organization.getStatus())
                || organization.getLifecycleStatus() != OrganizationLifecycleStatus.ACTIVE) {
            throw new ForbiddenException("Your organization's account is not currently active");
        }
        return member;
    }

    public OrganizationMember requireManageOrganizationBilling(Long customerId) {
        OrganizationMember member = resolveMembership(customerId);
        if (!authorizationService.hasOrganizationPermission(member.getOrgRole(), "MANAGE_ORGANIZATION")) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        return member;
    }
}
