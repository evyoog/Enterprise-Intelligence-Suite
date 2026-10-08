package com.vyoog.eisplatform.modules.orghierarchy.repository;

import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgNodeRepository extends JpaRepository<OrgNode, Long> {

    List<OrgNode> findByOrganizationIdOrderBySortOrderAscNameAsc(Long organizationId);

    Optional<OrgNode> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByOrganizationIdAndParentIdIsNull(Long organizationId);
}
