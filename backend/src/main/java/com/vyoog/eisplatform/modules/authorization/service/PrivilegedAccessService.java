package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessAuditEntryDto;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestDto;
import com.vyoog.eisplatform.modules.authorization.dto.RequestablePermissionDto;
import com.vyoog.eisplatform.modules.authorization.model.*;
import com.vyoog.eisplatform.modules.authorization.repository.PrivilegedAccessAuditEntryRepository;
import com.vyoog.eisplatform.modules.authorization.repository.PrivilegedAccessRequestRepository;
import com.vyoog.eisplatform.modules.authorization.repository.RoleRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Phase 6: Privileged Access Management. Implements the roadmap's own target
 * flow literally — "Normal user -> requests temporary admin access ->
 * approval -> temporary role granted -> access expires -> audit record
 * retained" — as a request/decision/expiry lifecycle on {@link PrivilegedAccessRequest},
 * with every transition recorded in {@link PrivilegedAccessAuditEntry}.
 *
 * <p>This is NOT {@code ImpersonationExchangeService} (SSO cross-app token
 * minting) and does not touch it — a completely separate concern this phase
 * was explicitly told not to confuse with PAM.
 *
 * <p><b>Authority-check placement, a deliberate design choice</b>: this
 * service does the mechanical state transitions (create/approve/reject/revoke)
 * and trusts its caller to have already verified the actor is allowed to
 * perform them — the same pattern {@code OrganizationSelfService} already
 * uses (its own {@code requirePermission} runs BEFORE calling into
 * {@code OrganizationMemberService}, which does no authority checking of its
 * own). Doing it this way here specifically avoids a circular dependency:
 * approving a request requires STANDING (permanent, Role-derived) authority
 * — checked via {@code AuthorizationService}'s existing pure RBAC methods,
 * by the caller — never another temporary grant from this same service. You
 * cannot approve privileged-access requests using privileged access that was
 * itself granted temporarily; that would let a chain of temporary grants
 * bootstrap arbitrary permanent-feeling access, defeating the entire point
 * of this phase.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivilegedAccessService {

    /** No request may ask for more than 8 hours — an unbounded "temporary"
     * grant isn't temporary. Enforced again here (not just at the DTO
     * validation layer) since this is the actual business rule, not merely
     * an input-shape constraint. */
    private static final int MAX_DURATION_MINUTES = 480;

    /** The one permission nobody may request through this flow — being able
     * to approve privileged-access requests must always come from a standing
     * role, never a temporary grant of the same kind (see this class's own
     * javadoc on why the authority check is placed the way it is). */
    private static final String SELF_ESCALATION_GUARD_PERMISSION = "MANAGE_PRIVILEGED_ACCESS";

    private final PrivilegedAccessRequestRepository requestRepository;
    private final PrivilegedAccessAuditEntryRepository auditRepository;
    private final RoleRepository roleRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // ------------------------------------------------------------------
    // Request
    // ------------------------------------------------------------------

    @Transactional
    public PrivilegedAccessRequestDto request(String requesterKeycloakSub, Long requesterCustomerId,
                                               String permissionName, String justification, int durationMinutes) {
        if (durationMinutes < 1 || durationMinutes > MAX_DURATION_MINUTES) {
            throw new IllegalArgumentException("Requested duration must be between 1 and " + MAX_DURATION_MINUTES + " minutes.");
        }
        if (SELF_ESCALATION_GUARD_PERMISSION.equals(permissionName)) {
            throw new IllegalArgumentException("This permission cannot be requested through privileged access — it must come from a standing role.");
        }

        RoleScope scope = resolveScope(permissionName);
        Long organizationId = null;
        if (scope == RoleScope.ORGANIZATION) {
            organizationId = organizationMemberRepository.findFirstByCustomerIdAndStatus(requesterCustomerId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("You must belong to an organization to request this permission."))
                .getOrganizationId();
        }

        PrivilegedAccessRequest request = new PrivilegedAccessRequest();
        request.setRequesterKeycloakSub(requesterKeycloakSub);
        request.setRequesterCustomerId(requesterCustomerId);
        request.setScope(scope);
        request.setOrganizationId(organizationId);
        request.setPermissionName(permissionName);
        request.setJustification(justification);
        request.setRequestedDurationMinutes(durationMinutes);
        request.setStatus(PrivilegedAccessStatus.PENDING);
        request.setRequestedAt(Instant.now());
        request = requestRepository.save(request);

        audit(request.getId(), PrivilegedAccessEventType.REQUESTED, requesterKeycloakSub, null);
        auditService.recordSuccess("PRIVILEGED_ACCESS_REQUESTED", requesterKeycloakSub, requesterCustomerId, null,
            "PrivilegedAccessRequest", request.getId().toString(), organizationId,
            "Requested " + permissionName + " for " + durationMinutes + " minutes");
        return toDto(request);
    }

    /**
     * Decision C24 (REQ-IAM-004): the permissions {@link #request} would accept
     * from this caller, so the request form can offer them as a dropdown. Applies
     * only the rules {@link #request} and {@link #resolveScope} already enforce —
     * granted by some role, in exactly one scope, never
     * {@link #SELF_ESCALATION_GUARD_PERMISSION}, and ORGANIZATION scope only for
     * an active organization member. Adds no rules of its own.
     */
    public List<RequestablePermissionDto> listRequestablePermissions(Long requesterCustomerId) {
        boolean isOrganizationMember = requesterCustomerId != null
            && organizationMemberRepository.findFirstByCustomerIdAndStatus(requesterCustomerId, MembershipStatus.ACTIVE).isPresent();

        Map<String, Set<RoleScope>> scopesByPermission = new TreeMap<>();
        Map<String, String> descriptionByPermission = new HashMap<>();
        for (Role role : roleRepository.findAll()) {
            for (Permission permission : role.getPermissions()) {
                scopesByPermission.computeIfAbsent(permission.getName(), name -> EnumSet.noneOf(RoleScope.class)).add(role.getScope());
                descriptionByPermission.putIfAbsent(permission.getName(), permission.getDescription());
            }
        }

        return scopesByPermission.entrySet().stream()
            .filter(entry -> !SELF_ESCALATION_GUARD_PERMISSION.equals(entry.getKey()))
            .filter(entry -> entry.getValue().size() == 1)
            .filter(entry -> isOrganizationMember || !entry.getValue().contains(RoleScope.ORGANIZATION))
            .map(entry -> new RequestablePermissionDto(
                entry.getKey(), entry.getValue().iterator().next(), descriptionByPermission.get(entry.getKey())))
            .toList();
    }

    private RoleScope resolveScope(String permissionName) {
        List<Role> grantingRoles = roleRepository.findByPermissions_Name(permissionName);
        if (grantingRoles.isEmpty()) {
            throw new IllegalArgumentException("\"" + permissionName + "\" is not a recognized permission.");
        }
        Set<RoleScope> scopes = grantingRoles.stream().map(Role::getScope).collect(Collectors.toSet());
        if (scopes.size() > 1) {
            // Not reachable with today's seeded data (no permission name is
            // shared across scopes) — guarded explicitly rather than silently
            // picking one, since that would be a real, surprising ambiguity.
            throw new IllegalArgumentException("\"" + permissionName + "\" is ambiguous across scopes.");
        }
        return scopes.iterator().next();
    }

    // ------------------------------------------------------------------
    // Decide (approve/reject) — caller must already have verified STANDING
    // authority for (expectedScope, expectedOrganizationId) before calling.
    // ------------------------------------------------------------------

    @Transactional
    public PrivilegedAccessRequestDto approve(String approverKeycloakSub, Long requestId,
                                               RoleScope expectedScope, Long expectedOrganizationId, String note) {
        PrivilegedAccessRequest request = requirePendingRequestInScope(requestId, expectedScope, expectedOrganizationId);
        if (approverKeycloakSub.equals(request.getRequesterKeycloakSub())) {
            throw new ForbiddenException("You cannot approve your own request.");
        }
        Instant now = Instant.now();
        request.setStatus(PrivilegedAccessStatus.APPROVED);
        request.setDecidedAt(now);
        request.setDecidedByKeycloakSub(approverKeycloakSub);
        request.setDecisionNote(note);
        request.setExpiresAt(now.plus(request.getRequestedDurationMinutes(), ChronoUnit.MINUTES));
        request = requestRepository.save(request);

        audit(request.getId(), PrivilegedAccessEventType.APPROVED, approverKeycloakSub, note);
        auditService.recordSuccess("PRIVILEGED_ACCESS_APPROVED", approverKeycloakSub, null, null,
            "PrivilegedAccessRequest", request.getId().toString(), request.getOrganizationId(),
            "Approved " + request.getPermissionName() + " for customer " + request.getRequesterCustomerId());
        notifyRequester(request, "Your privileged access request was approved",
            "Your request for " + request.getPermissionName() + " was approved and is now active.");
        return toDto(request);
    }

    @Transactional
    public PrivilegedAccessRequestDto reject(String approverKeycloakSub, Long requestId,
                                              RoleScope expectedScope, Long expectedOrganizationId, String note) {
        PrivilegedAccessRequest request = requirePendingRequestInScope(requestId, expectedScope, expectedOrganizationId);
        if (approverKeycloakSub.equals(request.getRequesterKeycloakSub())) {
            throw new ForbiddenException("You cannot reject your own request.");
        }
        request.setStatus(PrivilegedAccessStatus.REJECTED);
        request.setDecidedAt(Instant.now());
        request.setDecidedByKeycloakSub(approverKeycloakSub);
        request.setDecisionNote(note);
        request = requestRepository.save(request);

        audit(request.getId(), PrivilegedAccessEventType.REJECTED, approverKeycloakSub, note);
        auditService.recordSuccess("PRIVILEGED_ACCESS_REJECTED", approverKeycloakSub, null, null,
            "PrivilegedAccessRequest", request.getId().toString(), request.getOrganizationId(),
            "Rejected " + request.getPermissionName() + " for customer " + request.getRequesterCustomerId());
        notifyRequester(request, "Your privileged access request was rejected",
            "Your request for " + request.getPermissionName() + " was rejected."
                + (note != null && !note.isBlank() ? " Reason: " + note : ""));
        return toDto(request);
    }

    private void notifyRequester(PrivilegedAccessRequest request, String title, String message) {
        customerRepository.findById(request.getRequesterCustomerId()).ifPresent(requester ->
            notificationService.notify(requester.getId(), requester.getEmail(),
                NotificationCategory.PRIVILEGED_ACCESS, NotificationSeverity.INFO, title, message));
    }

    /** A requester withdrawing their own request/grant early — looked up by
     * id alone (no scope/org known or needed here, unlike {@link #revoke}),
     * with ownership itself as the only authorization check: only the
     * person who created the request may call this, regardless of whether
     * it turned out to be PLATFORM or ORGANIZATION scope. */
    @Transactional
    public PrivilegedAccessRequestDto revokeOwn(String requesterKeycloakSub, Long requestId, String note) {
        PrivilegedAccessRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Privileged access request not found"));
        if (!requesterKeycloakSub.equals(request.getRequesterKeycloakSub())) {
            throw new ForbiddenException("You do not have permission to do this");
        }
        boolean revocable = request.getStatus() == PrivilegedAccessStatus.PENDING || request.isActiveGrant(Instant.now());
        if (!revocable) {
            throw new IllegalArgumentException("This request is no longer active.");
        }
        request.setStatus(PrivilegedAccessStatus.REVOKED);
        request.setDecidedAt(request.getDecidedAt() == null ? Instant.now() : request.getDecidedAt());
        request = requestRepository.save(request);

        audit(request.getId(), PrivilegedAccessEventType.REVOKED, requesterKeycloakSub, note);
        return toDto(request);
    }

    /**
     * Revoking as an approver, early-ending someone else's PENDING request
     * or active grant — the caller MUST have already verified
     * {@code approverKeycloakSub} holds standing authority for
     * ({@code expectedScope}, {@code expectedOrganizationId}) before calling
     * this (same placement rule as {@link #approve}/{@link #reject}); see
     * {@link #revokeOwn} for a requester withdrawing their own request instead.
     */
    @Transactional
    public PrivilegedAccessRequestDto revoke(String approverKeycloakSub, Long requestId,
                                              RoleScope expectedScope, Long expectedOrganizationId, String note) {
        PrivilegedAccessRequest request = requestRepository.findByIdAndScope(requestId, expectedScope)
            .orElseThrow(() -> new ResourceNotFoundException("Privileged access request not found"));
        if (expectedOrganizationId != null && !expectedOrganizationId.equals(request.getOrganizationId())) {
            throw new ForbiddenException("That request does not belong to your organization");
        }
        boolean revocable = request.getStatus() == PrivilegedAccessStatus.PENDING
            || request.isActiveGrant(Instant.now());
        if (!revocable) {
            throw new IllegalArgumentException("This request is no longer active.");
        }
        request.setStatus(PrivilegedAccessStatus.REVOKED);
        request.setDecidedAt(request.getDecidedAt() == null ? Instant.now() : request.getDecidedAt());
        request = requestRepository.save(request);

        audit(request.getId(), PrivilegedAccessEventType.REVOKED, approverKeycloakSub, note);
        return toDto(request);
    }

    private PrivilegedAccessRequest requirePendingRequestInScope(Long requestId, RoleScope expectedScope, Long expectedOrganizationId) {
        PrivilegedAccessRequest request = requestRepository.findByIdAndScope(requestId, expectedScope)
            .orElseThrow(() -> new ResourceNotFoundException("Privileged access request not found"));
        if (expectedOrganizationId != null && !expectedOrganizationId.equals(request.getOrganizationId())) {
            throw new ForbiddenException("That request does not belong to your organization");
        }
        if (request.getStatus() != PrivilegedAccessStatus.PENDING) {
            throw new IllegalArgumentException("This request has already been decided.");
        }
        return request;
    }

    // ------------------------------------------------------------------
    // The actual grant checks — called from AuthorizationService's callers,
    // never from AuthorizationService itself (see this class's own javadoc
    // on why, to avoid a circular dependency).
    // ------------------------------------------------------------------

    public boolean hasActiveOrganizationGrant(Long requesterCustomerId, Long organizationId, String permissionName) {
        if (requesterCustomerId == null || organizationId == null) {
            return false;
        }
        return !requestRepository.findByRequesterCustomerIdAndScopeAndOrganizationIdAndPermissionNameAndStatusAndExpiresAtAfter(
            requesterCustomerId, RoleScope.ORGANIZATION, organizationId, permissionName, PrivilegedAccessStatus.APPROVED, Instant.now()
        ).isEmpty();
    }

    public boolean hasActivePlatformGrant(String requesterKeycloakSub, String permissionName) {
        if (requesterKeycloakSub == null) {
            return false;
        }
        return !requestRepository.findByRequesterKeycloakSubAndScopeAndPermissionNameAndStatusAndExpiresAtAfter(
            requesterKeycloakSub, RoleScope.PLATFORM, permissionName, PrivilegedAccessStatus.APPROVED, Instant.now()
        ).isEmpty();
    }

    // ------------------------------------------------------------------
    // Review (Support: "review of privileged access")
    // ------------------------------------------------------------------

    public List<PrivilegedAccessRequestDto> listMyRequests(String requesterKeycloakSub) {
        return requestRepository.findByRequesterKeycloakSubOrderByRequestedAtDesc(requesterKeycloakSub).stream()
            .map(this::toDto)
            .toList();
    }

    public List<PrivilegedAccessRequestDto> listPendingForOrganization(Long organizationId) {
        return requestRepository.findByScopeAndOrganizationIdAndStatusOrderByRequestedAtDesc(
                RoleScope.ORGANIZATION, organizationId, PrivilegedAccessStatus.PENDING).stream()
            .map(this::toDto)
            .toList();
    }

    /** REQ-IAM-004.7: the organization's currently active (approved, unexpired)
     * ORGANIZATION-scope grants, so an approver can revoke one early. */
    public List<PrivilegedAccessRequestDto> listActiveForOrganization(Long organizationId) {
        return requestRepository.findByScopeAndOrganizationIdAndStatusAndExpiresAtAfterOrderByExpiresAtAsc(
                RoleScope.ORGANIZATION, organizationId, PrivilegedAccessStatus.APPROVED, Instant.now()).stream()
            .map(this::toDto)
            .toList();
    }

    /** REQ-IAM-004.7: currently active PLATFORM-scope grants. */
    public List<PrivilegedAccessRequestDto> listActiveForPlatform() {
        return requestRepository.findByScopeAndStatusAndExpiresAtAfterOrderByExpiresAtAsc(
                RoleScope.PLATFORM, PrivilegedAccessStatus.APPROVED, Instant.now()).stream()
            .map(this::toDto)
            .toList();
    }

    public List<PrivilegedAccessRequestDto> listPendingForPlatform() {
        return requestRepository.findByScopeAndStatusOrderByRequestedAtDesc(RoleScope.PLATFORM, PrivilegedAccessStatus.PENDING).stream()
            .map(this::toDto)
            .toList();
    }

    // ------------------------------------------------------------------

    private void audit(Long requestId, PrivilegedAccessEventType eventType, String actorKeycloakSub, String note) {
        PrivilegedAccessAuditEntry entry = new PrivilegedAccessAuditEntry();
        entry.setRequestId(requestId);
        entry.setEventType(eventType);
        entry.setActorKeycloakSub(actorKeycloakSub);
        entry.setOccurredAt(Instant.now());
        entry.setNote(note);
        auditRepository.save(entry);
    }

    private PrivilegedAccessRequestDto toDto(PrivilegedAccessRequest request) {
        String effectiveStatus = request.getStatus() == PrivilegedAccessStatus.APPROVED && !request.isActiveGrant(Instant.now())
            ? "EXPIRED"
            : request.getStatus().name();

        List<PrivilegedAccessAuditEntryDto> auditTrail = auditRepository.findByRequestIdOrderByOccurredAtAsc(request.getId()).stream()
            .map(e -> new PrivilegedAccessAuditEntryDto(e.getEventType(), e.getActorKeycloakSub(), e.getOccurredAt(), e.getNote()))
            .toList();

        return new PrivilegedAccessRequestDto(
            request.getId(),
            request.getScope(),
            request.getOrganizationId(),
            request.getPermissionName(),
            request.getJustification(),
            request.getStatus().name(),
            effectiveStatus,
            request.getRequestedAt(),
            request.getRequestedDurationMinutes(),
            request.getDecidedAt(),
            request.getDecidedByKeycloakSub(),
            request.getDecisionNote(),
            request.getExpiresAt(),
            auditTrail,
            request.getRequesterCustomerId() == null ? null
                : customerRepository.findById(request.getRequesterCustomerId()).map(c -> c.getEmail()).orElse(null)
        );
    }
}
