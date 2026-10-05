package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBookmarkRepository extends JpaRepository<KnowledgeBookmark, Long> {

    List<KnowledgeBookmark> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<KnowledgeBookmark> findByCustomerIdAndContentId(Long customerId, Long contentId);

    void deleteByContentId(Long contentId);
}
