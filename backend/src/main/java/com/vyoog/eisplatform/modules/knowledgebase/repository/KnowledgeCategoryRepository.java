package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeCategoryRepository extends JpaRepository<KnowledgeCategory, Long> {

    List<KnowledgeCategory> findAllByOrderByScopeAscDisplayOrderAscNameAsc();

    boolean existsBySlug(String slug);
}
