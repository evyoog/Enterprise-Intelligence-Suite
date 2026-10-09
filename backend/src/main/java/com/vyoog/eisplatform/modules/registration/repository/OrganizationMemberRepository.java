package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    List<OrganizationMember> findByOrganizationId(Long organizationId);

    /** Every membership of a person (REQ-INT-003: the organizations whose tools must hear that the person changed). */
    List<OrganizationMember> findByCustomerId(Long customerId);

    long countByOrganizationIdAndStatus(Long organizationId, MembershipStatus status);

    /** Phase 3 (2026.3.3): used to refuse demoting the last active ORG_ADMIN
     * of an organization — see OrganizationSelfService#changeMemberRole. */
    long countByOrganizationIdAndOrgRoleAndStatus(Long organizationId, OrgRole orgRole, MembershipStatus status);

    Optional<OrganizationMember> findByOrganizationIdAndCustomerId(Long organizationId, Long customerId);

    /** 09.01 Order Management (sprint 2027.1.1): who to notify when a member
     * submits an order for their organization. */
    List<OrganizationMember> findByOrganizationIdAndOrgRoleAndStatus(Long organizationId, OrgRole orgRole, MembershipStatus status);

    /** REQ-SUB-004: the organizations a customer administers (renewal recipients). */
    List<OrganizationMember> findByCustomerIdAndOrgRoleAndStatus(Long customerId, OrgRole orgRole, MembershipStatus status);

    /** A person can belong to at most one organization today (no multi-org
     * membership in this phase) — used to resolve "which org is the caller
     * an ORG_ADMIN of" from their own customer id. */
    Optional<OrganizationMember> findFirstByCustomerIdAndStatus(Long customerId, MembershipStatus status);

    /** REQ-TEN-006: members placed on an organization-hierarchy node. */
    List<OrganizationMember> findByOrgNodeId(Long orgNodeId);

    long countByOrgNodeId(Long orgNodeId);
}
