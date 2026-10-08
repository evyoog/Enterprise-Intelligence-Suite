package com.vyoog.eisplatform.modules.orghierarchy.repository;

import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNodeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgNodeHistoryRepository extends JpaRepository<OrgNodeHistory, Long> {

    List<OrgNodeHistory> findByOrgNodeIdOrderByEffectiveAtDescIdDesc(Long orgNodeId);

    void deleteByOrgNodeId(Long orgNodeId);
}
