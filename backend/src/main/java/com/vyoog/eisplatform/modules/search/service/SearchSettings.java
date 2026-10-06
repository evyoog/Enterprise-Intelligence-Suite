package com.vyoog.eisplatform.modules.search.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Search settings (C70), from {@code app.search.*} in application.yml. The
 * thresholds and chunk size were tuned with the quality test set — see
 * docs/02-requirements/FRD/semantic-search/quality-report.md. The embedding
 * service address is a model setting and comes only from
 * {@code EMBEDDING_SERVICE_URL} in config/secrets.env.
 */
@Component
@Getter
public class SearchSettings {

    /** Fill the index on startup when it is empty. */
    @Value("${app.search.backfill-on-startup:true}")
    private boolean backfillOnStartup;

    @Value("${app.search.result-limit:50}")
    private int resultLimit;

    /** Title word-similarity (pg_trgm) a typo match needs. */
    @Value("${app.search.typo-threshold:0.4}")
    private double typoThreshold;

    /** Similarity (pg_trgm) a word needs to be offered as "Did you mean". */
    @Value("${app.search.correction-threshold:0.45}")
    private double correctionThreshold;

    @Value("${app.search.semantic.service-url:}")
    private String embeddingServiceUrl;

    @Value("${app.search.semantic.timeout-ms:400}")
    private int embeddingTimeoutMs;

    /** Cosine similarity a passage needs to count as a semantic match. */
    @Value("${app.search.semantic.similarity-threshold:0.45}")
    private double similarityThreshold;

    @Value("${app.search.semantic.chunk-size:600}")
    private int chunkSize;

    @Value("${app.search.semantic.chunk-overlap:100}")
    private int chunkOverlap;

    /** Reciprocal-rank-fusion constant for merging keyword and semantic rankings. */
    @Value("${app.search.semantic.rrf-k:60}")
    private int rrfK;

    /** Passages fetched from the vector index per query. */
    @Value("${app.search.semantic.candidates:40}")
    private int semanticCandidates;

    /** The vector size the search_chunk table is built for. */
    public static final int EMBEDDING_DIMENSION = 384;

    public boolean embeddingConfigured() {
        return embeddingServiceUrl != null && !embeddingServiceUrl.isBlank();
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("resultLimit", resultLimit);
        map.put("typoThreshold", typoThreshold);
        map.put("correctionThreshold", correctionThreshold);
        map.put("similarityThreshold", similarityThreshold);
        map.put("chunkSize", chunkSize);
        map.put("chunkOverlap", chunkOverlap);
        map.put("rrfK", rrfK);
        map.put("semanticCandidates", semanticCandidates);
        map.put("embeddingTimeoutMs", embeddingTimeoutMs);
        map.put("embeddingDimension", EMBEDDING_DIMENSION);
        return map;
    }
}
