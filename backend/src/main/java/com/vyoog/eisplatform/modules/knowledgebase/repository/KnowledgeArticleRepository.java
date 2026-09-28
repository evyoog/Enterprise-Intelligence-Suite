package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByStatusOrderByUpdatedAtDesc(ArticleStatus status);

    List<KnowledgeArticle> findAllByOrderByUpdatedAtDesc();

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.status = 'PUBLISHED' "
        + "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(a.body) LIKE LOWER(CONCAT('%', :q, '%'))) "
        + "ORDER BY a.updatedAt DESC")
    List<KnowledgeArticle> searchPublished(@Param("q") String query);
}
