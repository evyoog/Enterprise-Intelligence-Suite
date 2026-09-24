package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    List<OrganizationMember> findByOrganizationId(Long organizationId);

    long countByOrganizationIdAndStatus(Long organizationId, MembershipStatus status);

    /** Phase 3 (2026.3.3): used to refuse demoting the last active ORG_ADMIN
     * of an organization — see OrganizationSelfService#changeMemberRole. */
    long countByOrganizationIdAndOrgRoleAndStatus(Long organizationId, OrgRole orgRole, MembershipStatus status);

    Optional<OrganizationMember> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId);

    /** A person can belong to at most one organization today (no multi-org
     * membership in this phase) — used to resolve "which org is the caller
     * an ORG_ADMIN of" from their own customer id. */
    Optional<OrganizationMember> findFirstByCustomerIdAndStatus(Long customerId, MembershipStatus status);
}
