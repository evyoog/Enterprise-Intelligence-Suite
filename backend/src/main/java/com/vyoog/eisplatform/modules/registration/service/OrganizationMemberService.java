package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Seats are organization-wide, counted purely by how many
 * {@link OrganizationMember} rows are ACTIVE for that org — never per
 * product. This is the one place that rule is enforced (called from
 * RegistrationService for the first ORG_ADMIN, and would be called again by
 * a future invitation-acceptance flow — see this project's own deferred-work
 * notes) so it can never be bypassed by adding a member some other way.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationMemberService {

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final ProductSubscriptionRepository subscriptionRepository;

    /** Throws if the organization has no free seat left. Callers must call
     * this immediately before inserting the new ACTIVE member row, inside
     * the same transaction, so a concurrent add can't both pass the check. */
    public void assertSeatAvailable(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        // 05.02.01.05 Configure tenant policies (sprint 2026.4.2, carried from
        // 2026.4.1): a platform-admin-only override, set on the organization
        // itself — see Organization#allowSeatOverage's own javadoc.
        if (organization.isAllowSeatOverage()) {
            return;
        }
        long activeCount = memberRepository.countByOrganizationIdAndStatus(organizationId, MembershipStatus.ACTIVE);
        if (activeCount >= organization.getLicensedSeats()) {
            throw new SeatLimitExceededException(
                "All " + organization.getLicensedSeats() + " licensed seats are in use. "
                    + "Ask your Vyoog account manager to increase your seat limit, or free a seat first."
            );
        }
        // REQ-SUB-003.4 (C63, BR-4): every ACTIVE organization subscription's
        // seat quantity is also a limit on the pool of active members.
        subscriptionRepository.findByOwnerOrganizationId(organizationId).stream()
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
            .filter(s -> activeCount >= s.getQuantity())
            .findFirst()
            .ifPresent(s -> {
                throw new SeatLimitExceededException("All " + s.getQuantity() + " seats of a subscription are in use. "
                    + "Add seats to the subscription, or free a seat first.");
            });
    }

    public OrganizationMember addMember(Long organizationId, Long customerId, OrgRole orgRole) {
        assertSeatAvailable(organizationId);
        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(organizationId);
        member.setCustomerId(customerId);
        member.setOrgRole(orgRole);
        member.setStatus(MembershipStatus.ACTIVE);
        member.setJoinedAt(Instant.now());
        return memberRepository.save(member);
    }

    /** Never deletes the row — flips it INACTIVE and frees the seat, keeping
     * history and every past product-access assignment for audit purposes.
     * One-way: see MembershipStatus's own javadoc. */
    public void removeMember(Long organizationMemberId) {
        OrganizationMember member = memberRepository.findById(organizationMemberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        member.setStatus(MembershipStatus.INACTIVE);
        member.setDeactivatedAt(Instant.now());
        memberRepository.save(member);
    }

    /** 05.03.01 Suspend user (sprint 2026.4.1): frees the seat like
     * {@link #removeMember} but is reversible — see {@link #reactivateMember}. */
    public OrganizationMember suspendMember(Long organizationMemberId) {
        OrganizationMember member = memberRepository.findById(organizationMemberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        member.setStatus(MembershipStatus.SUSPENDED);
        member.setDeactivatedAt(Instant.now());
        return memberRepository.save(member);
    }

    /** 05.03.01 Activate user (sprint 2026.4.1): re-admits a SUSPENDED member,
     * subject to the same seat limit as adding a brand-new one. Refuses to
     * reactivate an INACTIVE (removed) member — that path is deliberately
     * one-way; adding them back is a new {@link #addMember} instead. */
    public OrganizationMember reactivateMember(Long organizationMemberId) {
        OrganizationMember member = memberRepository.findById(organizationMemberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (member.getStatus() == MembershipStatus.INACTIVE) {
            throw new IllegalArgumentException("A removed member cannot be reactivated.");
        }
        assertSeatAvailable(member.getOrganizationId());
        member.setStatus(MembershipStatus.ACTIVE);
        member.setDeactivatedAt(null);
        return memberRepository.save(member);
    }

    /**
     * Lowering a seat limit below the current active-member count is allowed
     * (an org shrinking its plan is a real thing) but this deliberately never
     * auto-deactivates anyone to force back under the new limit — the org
     * admin must free a seat manually. This method just reports whether that
     * over-limit state now exists; it changes nothing by itself.
     */
    public boolean isOverLimit(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        long activeCount = memberRepository.countByOrganizationIdAndStatus(organizationId, MembershipStatus.ACTIVE);
        return activeCount > organization.getLicensedSeats();
    }

    public List<OrganizationMember> listActiveMembers(Long organizationId) {
        return memberRepository.findByOrganizationId(organizationId).stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .toList();
    }
}
