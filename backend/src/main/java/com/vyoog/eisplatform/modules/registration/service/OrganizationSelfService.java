package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestDto;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.service.PlatformMfaService;
import com.vyoog.eisplatform.modules.authorization.service.PrivilegedAccessService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.OrgMemberDto;
import com.vyoog.eisplatform.modules.registration.dto.OrgProductAccessDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Everything here is scoped to the CALLER's own organization, resolved from
 * their own customer id (itself resolved from their JWT `sub` by the
 * controller) — never from an org id supplied by the client. An
 * ORG_ADMIN can never reach another organization's data through this
 * service; there's no method here that accepts an arbitrary organization id.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationSelfService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationProductAccessRepository accessRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final OrganizationMemberService organizationMemberService;
    private final CustomerRepository customerRepository;
    private final AuthorizationService authorizationService;
    private final PrivilegedAccessService privilegedAccessService;
    private final PlatformMfaService platformMfaService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    /** Phase 16: a plain, non-throwing "does this customer belong to any
     * organization at all" check — for callers like DashboardService that
     * need to branch on this as a normal, expected case (an individual
     * customer), rather than using {@link #resolveMembership}'s exception as
     * control flow the way the older frontend code (MyProductsPage.tsx's own
     * catch-based branch) already does. Deliberately membership-only, not
     * the fuller org-good-standing check {@link #resolveMembership} also
     * does — a dashboard should still show SOMETHING for a member of a
     * struggling org, not be blocked at this cheap existence check. */
    public boolean hasOrganization(Long customerId) {
        return memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE).isPresent();
    }

    /** Every self-service method starts here — resolves which org (if any)
     * this customer belongs to, as an ACTIVE member, AND (Phase 4) that the
     * organization itself is in good standing. A customer can't out-argue
     * their own organization being CANCELLED/EXPIRED by having an ACTIVE
     * membership row — both facts must hold. */
    private OrganizationMember resolveMembership(Long customerId) {
        OrganizationMember member = memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("You are not a member of an organization"));
        Organization organization = organizationRepository.findById(member.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (!authorizationService.organizationInGoodStanding(organization.getStatus())) {
            throw new ForbiddenException("Your organization's account is not currently active");
        }
        // REQ-TEN-001 BR-TEN-008: a suspended or closed organization is refused
        // too, so a member's still-valid access token stops working at once.
        if (organization.getLifecycleStatus() != OrganizationLifecycleStatus.ACTIVE) {
            throw new ForbiddenException("Your organization's account is not currently active");
        }
        return member;
    }

    /** Phase 3: replaces the old {@code orgRole != ORG_ADMIN} check with a
     * real permission lookup — ORG_ADMIN still qualifies for everything it
     * did before (see RbacSeeder's ORG_ADMIN permission set), but which
     * specific action needs which permission is now named and independently
     * revocable, not one blanket "is this person the admin" gate. Used where
     * the action has no distinct external "resource" to check ownership of
     * (e.g. listing the caller's own org's users) — see
     * {@link #requirePermissionOnMember} for actions that do.
     *
     * <p>Phase 6 (PAM): also allows a currently-active, approved privileged-
     * access grant for this exact permission to satisfy the check — "does
     * this role have this permission, OR does this specific person hold a
     * time-boxed grant for it right now" — so a normal MEMBER can act with
     * ORG_ADMIN-equivalent authority for a limited window without a
     * permanent role change. */
    private OrganizationMember requirePermission(Long customerId, String permissionName) {
        OrganizationMember member = resolveMembership(customerId);
        boolean allowed = authorizationService.hasOrganizationPermission(member.getOrgRole(), permissionName)
            || privilegedAccessService.hasActiveOrganizationGrant(customerId, member.getOrganizationId(), permissionName);
        if (!allowed) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        return member;
    }

    /** The caller and the specific org member ("resource") an action is
     * about to be performed on — see {@link #requirePermissionOnMember}. */
    private record MemberAccess(OrganizationMember caller, OrganizationMember target) {
    }

    /**
     * Phase 4/5 (ABAC + resource-based): the roadmap's own example rule,
     * applied to "another member of my organization" as the resource —
     * replaces what used to be two separate, sequential checks
     * ({@code requirePermission} then a standalone org-id equality
     * comparison) with one call into {@link AuthorizationService#evaluate},
     * so the "does this role have this permission" and "does this resource
     * belong to my organization" facts are composed in one place instead of
     * duplicated across call sites — and, since {@code evaluate} works
     * against any {@link com.vyoog.eisplatform.modules.authorization.model.OrganizationOwnedResource},
     * this same call would work unchanged if a future action needed to check
     * a different tenant-owned resource, not just {@code OrganizationMember}.
     * Deliberately returns one generic "you do not have permission" error for
     * both a wrong role AND a cross-organization target, rather than
     * distinguishing them — telling an attacker WHY they were denied (e.g.
     * confirming a member id exists, just in another organization) is
     * exactly the kind of leak a resource check like this exists to prevent.
     *
     * <p>Phase 6 (PAM): a currently-active grant for this permission also
     * satisfies this check — but tenant isolation is still enforced
     * separately even then, so a temporary grant can never reach across
     * organizations either.
     */
    private MemberAccess requirePermissionOnMember(Long callerCustomerId, Long targetMemberId, String permissionName) {
        OrganizationMember caller = resolveMembership(callerCustomerId);
        OrganizationMember target = memberRepository.findById(targetMemberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        boolean sameOrganization = caller.getOrganizationId().equals(target.getOrganizationId());
        boolean allowed = authorizationService.evaluate(caller, target, permissionName)
            || (sameOrganization && privilegedAccessService.hasActiveOrganizationGrant(callerCustomerId, caller.getOrganizationId(), permissionName));
        if (!allowed) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        return new MemberAccess(caller, target);
    }

    public OrganizationDto getMyOrganization(Long customerId) {
        OrganizationMember member = resolveMembership(customerId);
        return toDto(organizationRepository.findById(member.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found")));
    }

    public List<OrgMemberDto> listMyOrgUsers(Long customerId) {
        OrganizationMember caller = requirePermission(customerId, "MANAGE_USERS");
        return memberRepository.findByOrganizationId(caller.getOrganizationId()).stream()
            .map(member -> {
                var customer = customerRepository.findById(member.getCustomerId()).orElse(null);
                return new OrgMemberDto(
                    member.getId(),
                    member.getCustomerId(),
                    customer == null ? null : customer.getFirstName(),
                    customer == null ? null : customer.getLastName(),
                    customer == null ? null : customer.getEmail(),
                    member.getOrgRole(),
                    member.getStatus()
                );
            })
            .toList();
    }

    /**
     * Phase 3 (2026.3.3): ORG_ADMIN-only, same-organization-only (enforced by
     * {@link #requirePermissionOnMember}) — the RBAC administration ask's
     * "assign role / change user's role", applied to the one place role
     * membership is genuinely DB-driven in this app (see {@code Role}'s own
     * javadoc; PLATFORM-scope "who holds ADMIN" stays Keycloak-controlled,
     * deliberately, and is not touched here).
     *
     * <p>Refuses to demote the organization's LAST active ORG_ADMIN — unlike
     * a single user's own privileged-access self-approval (blocked because
     * an alternative approver always exists), there is no fallback path
     * back into an organization with zero admins: every ORG_ADMIN-gated
     * action, including this one, would become permanently unreachable for
     * that organization.
     */
    @Transactional
    public OrgMemberDto changeMemberRole(Long callerCustomerId, Long targetMemberId, OrgRole newRole) {
        MemberAccess access = requirePermissionOnMember(callerCustomerId, targetMemberId, "MANAGE_USERS");
        OrganizationMember target = access.target();

        if (target.getOrgRole() == OrgRole.ORG_ADMIN && newRole != OrgRole.ORG_ADMIN) {
            long activeAdmins = memberRepository.countByOrganizationIdAndOrgRoleAndStatus(
                access.caller().getOrganizationId(), OrgRole.ORG_ADMIN, MembershipStatus.ACTIVE);
            if (activeAdmins <= 1) {
                throw new IllegalArgumentException("Cannot remove the organization's last administrator.");
            }
        }

        target.setOrgRole(newRole);
        memberRepository.save(target);

        customerRepository.findById(target.getCustomerId()).ifPresent(targetCustomer ->
            notificationService.notify(targetCustomer.getId(), targetCustomer.getEmail(),
                NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
                "Your organization role changed",
                "Your role in your organization is now " + newRole + "."));
        auditService.recordSuccess("MEMBER_ROLE_CHANGED", null, callerCustomerId, null,
            "OrganizationMember", String.valueOf(target.getId()), access.caller().getOrganizationId(),
            "Changed member " + target.getId() + "'s role to " + newRole);

        var customer = customerRepository.findById(target.getCustomerId()).orElse(null);
        return new OrgMemberDto(
            target.getId(), target.getCustomerId(),
            customer == null ? null : customer.getFirstName(),
            customer == null ? null : customer.getLastName(),
            customer == null ? null : customer.getEmail(),
            target.getOrgRole(), target.getStatus()
        );
    }

    /** C30: an organization admin (MANAGE_USERS, same organization only)
     * resets a member's two-factor authentication. See PlatformMfaService#resetByAdmin. */
    @Transactional
    public void resetMemberMfa(Long callerCustomerId, Long targetMemberId) {
        MemberAccess access = requirePermissionOnMember(callerCustomerId, targetMemberId, "MANAGE_USERS");
        platformMfaService.resetByAdmin(access.target().getCustomerId(), null, callerCustomerId, null,
            access.caller().getOrganizationId());
    }

    public List<SubscriptionDto> listMyOrgSubscriptions(Long customerId) {
        OrganizationMember member = resolveMembership(customerId);
        return subscriptionRepository.findByOwnerOrganizationId(member.getOrganizationId()).stream()
            .map(subscription -> new SubscriptionDto(
                subscription.getId(),
                subscription.getProductId(),
                productRepository.findById(subscription.getProductId()).map(Product::getName).orElse("Unknown product"),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getExpiresAt()
            ))
            .toList();
    }

    /** The Product Suite's org-member view: for every product the ORG has
     * ever subscribed to, this member's OWN assignment is a completely
     * separate fact — never conflate "org purchased it" with "I can use
     * it" (see OrganizationProductAccess's own javadoc). */
    public List<OrgProductAccessDto> listMyOrgProducts(Long customerId) {
        return buildProductAccessView(resolveMembership(customerId));
    }

    /** Screen-level permission data (see PermissionsController) for a caller
     * who may or may not belong to an organization at all — deliberately a
     * plain empty list, never a 404, for "not a member of any organization",
     * since not belonging to one isn't an error at this specific call site. */
    public List<String> listMyOrganizationPermissions(Long customerId) {
        return memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .map(member -> authorizationService.listOrganizationPermissions(member.getOrgRole()))
            .orElse(List.of());
    }

    /** Same two-tier view as {@link #listMyOrgProducts}, but for an arbitrary
     * member of the caller's own organization — how an ORG_ADMIN sees what a
     * specific teammate is (and isn't) assigned to before granting/revoking
     * anything. */
    public List<OrgProductAccessDto> listMemberProducts(Long callerCustomerId, Long targetMemberId) {
        MemberAccess access = requirePermissionOnMember(callerCustomerId, targetMemberId, "MANAGE_PRODUCT_ACCESS");
        return buildProductAccessView(access.target());
    }

    /**
     * Grants (or updates) {@code targetMemberId}'s own access to one product —
     * ORG_ADMIN only, and only for a member of the caller's own organization.
     * The org must already hold an ACTIVE subscription for that product: a
     * member's access is always a SUBSET of what the org purchased, never a
     * superset (see OrganizationProductAccess's own javadoc on this). Phase 4:
     * also refuses to grant access to a product the catalog itself has since
     * marked INACTIVE — a discontinued product shouldn't accept new access
     * grants even if the org's own subscription row is technically still ACTIVE.
     */
    @Transactional
    public OrgProductAccessDto assignProductAccess(Long callerCustomerId, Long targetMemberId, Long productId, String productRole) {
        MemberAccess memberAccess = requirePermissionOnMember(callerCustomerId, targetMemberId, "MANAGE_PRODUCT_ACCESS");
        OrganizationMember caller = memberAccess.caller();
        OrganizationMember target = memberAccess.target();

        Product product = productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("This product is not currently available."));

        ProductSubscription subscription = subscriptionRepository
            .findByOwnerOrganizationIdAndProductId(caller.getOrganizationId(), productId)
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException(
                "Your organization does not have an active subscription for this product."));

        OrganizationProductAccess access = accessRepository
            .findByOrganizationMemberIdAndProductId(target.getId(), productId)
            .orElseGet(OrganizationProductAccess::new);
        access.setOrganizationMemberId(target.getId());
        access.setProductId(productId);
        access.setProductRole(productRole);
        access.setStatus(MembershipStatus.ACTIVE);
        if (access.getAssignedAt() == null) {
            access.setAssignedAt(Instant.now());
        }
        access = accessRepository.save(access);

        customerRepository.findById(target.getCustomerId()).ifPresent(targetCustomer ->
            notificationService.notify(targetCustomer.getId(), targetCustomer.getEmail(),
                NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
                "Access granted: " + product.getName(),
                "You've been given " + productRole + " access to " + product.getName() + "."));
        auditService.recordSuccess("PRODUCT_ACCESS_GRANTED", null, callerCustomerId, null,
            "OrganizationProductAccess", productId + ":" + target.getId(), caller.getOrganizationId(),
            "Granted " + productRole + " access to product " + productId + " for member " + target.getId());

        return new OrgProductAccessDto(
            productId,
            product.getName(),
            product.getCategory(),
            subscription.getStatus(),
            true,
            access.getProductRole()
        );
    }

    /** Revokes {@code targetMemberId}'s own access to one product — never
     * deletes the row, flips it INACTIVE, same "keep history" convention as
     * {@link OrganizationMemberService#removeMember}. A no-op (not an error)
     * if the member never had access in the first place. */
    @Transactional
    public void revokeProductAccess(Long callerCustomerId, Long targetMemberId, Long productId) {
        MemberAccess memberAccess = requirePermissionOnMember(callerCustomerId, targetMemberId, "MANAGE_PRODUCT_ACCESS");

        accessRepository.findByOrganizationMemberIdAndProductId(memberAccess.target().getId(), productId)
            .ifPresent(access -> {
                access.setStatus(MembershipStatus.INACTIVE);
                accessRepository.save(access);
                auditService.recordSuccess("PRODUCT_ACCESS_REVOKED", null, callerCustomerId, null,
                    "OrganizationProductAccess", productId + ":" + targetMemberId, memberAccess.caller().getOrganizationId(),
                    "Revoked access to product " + productId + " for member " + targetMemberId);
            });
    }

    // ------------------------------------------------------------------
    // Phase 6 (PAM): the ORGANIZATION-scope half of privileged access
    // review/approval — the PLATFORM-scope half lives in
    // PlatformPrivilegedAccessController instead. All three methods are
    // gated by MANAGE_PRIVILEGED_ACCESS, never satisfiable by a temporary
    // grant of that exact permission — PrivilegedAccessService.request()
    // refuses to create one in the first place (see its own javadoc), so
    // requirePermission's grant-aware OR-check can never actually find one
    // to approve with here, closing the self-escalation loop transitively
    // rather than needing a second explicit guard in this class too.
    // ------------------------------------------------------------------

    public List<PrivilegedAccessRequestDto> listPendingPrivilegedAccess(Long customerId) {
        OrganizationMember member = requirePermission(customerId, "MANAGE_PRIVILEGED_ACCESS");
        return privilegedAccessService.listPendingForOrganization(member.getOrganizationId());
    }

    /** REQ-IAM-004.7: same permission as reviewing pending requests. */
    public List<PrivilegedAccessRequestDto> listActivePrivilegedAccess(Long customerId) {
        OrganizationMember member = requirePermission(customerId, "MANAGE_PRIVILEGED_ACCESS");
        return privilegedAccessService.listActiveForOrganization(member.getOrganizationId());
    }

    public PrivilegedAccessRequestDto approvePrivilegedAccess(Long customerId, String approverKeycloakSub, Long requestId, String note) {
        OrganizationMember member = requirePermission(customerId, "MANAGE_PRIVILEGED_ACCESS");
        return privilegedAccessService.approve(approverKeycloakSub, requestId, RoleScope.ORGANIZATION, member.getOrganizationId(), note);
    }

    public PrivilegedAccessRequestDto rejectPrivilegedAccess(Long customerId, String approverKeycloakSub, Long requestId, String note) {
        OrganizationMember member = requirePermission(customerId, "MANAGE_PRIVILEGED_ACCESS");
        return privilegedAccessService.reject(approverKeycloakSub, requestId, RoleScope.ORGANIZATION, member.getOrganizationId(), note);
    }

    public PrivilegedAccessRequestDto revokePrivilegedAccess(Long customerId, String approverKeycloakSub, Long requestId, String note) {
        OrganizationMember member = requirePermission(customerId, "MANAGE_PRIVILEGED_ACCESS");
        return privilegedAccessService.revoke(approverKeycloakSub, requestId, RoleScope.ORGANIZATION, member.getOrganizationId(), note);
    }

    private List<OrgProductAccessDto> buildProductAccessView(OrganizationMember member) {
        Map<Long, SubscriptionStatus> orgSubscriptionByProduct = subscriptionRepository
            .findByOwnerOrganizationId(member.getOrganizationId()).stream()
            .collect(Collectors.toMap(ProductSubscription::getProductId, ProductSubscription::getStatus, (a, b) -> a));

        Map<Long, OrganizationProductAccess> accessByProduct = accessRepository
            .findByOrganizationMemberId(member.getId()).stream()
            .filter(access -> access.getStatus() == MembershipStatus.ACTIVE)
            .collect(Collectors.toMap(OrganizationProductAccess::getProductId, a -> a, (a, b) -> a));

        return orgSubscriptionByProduct.keySet().stream()
            .map(productId -> productRepository.findById(productId).orElse(null))
            .filter(java.util.Objects::nonNull)
            .map(product -> {
                OrganizationProductAccess access = accessByProduct.get(product.getId());
                return new OrgProductAccessDto(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    orgSubscriptionByProduct.get(product.getId()),
                    access != null,
                    access == null ? null : access.getProductRole()
                );
            })
            .toList();
    }

    private OrganizationDto toDto(Organization organization) {
        long activeMembers = organizationMemberService.listActiveMembers(organization.getId()).size();
        return new OrganizationDto(
            organization.getId(),
            organization.getName(),
            organization.getCode(),
            organization.getType(),
            organization.getIndustry(),
            organization.getWebsite(),
            organization.getBusinessEmail(),
            organization.getCountry(),
            organization.getLicensedSeats(),
            activeMembers,
            organization.getStatus(),
            organization.isMfaRequired()
        );
    }

    /** Phase 7: the one real enforcement point for MANAGE_ORGANIZATION this
     * permission has had since Phase 2 seeded it with no call site yet — see
     * that phase's own report. Toggling this never signs anyone out; it only
     * changes what the next fresh login checks (see AuthController#login). */
    @Transactional
    public OrganizationDto updateMfaPolicy(Long customerId, boolean mfaRequired) {
        OrganizationMember member = requirePermission(customerId, "MANAGE_ORGANIZATION");
        Organization organization = organizationRepository.findById(member.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        organization.setMfaRequired(mfaRequired);
        organization = organizationRepository.save(organization);
        auditService.recordSuccess("MFA_POLICY_CHANGED", null, customerId, null,
            "Organization", organization.getId().toString(), organization.getId(),
            "MFA requirement set to " + mfaRequired);
        return toDto(organization);
    }

    /** The one check gating any "see/manage the whole organization" surface
     * (business dashboard, org-wide audit trail, etc.) — same
     * MANAGE_ORGANIZATION permission as updateMfaPolicy. Returns the
     * organization id since callers typically run further org-scoped
     * queries, not just render one object. */
    public Long requireOrganizationManagement(Long customerId) {
        return requirePermission(customerId, "MANAGE_ORGANIZATION").getOrganizationId();
    }

    /** Phase 19: kept as its own name for readability at that call site —
     * same check as {@link #requireOrganizationManagement}. */
    public Long requireBusinessDashboardAccess(Long customerId) {
        return requireOrganizationManagement(customerId);
    }
}
