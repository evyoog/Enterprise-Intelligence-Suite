package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long>,
    JpaSpecificationExecutor<KnowledgeArticle> {

    List<KnowledgeArticle> findByStatusOrderByUpdatedAtDesc(ArticleStatus status);

    List<KnowledgeArticle> findAllByOrderByUpdatedAtDesc();

    List<KnowledgeArticle> findByContentTypeOrderByUpdatedAtDesc(KnowledgeContentType contentType);

    /** Every item with a live version (readers' candidate set; BR-KVS-001 is
     * applied on top of this, before anything is ranked or counted). */
    @Query("SELECT a FROM KnowledgeArticle a WHERE a.liveVersionId IS NOT NULL AND a.workflowState <> 'ARCHIVED'")
    List<KnowledgeArticle> findLive();

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.liveVersionId IS NOT NULL AND a.workflowState <> 'ARCHIVED' "
        + "AND a.contentType IN :types")
    List<KnowledgeArticle> findLiveOfTypes(@Param("types") Collection<KnowledgeContentType> types);

    Optional<KnowledgeArticle> findFirstByContentTypeAndSlug(KnowledgeContentType contentType, String slug);

    Optional<KnowledgeArticle> findFirstBySlug(String slug);

    boolean existsByContentTypeAndSlugAndIdNot(KnowledgeContentType contentType, String slug, Long id);

    boolean existsByContentTypeAndSlug(KnowledgeContentType contentType, String slug);

    List<KnowledgeArticle> findByWorkflowStateAndScheduledAtLessThanEqual(KnowledgeWorkflowState state, Instant time);

    long countByWorkflowState(KnowledgeWorkflowState state);

    long countByProductId(Long productId);

    long countByModuleId(Long moduleId);

    long countByCategoryId(Long categoryId);

    @Query("SELECT a.contentType, count(a) FROM KnowledgeArticle a GROUP BY a.contentType")
    List<Object[]> countByType();
}
