package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeVideoRepository;
import com.vyoog.eisplatform.modules.search.dto.SearchIndexStatusDto;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.repository.SearchDocumentRepository;
import com.vyoog.eisplatform.modules.search.service.SearchAdminService;
import com.vyoog.eisplatform.modules.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Knowledge search index management (REQ-KNW-005.10, prompt 6.6): what is
 * indexed, embedding status, last indexing, items not indexed and why, and
 * re-index all or one. The full rebuild of all search stays
 * /admin/search/index/rebuild (MANAGE_SEARCH).
 */
@Service
@RequiredArgsConstructor
public class KnowledgeSearchIndexService {

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeVideoRepository videoRepository;
    private final KnowledgeIndexSource indexSource;
    private final KnowledgeViewService viewService;
    private final SearchDocumentRepository documentRepository;
    private final SearchAdminService searchAdminService;
    private final SearchIndexService searchIndexService;
    private final AuditService auditService;

    public record NotIndexed(Long id, String title, String contentType, String reason) {
    }

    public record Status(long totalContent, long indexable, long indexed, Map<String, Long> indexedByType,
                         long videosWithTranscript, long withChapters, List<NotIndexed> notIndexed,
                         SearchIndexStatusDto search) {
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Status status() {
        List<KnowledgeArticle> all = articleRepository.findAll();
        Map<Long, KnowledgeVideo> videos = videoRepository.findAll().stream()
            .collect(Collectors.toMap(KnowledgeVideo::getContentId, v -> v));
        Map<String, Long> byType = new java.util.LinkedHashMap<>();
        long indexable = 0;
        List<NotIndexed> notIndexed = new java.util.ArrayList<>();
        for (KnowledgeArticle a : all) {
            if (indexSource.indexable(a.getId()).isPresent()) {
                indexable++;
                byType.merge(a.getContentType().name(), 1L, Long::sum);
            } else if (a.getLiveVersionId() != null) {
                notIndexed.add(new NotIndexed(a.getId(), a.getTitle(), a.getContentType().name(), reason(a)));
            }
        }
        long withTranscript = videos.values().stream().filter(v -> v.getTranscript() != null && !v.getTranscript().isBlank()).count();
        long withChapters = videos.values().stream().filter(v -> v.getChapters() != null).count();
        return new Status(all.size(), indexable, documentRepository.countBySourceType(SearchSourceType.KNOWLEDGE), byType,
            withTranscript, withChapters, notIndexed.stream().limit(100).toList(), searchAdminService.status());
    }

    private String reason(KnowledgeArticle a) {
        var live = viewService.filterVisible(List.of(a), new KnowledgeReader(true, null, null, null, Set.of(), true));
        if (live.isEmpty()) {
            return "NOT_LIVE";
        }
        var v = live.get(0).version();
        if (v.getAudience() != com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience.PUBLIC) {
            return "RESTRICTED_AUDIENCE";
        }
        if (v.isRequireProductAccess()) {
            return "PRODUCT_ACCESS_ONLY";
        }
        return "OUTSIDE_DATES";
    }

    public int reindexAll(KnowledgeActor actor) {
        Set<SearchIndexService.SourceKey> keys = articleRepository.findAll().stream()
            .map(a -> new SearchIndexService.SourceKey(SearchSourceType.KNOWLEDGE, a.getId()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        searchIndexService.reindex(keys);
        auditService.recordSuccess("KNOWLEDGE_SEARCH_REINDEXED", actor.keycloakSub(), null, actor.email(),
            "KnowledgeSearchIndex", null, null, "Re-indexed " + keys.size() + " knowledge item(s)");
        return keys.size();
    }

    public void reindexOne(KnowledgeActor actor, Long contentId) {
        if (!articleRepository.existsById(contentId)) {
            throw new ResourceNotFoundException("Content not found");
        }
        searchIndexService.reindex(Set.of(new SearchIndexService.SourceKey(SearchSourceType.KNOWLEDGE, contentId)));
        auditService.recordSuccess("KNOWLEDGE_SEARCH_REINDEXED", actor.keycloakSub(), null, actor.email(),
            "KnowledgeContent", contentId.toString(), null, "Re-indexed one item");
    }
}
