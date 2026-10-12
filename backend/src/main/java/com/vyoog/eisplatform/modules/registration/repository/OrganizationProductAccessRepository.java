package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrganizationProductAccessRepository extends JpaRepository<OrganizationProductAccess, Long> {

    List<OrganizationProductAccess> findByOrganizationMemberId(Long organizationMemberId);

    Optional<OrganizationProductAccess> findByOrganizationMemberIdAndProductId(Long organizationMemberId, Long productId);

    boolean existsByProductId(Long productId);

    /** Phase 19: one query for every member's access across the whole
     * organization, instead of one findByOrganizationMemberId call per
     * member — see BusinessDashboardService. */
    List<OrganizationProductAccess> findByOrganizationMemberIdIn(Collection<Long> organizationMemberIds);

    /** REQ-TEN-005 (AM-3 B, named seats): ACTIVE access rows to one product held by ACTIVE members of one organization. */
    @Query("select count(a) from OrganizationProductAccess a, OrganizationMember m where a.organizationMemberId = m.id "
        + "and m.organizationId = :organizationId and m.status = :memberStatus and a.productId = :productId and a.status = :accessStatus")
    long countSeatsInUse(@Param("organizationId") Long organizationId, @Param("productId") Long productId,
                         @Param("memberStatus") MembershipStatus memberStatus, @Param("accessStatus") MembershipStatus accessStatus);
}
