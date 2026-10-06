package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.repository.SearchRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Merges keyword and semantic rankings (REQ-PRT-003 BR-SEM-004) with
 * reciprocal rank fusion: a result scores 1/(k + rank) in each list it
 * appears in, and the scores add up. Exact-ID matches stay first. A result
 * found by both keeps its keyword match label; one found only by meaning is
 * labelled "Similar meaning".
 */
final class HybridMerger {

    record Merged(SearchRow row, double score, String passage) {
    }

    private HybridMerger() {
    }

    static List<Merged> merge(List<SearchRow> keyword, List<SearchRow> semantic, int k, int limit) {
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, SearchRow> rows = new LinkedHashMap<>();
        Map<Long, String> passages = new HashMap<>();
        for (int i = 0; i < keyword.size(); i++) {
            SearchRow row = keyword.get(i);
            rows.put(row.documentId(), row);
            scores.merge(row.documentId(), 1.0 / (k + i + 1), Double::sum);
        }
        for (int i = 0; i < semantic.size(); i++) {
            SearchRow row = semantic.get(i);
            rows.putIfAbsent(row.documentId(), row);
            passages.putIfAbsent(row.documentId(), row.passage());
            scores.merge(row.documentId(), 1.0 / (k + i + 1), Double::sum);
        }
        List<Merged> merged = new ArrayList<>();
        rows.forEach((id, row) -> merged.add(new Merged(row, scores.get(id), passages.get(id))));
        merged.sort(Comparator.comparing((Merged m) -> m.row().tier() == 1 ? 0 : 1)
            .thenComparing(Merged::score, Comparator.reverseOrder()));
        return merged.size() > limit ? merged.subList(0, limit) : merged;
    }
}
