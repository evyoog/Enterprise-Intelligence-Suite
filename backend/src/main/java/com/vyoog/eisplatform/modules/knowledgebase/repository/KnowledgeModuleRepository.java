package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeModuleRepository extends JpaRepository<KnowledgeModule, Long> {

    List<KnowledgeModule> findByProductIdOrderByDisplayOrderAscNameAsc(Long productId);

    List<KnowledgeModule> findAllByOrderByProductIdAscDisplayOrderAscNameAsc();

    boolean existsByProductIdAndSlug(Long productId, String slug);

    long countByProductId(Long productId);
}
