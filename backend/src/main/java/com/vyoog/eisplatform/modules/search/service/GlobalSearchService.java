package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.dto.GlobalSearchResultDto;
import com.vyoog.eisplatform.modules.search.dto.SearchResultItemDto;
import com.vyoog.eisplatform.modules.search.dto.SearchSuggestionDto;
import com.vyoog.eisplatform.modules.search.repository.SearchCapabilities;
import com.vyoog.eisplatform.modules.search.repository.SearchRow;
import com.vyoog.eisplatform.modules.search.repository.SearchSqlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 01.03.01 Unified Search: keyword search (REQ-PRT-002) and semantic search
 * (REQ-PRT-003), C70. Searches the public catalog, published knowledge
 * articles and — signed in only — the caller's own support tickets.
 *
 * <p>With the PostgreSQL search index installed, keyword search ranks
 * matches (exact ID, exact phrase, English/Spanish word forms, partial words,
 * typos) and, in hybrid mode, merges them with semantic matches from public
 * content. Without the index (or for a blank query) the basic engine runs
 * the matching that existed before C70. If the embedding model does not
 * answer, the keyword results are returned on their own
 * (semanticStatus UNAVAILABLE).
 *
 * <p>Visibility (BR-SRCH-001): every engine applies the caller's visibility
 * before ranking — public records plus the caller's own tickets.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchService {

    public enum Mode { HYBRID, KEYWORD }

    private static final Set<String> TYPES = Set.of("PRODUCT", "KNOWLEDGE", "TICKET");
    private static final int SUGGESTION_LIMIT = 8;

    private final BasicKeywordSearch basicSearch;
    private final PostgresKeywordSearch keywordSearch;
    private final SemanticSearchService semanticSearch;
    private final SearchCapabilityService capabilityService;
    private final SearchSqlRepository sqlRepository;
    private final SearchInsightsService insightsService;
    private final SearchSettings settings;

    public GlobalSearchResultDto search(String query, String type, Long customerId) {
        return search(query, type, customerId, Mode.HYBRID, null, false);
    }

    public GlobalSearchResultDto search(String query, String type, Long customerId, Mode mode, Integer limit,
                                        boolean track) {
        long started = System.nanoTime();
        String sourceType = normalizeType(type);
        if (type != null && !type.isBlank() && sourceType == null) {
            return new GlobalSearchResultDto(List.of(), List.of(), List.of(), List.of(), null, "NOT_APPLICABLE",
                engineName(), elapsed(started));
        }
        int max = limit == null ? settings.getResultLimit() : Math.max(1, Math.min(limit, 100));
        ParsedQuery parsed = QueryParser.parse(query);
        SearchCapabilities capabilities = capabilityService.get();

        GlobalSearchResultDto result;
        if (parsed.isBlank() || !capabilities.keywordIndex()) {
            BasicKeywordSearch.Grouped grouped = basicSearch.search(query, sourceType, customerId);
            List<SearchResultItemDto> all = Stream.of(grouped.products(), grouped.knowledgeArticles(), grouped.tickets())
                .flatMap(List::stream).toList();
            result = new GlobalSearchResultDto(grouped.products(), grouped.knowledgeArticles(), grouped.tickets(),
                all, null, "NOT_APPLICABLE", "BASIC", elapsed(started));
        } else {
            result = indexedSearch(parsed, sourceType, customerId, mode, max, capabilities, started);
        }
        if (track && !parsed.isBlank()) {
            insightsService.record(parsed.raw(), result.results().size(), mode.name(),
                "USED".equals(result.semanticStatus()), result.tookMs());
        }
        return result;
    }

    private GlobalSearchResultDto indexedSearch(ParsedQuery parsed, String sourceType, Long customerId, Mode mode,
                                                int max, SearchCapabilities capabilities, long started) {
        KeywordSearchOutcome keyword = keywordSearch.search(parsed, sourceType, customerId, max);

        String semanticStatus;
        List<SearchRow> semantic = List.of();
        boolean semanticApplies = mode == Mode.HYBRID && !"TICKET".equals(sourceType)
            && parsed.exactReference() == null && !parsed.hasQuotedPhrase();
        if (!semanticApplies) {
            semanticStatus = "NOT_APPLICABLE";
        } else if (!capabilities.semantic() || !settings.embeddingConfigured()) {
            semanticStatus = "DISABLED";
        } else {
            try {
                semantic = semanticSearch.search(parsed.raw(), sourceType);
                semanticStatus = "USED";
            } catch (EmbeddingClient.EmbeddingUnavailableException e) {
                log.debug("Semantic search unavailable, keyword results only: {}", e.getMessage());
                semanticStatus = "UNAVAILABLE";
            } catch (RuntimeException e) {
                log.warn("Semantic search failed, keyword results only: {}", e.getMessage());
                semanticStatus = "UNAVAILABLE";
            }
        }

        List<HybridMerger.Merged> merged = HybridMerger.merge(keyword.rows(), semantic, settings.getRrfK(), max);
        List<SearchResultItemDto> results = new ArrayList<>(merged.size());
        for (HybridMerger.Merged m : merged) {
            results.add(toItem(m, keyword.highlightTerms()));
        }
        return new GlobalSearchResultDto(ofType(results, "PRODUCT"), ofType(results, "KNOWLEDGE"),
            ofType(results, "TICKET"), results, keyword.didYouMean(), semanticStatus, "POSTGRES", elapsed(started));
    }

    /** Type-ahead suggestions: visible records whose title matches what was typed. */
    @Transactional(readOnly = true)
    public List<SearchSuggestionDto> suggest(String query, String type, Long customerId) {
        String folded = String.join(" ", TextFolding.tokens(TextFolding.fold(query == null ? "" : query)));
        if (folded.length() < 2) {
            return List.of();
        }
        String sourceType = normalizeType(type);
        if (capabilityService.get().keywordIndex()) {
            return sqlRepository.suggest(folded, customerId, sourceType, settings.getTypoThreshold(), SUGGESTION_LIMIT)
                .stream().map(r -> new SearchSuggestionDto(r.sourceType(), r.sourceId(), r.title(), r.reference()))
                .toList();
        }
        BasicKeywordSearch.Grouped grouped = basicSearch.search(query, sourceType, customerId);
        return Stream.of(grouped.products(), grouped.knowledgeArticles(), grouped.tickets()).flatMap(List::stream)
            .limit(SUGGESTION_LIMIT)
            .map(i -> new SearchSuggestionDto(i.type(), i.id(), i.title(), i.reference()))
            .toList();
    }

    private SearchResultItemDto toItem(HybridMerger.Merged merged, Set<String> terms) {
        SearchRow row = merged.row();
        String matchType = switch (row.tier()) {
            case 1 -> "EXACT_ID";
            case 2 -> "EXACT_PHRASE";
            case 3 -> "KEYWORD";
            case 4 -> "PARTIAL";
            case 5 -> "TYPO";
            default -> "SEMANTIC";
        };
        String snippet;
        if (row.tier() == 6 && merged.passage() != null) {
            String passage = merged.passage();
            int newline = passage.indexOf('\n');
            snippet = Highlighter.snippet(newline >= 0 ? passage.substring(newline + 1) : passage, terms);
        } else {
            snippet = Highlighter.snippet(row.body(), terms);
        }
        double score = Math.round(merged.score() * 1_000_000d) / 1_000_000d;
        return new SearchResultItemDto(row.sourceType(), row.sourceId(), row.title(), snippet, row.reference(),
            matchType, score, Highlighter.highlight(row.title(), terms), Highlighter.highlight(snippet, terms));
    }

    private static List<SearchResultItemDto> ofType(List<SearchResultItemDto> results, String type) {
        return results.stream().filter(r -> r.type().equals(type)).toList();
    }

    private static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String upper = type.trim().toUpperCase(Locale.ROOT);
        return TYPES.contains(upper) ? upper : null;
    }

    private String engineName() {
        return capabilityService.get().keywordIndex() ? "POSTGRES" : "BASIC";
    }

    private static long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}
