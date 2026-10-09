package com.vyoog.eisplatform.modules.registration.model;

import com.vyoog.eisplatform.modules.authorization.model.OrganizationOwnedResource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One row per person per organization — this is what a licensed seat is
 * counted against (see OrganizationMemberService#assertSeatAvailable),
 * regardless of how many products that person is later assigned to. Never
 * deleted on removal — {@link #status} flips to INACTIVE and
 * {@link #deactivatedAt} is stamped, keeping history and freeing the seat.
 *
 * <p>Implements {@link OrganizationOwnedResource} (Phase 5) so
 * {@code AuthorizationService}'s generic resource-based policies can check
 * "does this member belong to the caller's organization" the same way any
 * other tenant-owned resource would, instead of a bespoke comparison.
 */
@Entity
@EntityListeners(com.vyoog.eisplatform.modules.toolsync.tracking.ToolSyncListener.class)
@Table(name = "organization_member")
@Getter
@Setter
public class OrganizationMember implements OrganizationOwnedResource, com.vyoog.eisplatform.modules.toolsync.tracking.ToolSyncAggregate {

    /** REQ-INT-003: per-aggregate version that only goes up (column sync_version, migration V029); see ToolSyncListener. */
    @Column(name = "sync_version", nullable = false)
    private long syncVersion = 1;

    /** The sync_version this row had when it was loaded or last written; never saved. See ToolSyncListener. */
    @jakarta.persistence.Transient
    private Long loadedSyncVersion;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_role", nullable = false, length = 20)
    private OrgRole orgRole = OrgRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();

    private Instant deactivatedAt;

    /** 05.03.02 Review access (sprint 2026.4.1): stamped by
     * OrganizationSelfService#reviewMemberAccess each time an admin
     * confirms this member's role and access are still correct. Purely a
     * record — it does not itself change anything about the member. */
    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "last_reviewed_by_customer_id")
    private Long lastReviewedByCustomerId;

    /** REQ-TEN-006: the member's home node in the organization hierarchy (optional). */
    @Column(name = "org_node_id")
    private Long orgNodeId;

    @Override
    public Long organizationId() {
        return organizationId;
    }
}
