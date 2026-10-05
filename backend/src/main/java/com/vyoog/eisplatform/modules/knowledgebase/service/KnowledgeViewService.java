package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeItemDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeSummaryDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeCategory;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentVersion;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeEvent;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMedia;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeCategoryRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeContentVersionRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeEventRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeMediaRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds reader views of content and the visible set for a reader
 * (BR-KVS-001 applied before any ranking, counting or paging — BR-KCEN-001).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeViewService {

    private static final int WORDS_PER_MINUTE = 200;

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeContentVersionRepository versionRepository;
    private final KnowledgeProductRepository productRepository;
    private final KnowledgeModuleRepository moduleRepository;
    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeVideoRepository videoRepository;
    private final KnowledgeMediaRepository mediaRepository;
    private final KnowledgeEventRepository eventRepository;
    private final KnowledgeAccessService accessService;
    private final MediaStorageService storage;
    private final KnowledgeSettings settings;

    /** A live item and the version readers see. */
    public record Live(KnowledgeArticle item, KnowledgeContentVersion version) {
    }

    /** Everything needed to render cards for a set of items. */
    public record Lookups(Map<Long, KnowledgeProduct> products, Map<Long, KnowledgeModule> modules,
                          Map<Long, KnowledgeCategory> categories, Map<Long, KnowledgeVideo> videos,
                          Map<Long, Long> views) {
    }

    /** Every item the reader may see (optionally of some types). */
    public List<Live> visible(KnowledgeReader reader, Collection<com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType> types) {
        List<KnowledgeArticle> items = types == null || types.isEmpty()
            ? articleRepository.findLive() : articleRepository.findLiveOfTypes(types);
        return filterVisible(items, reader);
    }

    public List<Live> filterVisible(List<KnowledgeArticle> items, KnowledgeReader reader) {
        if (items.isEmpty()) {
            return List.of();
        }
        Map<Long, KnowledgeContentVersion> versions = versionRepository.findAllById(
                items.stream().map(KnowledgeArticle::getLiveVersionId).filter(Objects::nonNull).toList())
            .stream().collect(Collectors.toMap(KnowledgeContentVersion::getId, Function.identity()));
        Map<Long, Long> catalog = accessService.catalogLinks();
        Instant now = Instant.now();
        List<Live> result = new ArrayList<>();
        for (KnowledgeArticle item : items) {
            KnowledgeContentVersion version = item.getLiveVersionId() == null ? null : versions.get(item.getLiveVersionId());
            if (accessService.canSee(item, version, reader, catalog::get, now)) {
                result.add(new Live(item, version));
            }
        }
        return result;
    }

    /** The item if the reader may see it. */
    public java.util.Optional<Live> visibleItem(KnowledgeArticle item, KnowledgeReader reader) {
        List<Live> live = filterVisible(List.of(item), reader);
        return live.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(live.get(0));
    }

    public Lookups lookups(Collection<KnowledgeArticle> items) {
        Map<Long, KnowledgeProduct> products = productRepository.findAll().stream()
            .collect(Collectors.toMap(KnowledgeProduct::getId, Function.identity()));
        Map<Long, KnowledgeModule> modules = moduleRepository.findAll().stream()
            .collect(Collectors.toMap(KnowledgeModule::getId, Function.identity()));
        Map<Long, KnowledgeCategory> categories = categoryRepository.findAll().stream()
            .collect(Collectors.toMap(KnowledgeCategory::getId, Function.identity()));
        List<Long> ids = items.stream().map(KnowledgeArticle::getId).toList();
        Map<Long, KnowledgeVideo> videos = ids.isEmpty() ? Map.of() : videoRepository.findByContentIdIn(ids).stream()
            .collect(Collectors.toMap(KnowledgeVideo::getContentId, Function.identity()));
        return new Lookups(products, modules, categories, videos, viewCounts());
    }

    public Map<Long, Long> viewCounts() {
        Map<Long, Long> views = new HashMap<>();
        for (Object[] row : eventRepository.totals(KnowledgeEvent.Type.CONTENT_VIEWED)) {
            views.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return views;
    }

    public KnowledgeSummaryDto summary(Live live, Lookups lookups) {
        KnowledgeArticle a = live.item();
        KnowledgeContentVersion v = live.version();
        KnowledgeProduct product = a.getProductId() == null ? null : lookups.products().get(a.getProductId());
        KnowledgeModule module = a.getModuleId() == null ? null : lookups.modules().get(a.getModuleId());
        KnowledgeCategory category = a.getCategoryId() == null ? null : lookups.categories().get(a.getCategoryId());
        KnowledgeVideo video = lookups.videos().get(a.getId());
        return new KnowledgeSummaryDto(a.getId(), a.getContentType(), a.getSlug(), v.getTitle(), v.getShortDescription(),
            a.getProductId(), product == null ? null : product.getName(), product == null ? null : product.getSlug(),
            a.getModuleId(), module == null ? null : module.getName(), category == null ? null : category.getName(),
            KnowledgeBlocks.tagList(a.getTags()), readingMinutes(v.getBody()), lookups.views().getOrDefault(a.getId(), 0L),
            v.getVersionLabel(), v.getPublishedAt(), a.isFeatured(), isDeprecated(a), a.getDifficulty(),
            video == null ? null : video.getSourceType().name(), video == null ? null : video.getDurationSeconds(),
            video == null ? null : thumbnail(video));
    }

    public List<KnowledgeSummaryDto> summaries(List<Live> live) {
        Lookups lookups = lookups(live.stream().map(Live::item).toList());
        return live.stream().map(l -> summary(l, lookups)).toList();
    }

    /** Reader view of the live version. */
    public KnowledgeItemDto item(Live live, KnowledgeReader reader, boolean bookmarked) {
        KnowledgeArticle a = live.item();
        KnowledgeContentVersion v = live.version();
        Lookups lookups = lookups(List.of(a));
        return item(a, v.getTitle(), v.getShortDescription(), v.getBlocks(), v.getTypeFields(), v.getBody(),
            v.getVersionLabel(), v.getPublishedAt(), related(a, reader), lookups, bookmarked, false);
    }

    /** Preview of the working copy, rendered the same way (REQ-KNW-002.7). */
    public KnowledgeItemDto preview(KnowledgeArticle a, KnowledgeReader audience) {
        Lookups lookups = lookups(List.of(a));
        return item(a, a.getTitle(), a.getShortDescription(), a.getBlocks(), a.getTypeFields(), a.getBody(),
            a.getCurrentVersionLabel(), a.getPublishedAt(), related(a, audience), lookups, false, true);
    }

    private KnowledgeItemDto item(KnowledgeArticle a, String title, String shortDescription, String blocks,
                                  String typeFields, String body, String versionLabel, Instant publishedAt,
                                  List<KnowledgeSummaryDto> related, Lookups lookups, boolean bookmarked,
                                  boolean preview) {
        KnowledgeProduct product = a.getProductId() == null ? null : lookups.products().get(a.getProductId());
        KnowledgeModule module = a.getModuleId() == null ? null : lookups.modules().get(a.getModuleId());
        KnowledgeCategory category = a.getCategoryId() == null ? null : lookups.categories().get(a.getCategoryId());
        KnowledgeVideo video = lookups.videos().get(a.getId());
        JsonNode typeFieldsNode = typeFields == null ? null : KnowledgeBlocks.parse(typeFields);
        return new KnowledgeItemDto(a.getId(), a.getContentType(), a.getSlug(), title, shortDescription,
            KnowledgeBlocks.parse(blocks), typeFieldsNode,
            a.getProductId(), product == null ? null : product.getName(), product == null ? null : product.getSlug(),
            a.getModuleId(), module == null ? null : module.getName(), category == null ? null : category.getName(),
            a.getFeature(), KnowledgeBlocks.tagList(a.getTags()), versionLabel, a.getProductVersion(),
            a.getDocumentationVersion(), publishedAt, a.getUpdatedAt(), readingMinutes(body),
            lookups.views().getOrDefault(a.getId(), 0L), isDeprecated(a), a.getDifficulty(),
            safeRoute(a.getDirectActionRoute()), a.getDirectActionLabel(), related,
            video == null ? null : videoDto(video, true), bookmarked, preview);
    }

    /** Related content the reader may see (REQ-KNW-002.8). */
    private List<KnowledgeSummaryDto> related(KnowledgeArticle a, KnowledgeReader reader) {
        List<Long> ids = new ArrayList<>(KnowledgeAccessService.parseIds(a.getRelatedContentIds()));
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Live> live = filterVisible(articleRepository.findAllById(ids), reader);
        return summaries(live);
    }

    public KnowledgeVideoDto videoDto(KnowledgeVideo video, boolean forReader) {
        List<String> languages = new ArrayList<>();
        JsonNode subtitles = KnowledgeBlocks.parse(video.getSubtitles());
        if (subtitles.isObject()) {
            Iterator<String> names = subtitles.fieldNames();
            names.forEachRemaining(languages::add);
        }
        return new KnowledgeVideoDto(video.getSourceType(),
            video.getSourceType() == com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType.AWS_S3 ? null : video.getVideoUrl(),
            video.getVideoId(), forReader ? null : video.getMediaId(), forReader ? null : video.getThumbnailMediaId(),
            thumbnail(video), video.getDurationSeconds(), video.getChannel(), video.getTranscript(),
            KnowledgeBlocks.parse(video.getChapters()), languages);
    }

    /** Provider thumbnail, or a short-lived URL for an uploaded one (never a permanent S3 URL). */
    public String thumbnail(KnowledgeVideo video) {
        if (video.getThumbnailMediaId() != null && storage.configured()) {
            KnowledgeMedia media = mediaRepository.findById(video.getThumbnailMediaId()).orElse(null);
            if (media != null && media.getStatus() == KnowledgeMediaStatus.READY) {
                try {
                    return storage.getTemporaryUrl(media.getS3ObjectKey(), null, settings.getPlaybackUrlExpiry()).url();
                } catch (RuntimeException e) {
                    return null;
                }
            }
        }
        if (video.getThumbnailUrl() != null) {
            return video.getThumbnailUrl();
        }
        if (video.getVideoId() != null) {
            return "https://i.ytimg.com/vi/" + video.getVideoId() + "/hqdefault.jpg";
        }
        return null;
    }

    static boolean isDeprecated(KnowledgeArticle a) {
        return a.getDeprecatedAt() != null || a.getWorkflowState() == KnowledgeWorkflowState.DEPRECATED;
    }

    static int readingMinutes(String body) {
        if (body == null || body.isBlank()) {
            return 1;
        }
        int words = body.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(words / (double) WORDS_PER_MINUTE));
    }

    /** BR-KCEN-006: a direct action goes only to an EIS route. */
    static String safeRoute(String route) {
        if (route == null || route.isBlank() || !route.startsWith("/") || route.startsWith("//")) {
            return null;
        }
        return route;
    }
}
