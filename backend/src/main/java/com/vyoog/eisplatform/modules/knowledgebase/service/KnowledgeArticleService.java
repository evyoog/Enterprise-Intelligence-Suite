package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 11.01.01 Knowledge Articles (sprint 2027.1.1) — platform-admin-managed
 * (MANAGE_KNOWLEDGE_BASE), readable by anyone once published. See
 * {@link KnowledgeArticle}'s own javadoc for why 11.01.02 AI Knowledge isn't
 * built here.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;
    private final AuditService auditService;

    @Transactional
    public KnowledgeArticleDto createArticle(KnowledgeArticleRequest request) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle(request.title());
        article.setBody(request.body());
        article.setStatus(ArticleStatus.DRAFT);
        article.setVersion(1);
        article = articleRepository.save(article);
        auditService.recordSuccess("KNOWLEDGE_ARTICLE_CREATED", null, null, null,
            "KnowledgeArticle", article.getId().toString(), null, "Article created: " + article.getTitle());
        return toDto(article);
    }

    @Transactional
    public KnowledgeArticleDto editArticle(Long articleId, KnowledgeArticleRequest request) {
        KnowledgeArticle article = findById(articleId);
        article.setTitle(request.title());
        article.setBody(request.body());
        article.setVersion(article.getVersion() + 1);
        article = articleRepository.save(article);
        auditService.recordSuccess("KNOWLEDGE_ARTICLE_EDITED", null, null, null,
            "KnowledgeArticle", articleId.toString(), null, "Article edited, now version " + article.getVersion());
        return toDto(article);
    }

    @Transactional
    public KnowledgeArticleDto setPublished(Long articleId, boolean published) {
        KnowledgeArticle article = findById(articleId);
        article.setStatus(published ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT);
        article = articleRepository.save(article);
        auditService.recordSuccess(published ? "KNOWLEDGE_ARTICLE_PUBLISHED" : "KNOWLEDGE_ARTICLE_UNPUBLISHED", null, null, null,
            "KnowledgeArticle", articleId.toString(), null, "Article " + (published ? "published" : "unpublished"));
        return toDto(article);
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        KnowledgeArticle article = findById(articleId);
        articleRepository.delete(article);
        auditService.recordSuccess("KNOWLEDGE_ARTICLE_DELETED", null, null, null,
            "KnowledgeArticle", articleId.toString(), null, "Article deleted: " + article.getTitle());
    }

    /** Admin view — every article regardless of status. */
    public List<KnowledgeArticleDto> listAll() {
        return articleRepository.findAllByOrderByUpdatedAtDesc().stream().map(this::toDto).toList();
    }

    public KnowledgeArticleDto getForAdmin(Long articleId) {
        return toDto(findById(articleId));
    }

    /** 11.01.01.04 Search article — public, PUBLISHED only. Blank/absent
     * query returns every published article, newest first. */
    public List<KnowledgeArticleDto> searchPublished(String query) {
        List<KnowledgeArticle> articles = (query == null || query.isBlank())
            ? articleRepository.findByStatusOrderByUpdatedAtDesc(ArticleStatus.PUBLISHED)
            : articleRepository.searchPublished(query.trim());
        return articles.stream().map(this::toDto).toList();
    }

    /** Public article detail — refuses (404) a DRAFT article the same way a
     * nonexistent id would, never confirming unpublished content exists. */
    public KnowledgeArticleDto getPublished(Long articleId) {
        KnowledgeArticle article = articleRepository.findById(articleId)
            .filter(a -> a.getStatus() == ArticleStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        return toDto(article);
    }

    private KnowledgeArticle findById(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    private KnowledgeArticleDto toDto(KnowledgeArticle article) {
        return new KnowledgeArticleDto(article.getId(), article.getTitle(), article.getBody(),
            article.getStatus(), article.getVersion(), article.getUpdatedAt());
    }
}
