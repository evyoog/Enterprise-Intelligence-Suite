package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.IndexableKnowledge;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Feeds the platform search index (C76). Only content a signed-out visitor
 * may see is indexed — Published, within its dates, Public audience, not
 * restricted to product access — so the shared index (and its pgvector
 * passages) never holds restricted content. Restricted content is searched
 * by KnowledgeSearchService among items the reader may already see.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeIndexSource {

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeViewService viewService;

    public Optional<IndexableKnowledge> indexable(Long contentId) {
        return articleRepository.findById(contentId)
            .flatMap(a -> viewService.visibleItem(a, KnowledgeReader.anonymous()))
            .map(l -> new IndexableKnowledge(l.item().getId(), l.version().getTitle(), l.version().getBody(),
                l.version().getSearchText(), keywords(l), l.version().getPublishedAt()));
    }

    private static String keywords(KnowledgeViewService.Live l) {
        StringBuilder sb = new StringBuilder(l.item().getContentType().name().replace('_', ' ').toLowerCase(java.util.Locale.ROOT));
        if (l.item().getTags() != null) {
            sb.append(' ').append(l.item().getTags().replace(',', ' '));
        }
        if (l.item().getKeywords() != null) {
            sb.append(' ').append(l.item().getKeywords());
        }
        return sb.toString();
    }
}
