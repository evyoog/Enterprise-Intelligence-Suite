package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.GroupKind;
import com.vyoog.eisplatform.modules.registration.model.OrganizationGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationGroupRepository extends JpaRepository<OrganizationGroup, Long> {

    List<OrganizationGroup> findByOrganizationId(Long organizationId);

    List<OrganizationGroup> findByOrganizationIdAndKind(Long organizationId, GroupKind kind);
}
