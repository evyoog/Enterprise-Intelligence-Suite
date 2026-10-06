package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.repository.SearchRow;
import com.vyoog.eisplatform.modules.search.repository.SearchSqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Semantic search (REQ-PRT-003, C70): the query is embedded by the model in
 * ai-service and compared with the passages of public content (pgvector,
 * cosine similarity). A document counts once, with its best passage, and only
 * when that passage reaches the similarity threshold.
 */
@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private final EmbeddingClient embeddingClient;
    private final SearchSqlRepository sqlRepository;
    private final SearchSettings settings;

    /** Throws {@link EmbeddingClient.EmbeddingUnavailableException} when the model does not answer. */
    public List<SearchRow> search(String query, String sourceType) {
        float[] vector = embeddingClient.embedQuery(query);
        Map<Long, SearchRow> best = new LinkedHashMap<>();
        for (SearchRow row : sqlRepository.semanticSearch(vector, sourceType, settings.getSemanticCandidates())) {
            if (row.score() >= settings.getSimilarityThreshold()) {
                best.putIfAbsent(row.documentId(), row);
            }
        }
        return new ArrayList<>(best.values());
    }
}
