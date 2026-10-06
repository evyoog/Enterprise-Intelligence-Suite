package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Knowledge Center reader payloads (REQ-KNW-005). */
public final class KnowledgeReaderDtos {

    private KnowledgeReaderDtos() {
    }

    /** Home: section counts, recommended, popular guides, featured videos, popular searches. */
    public record Home(Map<String, Long> sectionCounts, List<KnowledgeSummaryDto> recommended,
                       boolean personalized, List<KnowledgeSummaryDto> popularGuides,
                       List<KnowledgeSummaryDto> featuredVideos, List<String> popularSearches,
                       List<ProductCard> products) {
    }

    public record ProductCard(Long id, String name, String slug, String description, Long catalogProductId,
                              List<ModuleCard> modules, long contentCount, boolean hasAccess) {
    }

    public record ModuleCard(Long id, String name, String slug, long contentCount) {
    }

    public record ModuleSection(ModuleCard module, List<KnowledgeSummaryDto> items) {
    }

    public record ProductHub(ProductCard product, List<ModuleSection> modules, List<KnowledgeSummaryDto> general) {
    }

    public record GlossaryTerm(Long id, String slug, String term, String definition, List<String> synonyms) {
    }

    /** Personal learning panel (signed in, BR-KCEN-004). */
    public record Personal(List<Progress> continueLearning, List<KnowledgeSummaryDto> recentlyViewed,
                           List<KnowledgeSummaryDto> bookmarks) {
    }

    public record Progress(KnowledgeSummaryDto item, int percent, Integer positionSeconds, Instant lastViewedAt,
                           boolean completed) {
    }

    public record ProgressRequest(Integer percent, Integer positionSeconds) {
    }

    /** Knowledge search, grouped by type; only visible content (C76). */
    public record SearchResult(String query, List<KnowledgeSummaryDto> results,
                               Map<KnowledgeContentType, List<KnowledgeSummaryDto>> byType,
                               List<ProductCard> products, List<ModuleHit> modules, String didYouMean,
                               String semanticStatus, long tookMs) {
    }

    public record ModuleHit(Long id, String name, String slug, String productName, String productSlug) {
    }

    public record FeedbackRequest(String kind, Boolean helpful, String reason, String comment) {
    }

    public record EventRequest(String type, Long contentId, Integer percent, Integer seconds) {
    }

    public record AssistantStatus(boolean configured, String message) {
    }

    public record AssistantRequest(String question, Map<String, Object> context) {
    }
}
