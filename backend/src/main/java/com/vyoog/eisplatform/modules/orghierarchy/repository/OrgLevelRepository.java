package com.vyoog.eisplatform.modules.orghierarchy.repository;

import com.vyoog.eisplatform.modules.orghierarchy.model.OrgLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgLevelRepository extends JpaRepository<OrgLevel, Long> {

    List<OrgLevel> findByOrganizationIdOrderByLevelRank(Long organizationId);
}
