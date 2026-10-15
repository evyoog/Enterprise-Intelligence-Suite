package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.AccessRequest;
import com.vyoog.eisplatform.modules.registration.model.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {

    List<AccessRequest> findByOrganizationMemberIdOrderByCreatedAtDesc(Long organizationMemberId);

    Optional<AccessRequest> findByOrganizationMemberIdAndProductIdAndStatus(Long organizationMemberId, Long productId, AccessRequestStatus status);

    /** Admin list: every request from members of one organization, newest first. */
    @org.springframework.data.jpa.repository.Query("select r from AccessRequest r, OrganizationMember m "
        + "where r.organizationMemberId = m.id and m.organizationId = :organizationId order by r.createdAt desc")
    List<AccessRequest> findByOrganizationIdOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("organizationId") Long organizationId);
}
