package com.vyoog.eisplatform.modules.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 6 (PAM): "Normal user requests temporary admin access" as a real,
 * persisted row — the target flow's request/approval/grant/expiry lifecycle
 * lives entirely on this one entity plus its immutable
 * {@link PrivilegedAccessAuditEntry} trail, rather than as a temporary Role
 * assignment. Not a new Role: the permission this grants is checked
 * ADDITIONALLY, alongside the caller's real (permanent) Role-derived
 * permissions — see {@code PrivilegedAccessService#hasActiveOrganizationGrant}/
 * {@code #hasActivePlatformGrant} — so "permanent administrator access is
 * not the only mechanism available" without inventing a second role system.
 *
 * <p>Keyed by {@link #requesterKeycloakSub}, not a Customer id, because a
 * PLATFORM-scope requester may have no linked {@code Customer} row at all —
 * the exact same reasoning {@code AuthorizationService}'s own javadoc gives
 * for why platform-role-holding stays Keycloak-derived (see Phase 3).
 * {@link #requesterCustomerId} is populated when available, purely for
 * display and for matching ORGANIZATION-scope grants (which always have one,
 * since org membership implies a linked Customer).
 */
@Entity
@Table(name = "privileged_access_request")
@Getter
@Setter
public class PrivilegedAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_keycloak_sub", nullable = false)
    private String requesterKeycloakSub;

    @Column(name = "requester_customer_id")
    private Long requesterCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleScope scope;

    /** Required for ORGANIZATION scope, always null for PLATFORM scope —
     * never client-supplied (resolved server-side from the requester's own
     * membership), same rule every other org-scoped id in this app follows. */
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "permission_name", nullable = false, length = 60)
    private String permissionName;

    @Column(nullable = false, length = 500)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrivilegedAccessStatus status = PrivilegedAccessStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "requested_duration_minutes", nullable = false)
    private int requestedDurationMinutes;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decided_by_keycloak_sub")
    private String decidedByKeycloakSub;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    /** Set only on approval, to {@code decidedAt + requestedDurationMinutes}.
     * A request that is still PENDING, or was REJECTED/REVOKED, has none. */
    @Column(name = "expires_at")
    private Instant expiresAt;

    public boolean isActiveGrant(Instant now) {
        return status == PrivilegedAccessStatus.APPROVED && expiresAt != null && now.isBefore(expiresAt);
    }
}
