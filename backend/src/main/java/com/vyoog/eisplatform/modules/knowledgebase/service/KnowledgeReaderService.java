package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeItemDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePageDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeReaderDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeSummaryDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMedia;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeProduct;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeBookmarkRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Knowledge Center (REQ-KNW-005). Every list, hub, recommendation, count
 * and download applies BR-KVS-001 first (KnowledgeViewService#visible), so
 * content the reader may not see is never ranked, counted or returned
 * (BR-KCEN-001); a hidden item answers 404 like a missing one.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeReaderService {

    static final Set<KnowledgeContentType> GUIDES = EnumSet.of(KnowledgeContentType.ARTICLE,
        KnowledgeContentType.GETTING_STARTED, KnowledgeContentType.PRODUCT_GUIDE);
    static final Set<KnowledgeContentType> DOWNLOADS = EnumSet.of(KnowledgeContentType.DOCUMENT,
        KnowledgeContentType.TEMPLATE, KnowledgeContentType.STUDY_MATERIAL);

    private final KnowledgeViewService viewService;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeProductRepository productRepository;
    private final KnowledgeModuleRepository moduleRepository;
    private final KnowledgeBookmarkRepository bookmarkRepository;
    private final KnowledgeAccessService accessService;
    private final KnowledgeMediaService mediaService;
    private final KnowledgeSettings settings;
    private final KnowledgeAnalyticsService analyticsService;
    private final KnowledgeSearchService searchService;

    public KnowledgeReaderDtos.Home home(KnowledgeReader reader) {
        List<KnowledgeViewService.Live> visible = viewService.visible(reader, null);
        KnowledgeViewService.Lookups lookups = viewService.lookups(visible.stream().map(KnowledgeViewService.Live::item).toList());
        Map<String, Long> sections = new LinkedHashMap<>();
        sections.put("gettingStarted", count(visible, EnumSet.of(KnowledgeContentType.GETTING_STARTED)));
        sections.put("productGuides", count(visible, EnumSet.of(KnowledgeContentType.PRODUCT_GUIDE, KnowledgeContentType.ARTICLE)));
        sections.put("videos", count(visible, EnumSet.of(KnowledgeContentType.VIDEO)));
        sections.put("troubleshooting", count(visible, EnumSet.of(KnowledgeContentType.TROUBLESHOOTING, KnowledgeContentType.ERROR_CODE)));
        sections.put("downloads", count(visible, DOWNLOADS));
        sections.put("faqs", count(visible, EnumSet.of(KnowledgeContentType.FAQ)));

        Map<Long, Long> catalog = accessService.catalogLinks();
        Comparator<KnowledgeViewService.Live> byViews = Comparator.comparingLong(
            (KnowledgeViewService.Live l) -> lookups.views().getOrDefault(l.item().getId(), 0L)).reversed();
        Comparator<KnowledgeViewService.Live> byRecent = Comparator.comparing(
            (KnowledgeViewService.Live l) -> l.version().getPublishedAt()).reversed();

        // Recommended (REQ-KNW-005.5, ranking applied 2026-10-05): content for
        // products the reader has access to first, then recent, then popular.
        boolean personalized = !reader.productIds().isEmpty();
        List<KnowledgeViewService.Live> recommendedPool = visible.stream()
            .filter(l -> l.item().getContentType() != KnowledgeContentType.GLOSSARY_TERM)
            .collect(Collectors.toCollection(ArrayList::new));
        if (personalized) {
            recommendedPool.sort(Comparator.comparing((KnowledgeViewService.Live l) ->
                    !hasAccess(l.item(), reader, catalog)).thenComparing(byRecent).thenComparing(byViews));
        } else {
            recommendedPool.sort(byViews.thenComparing(byRecent));
        }
        List<KnowledgeSummaryDto> recommended = recommendedPool.stream().limit(8).map(l -> viewService.summary(l, lookups)).toList();

        List<KnowledgeSummaryDto> popularGuides = visible.stream().filter(l -> GUIDES.contains(l.item().getContentType()))
            .sorted(byViews.thenComparing(byRecent)).limit(6).map(l -> viewService.summary(l, lookups)).toList();
        List<KnowledgeSummaryDto> featuredVideos = visible.stream()
            .filter(l -> l.item().getContentType() == KnowledgeContentType.VIDEO)
            .sorted(Comparator.comparing((KnowledgeViewService.Live l) -> !l.item().isFeatured()).thenComparing(byViews)
                .thenComparing(byRecent))
            .limit(6).map(l -> viewService.summary(l, lookups)).toList();
        return new KnowledgeReaderDtos.Home(sections, recommended, personalized, popularGuides, featuredVideos,
            analyticsService.popularKnowledgeSearches(8), productCards(visible, reader, catalog));
    }

    private static long count(List<KnowledgeViewService.Live> visible, Set<KnowledgeContentType> types) {
        return visible.stream().filter(l -> types.contains(l.item().getContentType())).count();
    }

    private static boolean hasAccess(KnowledgeArticle item, KnowledgeReader reader, Map<Long, Long> catalog) {
        Long catalogId = item.getProductId() == null ? null : catalog.get(item.getProductId());
        return catalogId != null && reader.productIds().contains(catalogId);
    }

    public KnowledgePageDto<KnowledgeSummaryDto> list(KnowledgeReader reader, List<KnowledgeContentType> types,
                                                      String productSlug, Long moduleId, Long categoryId, String tag,
                                                      String query, String sort, int page, int size) {
        Long productId = productSlug == null || productSlug.isBlank() ? null
            : productRepository.findBySlug(productSlug).map(KnowledgeProduct::getId).orElse(-1L);
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String t = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
        List<KnowledgeViewService.Live> visible = viewService.visible(reader, types).stream()
            .filter(l -> productId == null || productId.equals(l.item().getProductId()))
            .filter(l -> moduleId == null || moduleId.equals(l.item().getModuleId()))
            .filter(l -> categoryId == null || categoryId.equals(l.item().getCategoryId()))
            .filter(l -> t.isEmpty() || KnowledgeBlocks.tagList(l.item().getTags()).stream()
                .anyMatch(x -> x.toLowerCase(Locale.ROOT).equals(t)))
            .filter(l -> q.isEmpty() || contains(l.version().getTitle(), q) || contains(l.version().getBody(), q)
                || contains(l.version().getSearchText(), q))
            .collect(Collectors.toCollection(ArrayList::new));
        Map<Long, Long> views = viewService.viewCounts();
        if ("popular".equalsIgnoreCase(sort)) {
            visible.sort(Comparator.comparingLong((KnowledgeViewService.Live l) -> views.getOrDefault(l.item().getId(), 0L)).reversed());
        } else if ("title".equalsIgnoreCase(sort)) {
            visible.sort(Comparator.comparing((KnowledgeViewService.Live l) -> l.version().getTitle(), String.CASE_INSENSITIVE_ORDER));
        } else {
            visible.sort(Comparator.comparing((KnowledgeViewService.Live l) -> l.version().getPublishedAt()).reversed());
        }
        int safeSize = Math.max(1, Math.min(size, 100));
        int from = Math.min(Math.max(page, 0) * safeSize, visible.size());
        List<KnowledgeViewService.Live> slice = visible.subList(from, Math.min(from + safeSize, visible.size()));
        return new KnowledgePageDto<>(viewService.summaries(slice), visible.size(), Math.max(page, 0), safeSize);
    }

    /** One item by id or slug; also counts the view (BR-KANL-001/002) and updates "recently viewed". */
    @Transactional
    public KnowledgeItemDto get(KnowledgeReader reader, String idOrSlug, String viewerKey) {
        KnowledgeArticle item = resolve(idOrSlug);
        KnowledgeViewService.Live live = viewService.visibleItem(item, reader)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        analyticsService.recordView(live, reader, viewerKey);
        boolean bookmarked = reader.customerId() != null
            && bookmarkRepository.findByCustomerIdAndContentId(reader.customerId(), item.getId()).isPresent();
        return viewService.item(live, reader, bookmarked);
    }

    private KnowledgeArticle resolve(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            throw new ResourceNotFoundException("Content not found");
        }
        Optional<KnowledgeArticle> found = Optional.empty();
        if (idOrSlug.chars().allMatch(Character::isDigit) && idOrSlug.length() < 19) {
            found = articleRepository.findById(Long.parseLong(idOrSlug));
        }
        if (found.isEmpty()) {
            found = articleRepository.findFirstBySlug(idOrSlug);
        }
        return found.orElseThrow(() -> new ResourceNotFoundException("Content not found"));
    }

    // ---- products and hubs --------------------------------------------------

    public List<KnowledgeReaderDtos.ProductCard> products(KnowledgeReader reader) {
        return productCards(viewService.visible(reader, null), reader, accessService.catalogLinks());
    }

    private List<KnowledgeReaderDtos.ProductCard> productCards(List<KnowledgeViewService.Live> visible, KnowledgeReader reader,
                                                               Map<Long, Long> catalog) {
        Map<Long, Long> byProduct = visible.stream().filter(l -> l.item().getProductId() != null)
            .collect(Collectors.groupingBy(l -> l.item().getProductId(), Collectors.counting()));
        Map<Long, Long> byModule = visible.stream().filter(l -> l.item().getModuleId() != null)
            .collect(Collectors.groupingBy(l -> l.item().getModuleId(), Collectors.counting()));
        List<KnowledgeModule> modules = moduleRepository.findAllByOrderByProductIdAscDisplayOrderAscNameAsc();
        return productRepository.findAllByOrderByDisplayOrderAscNameAsc().stream().filter(KnowledgeProduct::isActive)
            .map(p -> new KnowledgeReaderDtos.ProductCard(p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getCatalogProductId(),
                modules.stream().filter(m -> m.getProductId().equals(p.getId()) && m.isActive())
                    .map(m -> new KnowledgeReaderDtos.ModuleCard(m.getId(), m.getName(), m.getSlug(), byModule.getOrDefault(m.getId(), 0L)))
                    .toList(),
                byProduct.getOrDefault(p.getId(), 0L),
                p.getCatalogProductId() != null && reader.productIds().contains(p.getCatalogProductId())))
            .toList();
    }

    public KnowledgeReaderDtos.ProductHub hub(KnowledgeReader reader, String slug) {
        KnowledgeProduct product = productRepository.findBySlug(slug).filter(KnowledgeProduct::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        List<KnowledgeViewService.Live> visible = viewService.visible(reader, null).stream()
            .filter(l -> product.getId().equals(l.item().getProductId())).toList();
        KnowledgeViewService.Lookups lookups = viewService.lookups(visible.stream().map(KnowledgeViewService.Live::item).toList());
        KnowledgeReaderDtos.ProductCard card = productCards(viewService.visible(reader, null), reader, accessService.catalogLinks())
            .stream().filter(c -> c.id().equals(product.getId())).findFirst().orElseThrow();
        List<KnowledgeReaderDtos.ModuleSection> sections = new ArrayList<>();
        for (KnowledgeReaderDtos.ModuleCard module : card.modules()) {
            List<KnowledgeSummaryDto> items = visible.stream().filter(l -> module.id().equals(l.item().getModuleId()))
                .sorted(Comparator.comparing((KnowledgeViewService.Live l) -> l.version().getPublishedAt()).reversed())
                .limit(12).map(l -> viewService.summary(l, lookups)).toList();
            sections.add(new KnowledgeReaderDtos.ModuleSection(module, items));
        }
        List<KnowledgeSummaryDto> general = visible.stream().filter(l -> l.item().getModuleId() == null)
            .limit(12).map(l -> viewService.summary(l, lookups)).toList();
        return new KnowledgeReaderDtos.ProductHub(card, sections, general);
    }

    // ---- sections -----------------------------------------------------------

    public List<KnowledgeReaderDtos.GlossaryTerm> glossary(KnowledgeReader reader) {
        return viewService.visible(reader, EnumSet.of(KnowledgeContentType.GLOSSARY_TERM)).stream()
            .map(l -> {
                JsonNode f = KnowledgeBlocks.parse(l.version().getTypeFields());
                String term = text(f, "term", l.version().getTitle());
                String definition = text(f, "definition", l.version().getBody());
                List<String> synonyms = new ArrayList<>();
                f.path("synonyms").forEach(s -> synonyms.add(s.asText()));
                return new KnowledgeReaderDtos.GlossaryTerm(l.item().getId(), l.item().getSlug(), term, definition, synonyms);
            })
            .sorted(Comparator.comparing(KnowledgeReaderDtos.GlossaryTerm::term, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    /** An error code (for example EIS-PO-001) — the ERROR_CODE item with that code. */
    @Transactional
    public KnowledgeItemDto errorCode(KnowledgeReader reader, String code, String viewerKey) {
        String wanted = code == null ? "" : code.trim();
        KnowledgeViewService.Live live = viewService.visible(reader, EnumSet.of(KnowledgeContentType.ERROR_CODE)).stream()
            .filter(l -> wanted.equalsIgnoreCase(text(KnowledgeBlocks.parse(l.version().getTypeFields()), "code", "")))
            .findFirst().orElseThrow(() -> new ResourceNotFoundException("Error code not found"));
        analyticsService.recordView(live, reader, viewerKey);
        return viewService.item(live, reader, false);
    }

    /** Release notes, newest release first, optionally for one product. */
    public List<KnowledgeItemDto> releaseNotes(KnowledgeReader reader, String productSlug) {
        Long productId = productSlug == null || productSlug.isBlank() ? null
            : productRepository.findBySlug(productSlug).map(KnowledgeProduct::getId).orElse(-1L);
        return viewService.visible(reader, EnumSet.of(KnowledgeContentType.RELEASE_NOTE)).stream()
            .filter(l -> productId == null || productId.equals(l.item().getProductId()))
            .sorted(Comparator.comparing((KnowledgeViewService.Live l) ->
                text(KnowledgeBlocks.parse(l.version().getTypeFields()), "releaseDate", "")).reversed()
                .thenComparing(l -> l.version().getPublishedAt(), Comparator.reverseOrder()))
            .limit(100)
            .map(l -> viewService.item(l, reader, false)).toList();
    }

    /** FAQs with their answers (accordions), filterable like lists. */
    public List<KnowledgeItemDto> faqs(KnowledgeReader reader, String productSlug, Long moduleId) {
        Long productId = productSlug == null || productSlug.isBlank() ? null
            : productRepository.findBySlug(productSlug).map(KnowledgeProduct::getId).orElse(-1L);
        return viewService.visible(reader, EnumSet.of(KnowledgeContentType.FAQ)).stream()
            .filter(l -> productId == null || productId.equals(l.item().getProductId()))
            .filter(l -> moduleId == null || moduleId.equals(l.item().getModuleId()))
            .sorted(Comparator.comparing((KnowledgeViewService.Live l) -> l.version().getTitle(), String.CASE_INSENSITIVE_ORDER))
            .limit(200)
            .map(l -> viewService.item(l, reader, false)).toList();
    }

    /** Workflow guides with their steps. */
    public List<KnowledgeItemDto> workflows(KnowledgeReader reader) {
        return viewService.visible(reader, EnumSet.of(KnowledgeContentType.WORKFLOW_GUIDE)).stream()
            .sorted(Comparator.comparing((KnowledgeViewService.Live l) -> l.version().getTitle(), String.CASE_INSENSITIVE_ORDER))
            .map(l -> viewService.item(l, reader, false)).toList();
    }

    /** Presigned download (REQ-KNW-003.8): only for a file used by content the reader may see. */
    @Transactional
    public KnowledgeMediaDtos.TemporaryUrl downloadUrl(KnowledgeReader reader, Long mediaId, String viewerKey) {
        KnowledgeMedia media = mediaService.find(mediaId);
        List<Long> using = mediaService.contentUsing(mediaId);
        List<KnowledgeViewService.Live> visible = viewService.filterVisible(articleRepository.findAllById(using), reader);
        if (visible.isEmpty()) {
            throw new ResourceNotFoundException("File not found");
        }
        KnowledgeMediaDtos.TemporaryUrl url = mediaService.temporaryUrl(media, true, settings.getDownloadUrlExpiry());
        analyticsService.recordDownload(visible.get(0), reader, viewerKey);
        return url;
    }

    // ---- knowledge search (C76) ---------------------------------------------

    public KnowledgeReaderDtos.SearchResult search(KnowledgeReader reader, String query, KnowledgeContentType type) {
        return searchService.search(reader, query, type, products(reader));
    }

    // ---- helpers ------------------------------------------------------------

    static String text(JsonNode fields, String name, String fallback) {
        JsonNode v = fields == null ? null : fields.get(name);
        return v == null || v.isNull() || v.asText().isBlank() ? fallback : v.asText();
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    static <T> List<T> nonNull(List<T> list) {
        return list.stream().filter(Objects::nonNull).toList();
    }
}
