package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeSummaryDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import com.vyoog.eisplatform.modules.search.dto.GlobalSearchResultDto;
import com.vyoog.eisplatform.modules.search.dto.SearchResultItemDto;
import com.vyoog.eisplatform.modules.search.service.GlobalSearchService;
import com.vyoog.eisplatform.modules.search.service.SearchInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Knowledge Center search (REQ-KNW-005.10, C76). Reuses the platform search
 * (REQ-PRT-002 keyword + REQ-PRT-003 hybrid) for public knowledge — no second
 * engine or vector store. Restricted content (signed-in, organization,
 * admin audiences or product access) is never in the shared index; it is
 * matched here only among items the reader may already see. Every hit is
 * checked against BR-KVS-001 before it is returned, so nothing the reader
 * may not see is ranked or returned.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeSearchService {

    public static final String SCOPE = "KNOWLEDGE";

    private final GlobalSearchService globalSearchService;
    private final SearchInsightsService insightsService;
    private final KnowledgeViewService viewService;
    private final KnowledgeProductRepository productRepository;
    private final KnowledgeModuleRepository moduleRepository;

    public KnowledgeReaderDtos.SearchResult search(KnowledgeReader reader, String query, KnowledgeContentType type,
                                                   List<KnowledgeReaderDtos.ProductCard> products) {
        long started = System.nanoTime();
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            return new KnowledgeReaderDtos.SearchResult(q, List.of(), Map.of(), List.of(), List.of(), null,
                "NOT_APPLICABLE", 0);
        }
        List<KnowledgeViewService.Live> visible = viewService.visible(reader, type == null ? null : List.of(type));
        Map<Long, KnowledgeViewService.Live> byId = visible.stream()
            .collect(Collectors.toMap(l -> l.item().getId(), Function.identity()));

        GlobalSearchResultDto platform = globalSearchService.search(q, "KNOWLEDGE", reader.customerId(),
            GlobalSearchService.Mode.HYBRID, 100, false);
        Set<Long> ordered = new LinkedHashSet<>();
        for (SearchResultItemDto hit : platform.results()) {
            if ("KNOWLEDGE".equals(hit.type()) && byId.containsKey(hit.id())) {
                ordered.add(hit.id());
            }
        }
        List<String> words = words(q);
        for (KnowledgeViewService.Live l : visible) {
            if (!ordered.contains(l.item().getId()) && restricted(l) && matchesAll(l, words)) {
                ordered.add(l.item().getId());
            }
        }
        List<KnowledgeViewService.Live> hits = ordered.stream().map(byId::get).toList();
        List<KnowledgeSummaryDto> results = viewService.summaries(hits);
        Map<KnowledgeContentType, List<KnowledgeSummaryDto>> byType = new EnumMap<>(KnowledgeContentType.class);
        for (KnowledgeSummaryDto r : results) {
            byType.computeIfAbsent(r.contentType(), k -> new ArrayList<>()).add(r);
        }
        List<KnowledgeReaderDtos.ProductCard> productHits = type != null ? List.of() : products.stream()
            .filter(p -> anyWord(p.name() + " " + (p.description() == null ? "" : p.description()), words)).toList();
        List<KnowledgeReaderDtos.ModuleHit> moduleHits = type != null ? List.of() : moduleHits(words);
        long tookMs = (System.nanoTime() - started) / 1_000_000;
        int total = results.size() + productHits.size() + moduleHits.size();
        insightsService.record(q, total, "HYBRID", "USED".equals(platform.semanticStatus()), tookMs, SCOPE);
        return new KnowledgeReaderDtos.SearchResult(q, results, byType, productHits, moduleHits,
            platform.didYouMean(), platform.semanticStatus(), tookMs);
    }

    private List<KnowledgeReaderDtos.ModuleHit> moduleHits(List<String> words) {
        Map<Long, KnowledgeProduct> products = new LinkedHashMap<>();
        productRepository.findAll().stream().filter(KnowledgeProduct::isActive).forEach(p -> products.put(p.getId(), p));
        List<KnowledgeReaderDtos.ModuleHit> hits = new ArrayList<>();
        for (KnowledgeModule m : moduleRepository.findAll()) {
            KnowledgeProduct p = products.get(m.getProductId());
            if (p != null && m.isActive() && anyWord(m.getName(), words)) {
                hits.add(new KnowledgeReaderDtos.ModuleHit(m.getId(), m.getName(), m.getSlug(), p.getName(), p.getSlug()));
            }
        }
        return hits.stream().limit(12).toList();
    }

    /** Content that is not in the shared (public) search index. */
    static boolean restricted(KnowledgeViewService.Live l) {
        return l.version().getAudience() != KnowledgeAudience.PUBLIC || l.version().isRequireProductAccess();
    }

    private static boolean matchesAll(KnowledgeViewService.Live l, List<String> words) {
        if (words.isEmpty()) {
            return false;
        }
        String text = fold(l.version().getTitle() + " " + l.version().getBody() + " "
            + (l.version().getSearchText() == null ? "" : l.version().getSearchText()));
        return words.stream().allMatch(text::contains);
    }

    private static boolean anyWord(String text, List<String> words) {
        String folded = fold(text);
        return words.stream().filter(w -> w.length() >= 3).anyMatch(w -> folded.matches("(?s).*\\b" + java.util.regex.Pattern.quote(w) + ".*"));
    }

    static List<String> words(String q) {
        return java.util.Arrays.stream(fold(q).split("[^\\p{L}\\p{N}]+")).filter(w -> !w.isBlank()).distinct().toList();
    }

    static String fold(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
