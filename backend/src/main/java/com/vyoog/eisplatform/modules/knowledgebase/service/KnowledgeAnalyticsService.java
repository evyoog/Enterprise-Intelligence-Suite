package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeAnalyticsDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeEvent;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeFeedback;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProgress;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeEventRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeFeedbackRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProgressRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeVideoRepository;
import com.vyoog.eisplatform.modules.search.dto.QueryCountDto;
import com.vyoog.eisplatform.modules.search.service.SearchInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Knowledge analytics, feedback and personal progress (REQ-KNW-006,
 * REQ-KNW-005.8/.13). Events are recorded only for content the reader was
 * allowed to see (BR-KANL-001); analytics screens show aggregates only, never
 * readers (BR-KANL-004). Defaults applied on 2026-10-05: gap threshold 5
 * searches with no results; lowest rated needs at least 5 votes.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeAnalyticsService {

    static final int GAP_MIN_SEARCHES = 5;
    static final int LOWEST_RATED_MIN_VOTES = 5;
    static final Duration VIEW_WINDOW = Duration.ofMinutes(30);
    static final Set<String> NO_REASONS = Set.of("UNCLEAR", "OUTDATED", "MISSING", "NOT_SOLVED", "OTHER");
    static final Set<Integer> PROGRESS_MARKS = Set.of(25, 50, 75, 100);
    static final int COMPLETION_PERCENT = 95;

    private final KnowledgeEventRepository eventRepository;
    private final KnowledgeFeedbackRepository feedbackRepository;
    private final KnowledgeProgressRepository progressRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeVideoRepository videoRepository;
    private final KnowledgeViewService viewService;
    private final SearchInsightsService insightsService;

    // ---- recording ----------------------------------------------------------

    /** CONTENT_VIEWED, once per reader per item per 30 minutes (BR-KANL-002). */
    @Transactional
    public void recordView(KnowledgeViewService.Live live, KnowledgeReader reader, String viewerKey) {
        String hash = hash(viewerKey);
        Instant now = Instant.now();
        if (!eventRepository.existsByEventTypeAndContentIdAndViewerHashAndCreatedAtAfter(KnowledgeEvent.Type.CONTENT_VIEWED,
                live.item().getId(), hash, now.minus(VIEW_WINDOW))) {
            eventRepository.save(event(KnowledgeEvent.Type.CONTENT_VIEWED, live, reader, hash, null, null));
        }
        if (reader.customerId() != null) {
            KnowledgeProgress progress = progressRepository.findByCustomerIdAndContentId(reader.customerId(), live.item().getId())
                .orElseGet(() -> newProgress(reader.customerId(), live.item().getId()));
            progress.setLastViewedAt(now);
            progressRepository.save(progress);
        }
    }

    @Transactional
    public void recordDownload(KnowledgeViewService.Live live, KnowledgeReader reader, String viewerKey) {
        eventRepository.save(event(KnowledgeEvent.Type.DOWNLOADED, live, reader, hash(viewerKey), null, null));
    }

    /** Events reported by the page: video plays, progress, tickets created from knowledge. */
    @Transactional
    public void recordClientEvent(KnowledgeReader reader, KnowledgeReaderDtos.EventRequest request, String viewerKey) {
        KnowledgeEvent.Type type;
        try {
            type = KnowledgeEvent.Type.valueOf(request.type());
        } catch (RuntimeException e) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Unknown event type.");
        }
        if (type != KnowledgeEvent.Type.VIDEO_PLAYED && type != KnowledgeEvent.Type.VIDEO_PROGRESS
            && type != KnowledgeEvent.Type.TICKET_CREATED_FROM_KNOWLEDGE) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "This event is recorded by the server.");
        }
        KnowledgeViewService.Live live = visible(reader, request.contentId());
        Integer percent = null;
        if (type == KnowledgeEvent.Type.VIDEO_PROGRESS) {
            if (request.percent() == null || !PROGRESS_MARKS.contains(request.percent())) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Progress must be 25, 50, 75 or 100.");
            }
            percent = request.percent();
        }
        Integer seconds = request.seconds() == null ? null : Math.max(0, Math.min(request.seconds(), 86_400));
        eventRepository.save(event(type, live, reader, hash(viewerKey), percent, seconds));
        if (percent != null && reader.customerId() != null) {
            saveProgress(reader, live.item().getId(), percent, seconds);
        }
    }

    @Transactional
    public void saveProgress(KnowledgeReader reader, Long contentId, Integer percent, Integer positionSeconds) {
        if (reader.customerId() == null) {
            throw new KnowledgeException(HttpStatus.UNAUTHORIZED, "SIGN_IN_REQUIRED", "Sign in to save your progress.");
        }
        visible(reader, contentId);
        KnowledgeProgress progress = progressRepository.findByCustomerIdAndContentId(reader.customerId(), contentId)
            .orElseGet(() -> newProgress(reader.customerId(), contentId));
        int value = percent == null ? progress.getPercent() : Math.max(0, Math.min(100, percent));
        progress.setPercent(Math.max(progress.getPercent(), value));
        if (positionSeconds != null) {
            progress.setPositionSeconds(Math.max(0, positionSeconds));
        }
        if (progress.getPercent() >= COMPLETION_PERCENT && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }
        progress.setLastViewedAt(Instant.now());
        progressRepository.save(progress);
    }

    /** BR-KCEN-003: one vote per reader per version (a second replaces the first); No needs a reason. */
    @Transactional
    public void feedback(KnowledgeReader reader, Long contentId, KnowledgeReaderDtos.FeedbackRequest request) {
        if (!reader.signedIn()) {
            throw new KnowledgeException(HttpStatus.UNAUTHORIZED, "SIGN_IN_REQUIRED", "Sign in to give feedback.");
        }
        KnowledgeViewService.Live live = visible(reader, contentId);
        KnowledgeFeedback.Kind kind;
        try {
            kind = KnowledgeFeedback.Kind.valueOf(request.kind() == null ? "VOTE" : request.kind());
        } catch (RuntimeException e) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Unknown feedback kind.");
        }
        String comment = request.comment() == null || request.comment().isBlank() ? null : request.comment().trim();
        if (comment != null && comment.length() > 1000) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Comments can be up to 1,000 characters.");
        }
        String reason = null;
        if (kind == KnowledgeFeedback.Kind.VOTE) {
            if (request.helpful() == null) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Choose Yes or No.");
            }
            if (!request.helpful()) {
                reason = request.reason();
                if (reason == null || !NO_REASONS.contains(reason)) {
                    throw KnowledgeException.badRequest("INVALID_CONTENT", "Choose a reason.");
                }
            }
        } else if (comment == null) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Tell us what should change.");
        }
        KnowledgeFeedback entry = kind == KnowledgeFeedback.Kind.VOTE
            ? feedbackRepository.findFirstByContentIdAndVersionLabelAndVoterSubAndKind(contentId,
                live.version().getVersionLabel(), reader.keycloakSub(), kind).orElseGet(KnowledgeFeedback::new)
            : new KnowledgeFeedback();
        entry.setContentId(contentId);
        entry.setVersionLabel(live.version().getVersionLabel());
        entry.setCustomerId(reader.customerId());
        entry.setVoterSub(reader.keycloakSub());
        entry.setKind(kind);
        entry.setHelpful(kind == KnowledgeFeedback.Kind.VOTE ? request.helpful() : null);
        entry.setReason(reason);
        entry.setComment(comment);
        entry.setCreatedAt(Instant.now());
        feedbackRepository.save(entry);
        eventRepository.save(event(KnowledgeEvent.Type.FEEDBACK_GIVEN, live, reader, hash(reader.keycloakSub()), null, null));
    }

    private KnowledgeViewService.Live visible(KnowledgeReader reader, Long contentId) {
        if (contentId == null) {
            throw new ResourceNotFoundException("Content not found");
        }
        KnowledgeArticle item = articleRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        return viewService.visibleItem(item, reader).orElseThrow(() -> new ResourceNotFoundException("Content not found"));
    }

    private KnowledgeEvent event(KnowledgeEvent.Type type, KnowledgeViewService.Live live, KnowledgeReader reader,
                                 String hash, Integer percent, Integer seconds) {
        KnowledgeEvent e = new KnowledgeEvent();
        e.setEventType(type);
        e.setContentId(live.item().getId());
        e.setVersionLabel(live.version().getVersionLabel());
        e.setContentType(live.item().getContentType());
        e.setProductId(live.item().getProductId());
        e.setModuleId(live.item().getModuleId());
        videoRepository.findByContentId(live.item().getId()).ifPresent(v -> e.setSourceType(v.getSourceType().name()));
        e.setOrganizationId(reader.organizationId());
        e.setPercent(percent);
        e.setSeconds(seconds);
        e.setViewerHash(hash);
        e.setCreatedAt(Instant.now());
        return e;
    }

    private static KnowledgeProgress newProgress(Long customerId, Long contentId) {
        KnowledgeProgress p = new KnowledgeProgress();
        p.setCustomerId(customerId);
        p.setContentId(contentId);
        return p;
    }

    static String hash(String key) {
        if (key == null) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---- reporting ----------------------------------------------------------

    public List<String> popularKnowledgeSearches(int limit) {
        Set<String> empty = insightsService.topQueries(KnowledgeSearchService.SCOPE, 30, true, 1, 100).stream()
            .map(QueryCountDto::query).collect(Collectors.toSet());
        return insightsService.topQueries(KnowledgeSearchService.SCOPE, 30, false, 1, 50).stream()
            .map(QueryCountDto::query).filter(q -> !empty.contains(q)).limit(limit).toList();
    }

    public KnowledgeAnalyticsDto.Analytics analytics(int days) {
        int window = Math.max(1, Math.min(days, 365));
        Instant since = Instant.now().minus(Duration.ofDays(window));
        Map<KnowledgeEvent.Type, Long> counts = new LinkedHashMap<>();
        for (Object[] row : eventRepository.countByTypeSince(since)) {
            counts.put((KnowledgeEvent.Type) row[0], ((Number) row[1]).longValue());
        }
        Map<Long, KnowledgeArticle> articles = articleRepository.findAll().stream()
            .collect(Collectors.toMap(KnowledgeArticle::getId, Function.identity()));
        long[] searches = insightsService.counts(KnowledgeSearchService.SCOPE, window);
        Map<String, Long> bySource = new LinkedHashMap<>();
        for (Object[] row : eventRepository.playsBySource(since)) {
            bySource.put(row[0] == null ? "UNKNOWN" : (String) row[0], ((Number) row[1]).longValue());
        }
        Double avg = eventRepository.averageWatchSecondsSince(since);
        List<KnowledgeFeedback> recent = feedbackRepository.findByKindInAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            List.of(KnowledgeFeedback.Kind.OUTDATED, KnowledgeFeedback.Kind.SUGGESTION, KnowledgeFeedback.Kind.VOTE), since);
        List<KnowledgeAnalyticsDto.FeedbackItem> feedback = recent.stream()
            .filter(f -> f.getKind() != KnowledgeFeedback.Kind.VOTE || f.getComment() != null)
            .limit(20)
            .map(f -> new KnowledgeAnalyticsDto.FeedbackItem(f.getContentId(), title(articles, f.getContentId()),
                f.getKind().name(), f.getReason(), f.getComment(), f.getCreatedAt()))
            .toList();
        return new KnowledgeAnalyticsDto.Analytics(window,
            counts.getOrDefault(KnowledgeEvent.Type.CONTENT_VIEWED, 0L),
            counts.getOrDefault(KnowledgeEvent.Type.VIDEO_PLAYED, 0L),
            counts.getOrDefault(KnowledgeEvent.Type.DOWNLOADED, 0L),
            searches[0], searches[1], helpfulPercent(since),
            counts.getOrDefault(KnowledgeEvent.Type.TICKET_CREATED_FROM_KNOWLEDGE, 0L),
            eventRepository.completionsSince(since), avg == null ? null : (int) Math.round(avg), bySource,
            ranked(KnowledgeEvent.Type.CONTENT_VIEWED, since, articles),
            ranked(KnowledgeEvent.Type.VIDEO_PLAYED, since, articles),
            ranked(KnowledgeEvent.Type.DOWNLOADED, since, articles),
            insightsService.topQueries(KnowledgeSearchService.SCOPE, window, false, 1, 10).stream().map(QueryCountDto::query).toList(),
            gaps(window), lowestRated(since, articles), feedback);
    }

    public KnowledgeAnalyticsDto.Dashboard dashboard() {
        List<KnowledgeArticle> all = articleRepository.findAll();
        Instant now = Instant.now();
        Map<String, Long> byState = new LinkedHashMap<>();
        for (KnowledgeWorkflowState s : KnowledgeWorkflowState.values()) {
            byState.put(s.name(), all.stream().filter(a -> a.getWorkflowState() == s).count());
        }
        Map<String, Long> byType = all.stream().collect(Collectors.groupingBy(a -> a.getContentType().name(),
            LinkedHashMap::new, Collectors.counting()));
        Map<Long, KnowledgeArticle> articles = all.stream().collect(Collectors.toMap(KnowledgeArticle::getId, Function.identity()));
        Instant since = now.minus(Duration.ofDays(30));
        return new KnowledgeAnalyticsDto.Dashboard(all.size(), byState, byType,
            all.stream().filter(a -> KnowledgeContentService.isExpired(a, now)).count(),
            all.stream().filter(a -> KnowledgeContentService.requiresReview(a, now)).count(),
            byState.getOrDefault(KnowledgeWorkflowState.SCHEDULED.name(), 0L),
            ranked(KnowledgeEvent.Type.CONTENT_VIEWED, since, articles), gaps(30), lowestRated(since, articles));
    }

    /** REQ-KNW-006.4: frequent knowledge searches that found nothing. */
    public List<KnowledgeAnalyticsDto.Gap> gaps(int days) {
        return insightsService.topQueries(KnowledgeSearchService.SCOPE, days, true, GAP_MIN_SEARCHES, 20).stream()
            .map(q -> new KnowledgeAnalyticsDto.Gap(q.query(), q.count(), 0)).toList();
    }

    private Integer helpfulPercent(Instant since) {
        long helpful = 0;
        long total = 0;
        for (Object[] row : feedbackRepository.voteTotalsSince(since)) {
            helpful += ((Number) row[1]).longValue();
            total += ((Number) row[2]).longValue();
        }
        return total == 0 ? null : (int) Math.round(helpful * 100.0 / total);
    }

    private List<KnowledgeAnalyticsDto.Rated> lowestRated(Instant since, Map<Long, KnowledgeArticle> articles) {
        List<KnowledgeAnalyticsDto.Rated> rated = new ArrayList<>();
        for (Object[] row : feedbackRepository.voteTotalsSince(since)) {
            Long id = (Long) row[0];
            long helpful = ((Number) row[1]).longValue();
            long total = ((Number) row[2]).longValue();
            KnowledgeArticle a = articles.get(id);
            if (a != null && total >= LOWEST_RATED_MIN_VOTES) {
                rated.add(new KnowledgeAnalyticsDto.Rated(id, a.getTitle(), a.getContentType(), total,
                    (int) Math.round(helpful * 100.0 / total)));
            }
        }
        rated.sort(java.util.Comparator.comparingInt(KnowledgeAnalyticsDto.Rated::helpfulPercent)
            .thenComparing(KnowledgeAnalyticsDto.Rated::votes, java.util.Comparator.reverseOrder()));
        return rated.stream().limit(10).toList();
    }

    private List<KnowledgeAnalyticsDto.Ranked> ranked(KnowledgeEvent.Type type, Instant since, Map<Long, KnowledgeArticle> articles) {
        return eventRepository.topContent(type, since).stream()
            .filter(row -> articles.containsKey((Long) row[0]))
            .limit(10)
            .map(row -> {
                KnowledgeArticle a = articles.get((Long) row[0]);
                return new KnowledgeAnalyticsDto.Ranked(a.getId(), a.getTitle(), a.getContentType(), ((Number) row[1]).longValue());
            })
            .toList();
    }

    private static String title(Map<Long, KnowledgeArticle> articles, Long id) {
        KnowledgeArticle a = articles.get(id);
        return a == null ? null : a.getTitle();
    }

    /** Personal learning panel (BR-KCEN-004: the reader's own data only). */
    public KnowledgeReaderDtos.Personal personal(KnowledgeReader reader, List<KnowledgeViewService.Live> bookmarks) {
        if (reader.customerId() == null) {
            return new KnowledgeReaderDtos.Personal(List.of(), List.of(), viewService.summaries(bookmarks));
        }
        List<KnowledgeProgress> progress = progressRepository.findTop20ByCustomerIdOrderByLastViewedAtDesc(reader.customerId());
        Map<Long, KnowledgeArticle> items = articleRepository.findAllById(progress.stream().map(KnowledgeProgress::getContentId).toList())
            .stream().collect(Collectors.toMap(KnowledgeArticle::getId, Function.identity()));
        List<KnowledgeViewService.Live> visible = viewService.filterVisible(new ArrayList<>(items.values()), reader);
        Map<Long, KnowledgeViewService.Live> visibleById = visible.stream()
            .collect(Collectors.toMap(l -> l.item().getId(), Function.identity()));
        KnowledgeViewService.Lookups lookups = viewService.lookups(visible.stream().map(KnowledgeViewService.Live::item).toList());
        List<KnowledgeReaderDtos.Progress> continueLearning = new ArrayList<>();
        List<com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeSummaryDto> recent = new ArrayList<>();
        for (KnowledgeProgress p : progress) {
            KnowledgeViewService.Live live = visibleById.get(p.getContentId());
            if (live == null) {
                continue;
            }
            var summary = viewService.summary(live, lookups);
            recent.add(summary);
            if (p.getPercent() > 0 && p.getCompletedAt() == null) {
                continueLearning.add(new KnowledgeReaderDtos.Progress(summary, p.getPercent(), p.getPositionSeconds(),
                    p.getLastViewedAt(), false));
            }
        }
        return new KnowledgeReaderDtos.Personal(continueLearning.stream().limit(6).toList(),
            recent.stream().limit(10).toList(), viewService.summaries(bookmarks));
    }
}
