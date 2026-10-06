package com.vyoog.eisplatform.modules.knowledgebase.dto;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Admin analytics and dashboard (REQ-KNW-006) — aggregates only (BR-KANL-004). */
public final class KnowledgeAnalyticsDto {

    private KnowledgeAnalyticsDto() {
    }

    public record Ranked(Long contentId, String title, KnowledgeContentType contentType, long count) {
    }

    public record Rated(Long contentId, String title, KnowledgeContentType contentType, long votes, int helpfulPercent) {
    }

    public record Gap(String query, long searches, int results) {
    }

    public record FeedbackItem(Long contentId, String title, String kind, String reason, String comment, Instant createdAt) {
    }

    public record Analytics(int days, long contentViews, long videoPlays, long downloads, long knowledgeSearches,
                            long noResultSearches, Integer helpfulPercent, long ticketsFromKnowledge,
                            long videoCompletions, Integer averageWatchSeconds, Map<String, Long> playsBySource,
                            List<Ranked> mostViewed, List<Ranked> mostWatched, List<Ranked> mostDownloaded,
                            List<String> mostSearched, List<Gap> gaps, List<Rated> lowestRated,
                            List<FeedbackItem> recentFeedback) {
    }

    public record Dashboard(long total, Map<String, Long> byState, Map<String, Long> byType, long expired,
                            long requiresReview, long scheduled, List<Ranked> mostViewed, List<Gap> gaps,
                            List<Rated> lowestRated) {
    }
}
