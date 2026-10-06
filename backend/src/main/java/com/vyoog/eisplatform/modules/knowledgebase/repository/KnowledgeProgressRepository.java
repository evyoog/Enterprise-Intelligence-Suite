package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeProgressRepository extends JpaRepository<KnowledgeProgress, Long> {

    List<KnowledgeProgress> findTop20ByCustomerIdOrderByLastViewedAtDesc(Long customerId);

    Optional<KnowledgeProgress> findByCustomerIdAndContentId(Long customerId, Long contentId);

    void deleteByContentId(Long contentId);
}
