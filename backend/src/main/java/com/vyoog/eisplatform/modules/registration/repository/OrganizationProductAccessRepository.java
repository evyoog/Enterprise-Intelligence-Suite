package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
