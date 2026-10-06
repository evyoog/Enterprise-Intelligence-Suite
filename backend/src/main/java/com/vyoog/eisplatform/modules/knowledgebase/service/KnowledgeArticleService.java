package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 11.01.01 Knowledge Articles — the original endpoints (REQ-KNW-001), kept
 * with the same request and response shapes (REQ-KNW-001.8) on top of the
 * content model (REQ-KNW-002):
 * <ul>
 *   <li>articles are content items of type ARTICLE with a Public audience;</li>
 *   <li>publish/unpublish stays a plain toggle (BR-KNW-003) — publishing
 *       records a version snapshot, and an edit of a published article
 *       goes live at once as a new minor version, as before;</li>
 *   <li>the public reads return only Published, Public ARTICLE items
 *       (BR-KNW-008), from the live version.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeContentService contentService;
    private final KnowledgeViewService viewService;
    private final AuditService auditService;

    @Transactional
    public KnowledgeArticleDto createArticle(KnowledgeArticleRequest request) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setContentType(KnowledgeContentType.ARTICLE);
        article.setTitle(request.title());
        article.setBody(request.body());
        article.setBlocks(KnowledgeBlocks.paragraph(request.body()));
        article.setAudience(KnowledgeAudience.PUBLIC);
        article.setStatus(ArticleStatus.DRAFT);
        article.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        article.setVersion(1);
        article = articleRepository.save(article);
        article.setSlug(KnowledgeContentService.slugify(article.getTitle()) + "-" + article.getId());
        contentService.refreshSearchText(article);
        auditService.recordSuccess("KNOWLEDGE_ARTICLE_CREATED", null, null, null,
            "KnowledgeArticle", article.getId().toString(), null, "Article created: " + article.getTitle());
        return toDto(articleRepository.save(article));
    }

    @Transactional
    public KnowledgeArticleDto editArticle(Long articleId, KnowledgeArticleRequest request) {
        KnowledgeArticle article = findById(articleId);
        article.setTitle(request.title());
        article.setBody(request.body());
        article.setBlocks(KnowledgeBlocks.paragraph(request.body()));
        article.setVersion(article.getVersion() + 1);
        contentService.refreshSearchText(article);
        if (article.getLiveVersionId() != null) {
            // Old behaviour: an edit of a published article is live at once.
            contentService.publishNow(article, false, null);
        } else {
            article = articleRepository.save(article);
        }
        auditService.recordSuccess("KNOWLEDGE_ARTICLE_EDITED", null, null, null,
            "KnowledgeArticle", articleId.toString(), null, "Article edited, now version " + article.getVersion());
        return toDto(article);
    }

    @Transactional
    public KnowledgeArticleDto setPublished(Long articleId, boolean published) {
        KnowledgeArticle article = findById(articleId);
        if (published) {
            if (article.getLiveVersionId() == null || article.getWorkflowState() != KnowledgeWorkflowState.PUBLISHED) {
                contentService.publishNow(article, false, null);
            }
        } else {
            article.setLiveVersionId(null);
            article.setStatus(ArticleStatus.DRAFT);
            article.setWorkflowState(KnowledgeWorkflowState.DRAFT);
            article = articleRepository.save(article);
        }
        auditService.recordSuccess(published ? "KNOWLEDGE_ARTICLE_PUBLISHED" : "KNOWLEDGE_ARTICLE_UNPUBLISHED", null, null, null,
            "KnowledgeArticle", articleId.toString(), null, "Article " + (published ? "published" : "unpublished"));
        return toDto(article);
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        KnowledgeArticle article = findById(articleId);
        contentService.deleteCascade(article, null);
        auditService.recordSuccess("KNOWLEDGE_ARTICLE_DELETED", null, null, null,
            "KnowledgeArticle", articleId.toString(), null, "Article deleted: " + article.getTitle());
    }

    /** Admin view — every ARTICLE regardless of status. */
    public List<KnowledgeArticleDto> listAll() {
        return articleRepository.findByContentTypeOrderByUpdatedAtDesc(KnowledgeContentType.ARTICLE).stream()
            .map(this::toDto).toList();
    }

    public KnowledgeArticleDto getForAdmin(Long articleId) {
        return toDto(findById(articleId));
    }

    /** 11.01.01.04 Search article — public, Published Public ARTICLE items only (BR-KNW-004, BR-KNW-008). */
    public List<KnowledgeArticleDto> searchPublished(String query) {
        return searchPublic(query, Set.of(KnowledgeContentType.ARTICLE));
    }

    /** Public knowledge of every type, for the basic search engine (C76). */
    public List<KnowledgeArticleDto> searchPublicContent(String query) {
        return searchPublic(query, null);
    }

    private List<KnowledgeArticleDto> searchPublic(String query, Set<KnowledgeContentType> types) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return viewService.visible(KnowledgeReader.anonymous(), types).stream()
            .filter(l -> q.isEmpty() || contains(l.version().getTitle(), q) || contains(l.version().getBody(), q)
                || (types == null && contains(l.version().getSearchText(), q)))
            .sorted(Comparator.comparing((KnowledgeViewService.Live l) -> l.item().getUpdatedAt(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(this::toPublicDto)
            .toList();
    }

    /** Public article detail — 404 for anything not Published, Public and of type ARTICLE (BR-KNW-005). */
    public KnowledgeArticleDto getPublished(Long articleId) {
        KnowledgeArticle article = articleRepository.findById(articleId)
            .filter(a -> a.getContentType() == KnowledgeContentType.ARTICLE)
            .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        return viewService.visibleItem(article, KnowledgeReader.anonymous()).map(this::toPublicDto)
            .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    private KnowledgeArticle findById(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    private KnowledgeArticleDto toPublicDto(KnowledgeViewService.Live live) {
        return new KnowledgeArticleDto(live.item().getId(), live.version().getTitle(), live.version().getBody(),
            ArticleStatus.PUBLISHED, live.item().getVersion(), live.item().getUpdatedAt());
    }

    private KnowledgeArticleDto toDto(KnowledgeArticle article) {
        return new KnowledgeArticleDto(article.getId(), article.getTitle(), article.getBody(),
            article.getStatus(), article.getVersion(), article.getUpdatedAt());
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}
