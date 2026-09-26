package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.OrganizationGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationGroupMemberRepository extends JpaRepository<OrganizationGroupMember, Long> {

    List<OrganizationGroupMember> findByGroupId(Long groupId);

    Optional<OrganizationGroupMember> findByGroupIdAndOrganizationMemberId(Long groupId, Long organizationMemberId);

    void deleteByGroupId(Long groupId);

    long countByGroupId(Long groupId);
}
