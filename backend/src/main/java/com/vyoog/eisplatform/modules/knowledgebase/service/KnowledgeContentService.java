package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeCompareDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRowDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeItemDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePageDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePublishRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVersionDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.ArticleStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeAudience;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentVersion;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeModule;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeBookmarkRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeCategoryRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeContentVersionRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeEventRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeFeedbackRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeModuleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProductRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeProgressRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Knowledge Management: content items, workflow and versions (REQ-KNW-002,
 * REQ-KNW-008). Contributors create and edit drafts and submit them;
 * publishers review, approve, schedule, publish, deprecate, archive, restore
 * and delete (BR-KPRM-002). Allowed transitions only (BR-KCON-002); every
 * action is audited (BR-KPRM-005).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeContentService {

    /** Types offered in the editor; COURSE waits for the Academy (C77). */
    public static final Set<KnowledgeContentType> EDITABLE_TYPES = Stream.of(KnowledgeContentType.values())
        .filter(t -> t != KnowledgeContentType.COURSE).collect(Collectors.toUnmodifiableSet());

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeContentVersionRepository versionRepository;
    private final KnowledgeProductRepository productRepository;
    private final KnowledgeModuleRepository moduleRepository;
    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeVideoRepository videoRepository;
    private final KnowledgeFeedbackRepository feedbackRepository;
    private final KnowledgeBookmarkRepository bookmarkRepository;
    private final KnowledgeProgressRepository progressRepository;
    private final KnowledgeEventRepository eventRepository;
    private final KnowledgeViewService viewService;
    private final KnowledgeMediaService mediaService;
    private final AuditService auditService;

    // ---- list and read ------------------------------------------------------

    public KnowledgePageDto<KnowledgeContentRowDto> list(KnowledgeContentType type, String state, Long productId,
                                                         String query, String source, int page, int size) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Instant now = Instant.now();
        List<KnowledgeArticle> all = articleRepository.findAllByOrderByUpdatedAtDesc();
        Map<Long, KnowledgeVideo> videos = videoRepository.findAll().stream()
            .collect(Collectors.toMap(KnowledgeVideo::getContentId, v -> v));
        Map<Long, Long> views = viewService.viewCounts();
        List<KnowledgeArticle> filtered = all.stream()
            .filter(a -> type == null || a.getContentType() == type)
            .filter(a -> productId == null || productId.equals(a.getProductId()))
            .filter(a -> matchesState(a, state, now))
            .filter(a -> source == null || source.isBlank()
                || (videos.containsKey(a.getId()) && videos.get(a.getId()).getSourceType().name().equalsIgnoreCase(source)))
            .filter(a -> q.isEmpty() || contains(a.getTitle(), q) || contains(a.getShortDescription(), q)
                || contains(a.getTags(), q) || contains(a.getKeywords(), q))
            .toList();
        int safeSize = Math.max(1, Math.min(size, 100));
        int from = Math.min(Math.max(page, 0) * safeSize, filtered.size());
        List<KnowledgeContentRowDto> rows = filtered.subList(from, Math.min(from + safeSize, filtered.size())).stream()
            .map(a -> row(a, videos.get(a.getId()), views.getOrDefault(a.getId(), 0L), now)).toList();
        return new KnowledgePageDto<>(rows, filtered.size(), Math.max(page, 0), safeSize);
    }

    private static boolean matchesState(KnowledgeArticle a, String state, Instant now) {
        if (state == null || state.isBlank()) {
            return true;
        }
        return switch (state.toUpperCase(Locale.ROOT)) {
            case "EXPIRED" -> isExpired(a, now);
            case "REQUIRES_REVIEW" -> requiresReview(a, now);
            case "LIVE" -> a.getLiveVersionId() != null && a.getWorkflowState() != KnowledgeWorkflowState.ARCHIVED;
            default -> a.getWorkflowState().name().equalsIgnoreCase(state);
        };
    }

    public KnowledgeContentDto get(Long id) {
        return toDto(find(id));
    }

    public KnowledgeItemDto preview(Long id) {
        return viewService.preview(find(id), new KnowledgeReader(true, null, null, null, Set.of(), true));
    }

    // ---- create and edit ----------------------------------------------------

    @Transactional
    public KnowledgeContentDto create(KnowledgeActor actor, KnowledgeContentRequest request) {
        if (!EDITABLE_TYPES.contains(request.contentType())) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "This content type is not available yet.");
        }
        KnowledgeArticle a = new KnowledgeArticle();
        a.setContentType(request.contentType());
        a.setAuthorSub(actor.keycloakSub());
        a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        a.setStatus(ArticleStatus.DRAFT);
        a.setCurrentVersionLabel("0.1");
        a.setVersion(1);
        apply(a, request, actor);
        a = articleRepository.save(a);
        if (a.getSlug() == null || a.getSlug().isBlank()) {
            a.setSlug(uniqueSlug(a.getContentType(), a.getTitle(), a.getId()));
        }
        audit(actor, "KNOWLEDGE_CONTENT_CREATED", a, a.getContentType() + " created: " + a.getTitle());
        return toDto(a);
    }

    @Transactional
    public KnowledgeContentDto update(KnowledgeActor actor, Long id, KnowledgeContentRequest request) {
        KnowledgeArticle a = find(id);
        requireEditable(actor, a);
        if (request.contentType() != a.getContentType()) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The content type cannot be changed.");
        }
        apply(a, request, actor);
        a.setVersion(a.getVersion() + 1);
        // Editing published (or deprecated) content starts a new draft; the
        // published version stays live until the draft is published.
        if (a.getWorkflowState() == KnowledgeWorkflowState.PUBLISHED
            || a.getWorkflowState() == KnowledgeWorkflowState.DEPRECATED) {
            a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        }
        a = articleRepository.save(a);
        audit(actor, "KNOWLEDGE_CONTENT_EDITED", a, "Edited, edit " + a.getVersion());
        return toDto(a);
    }

    /** BR-KPRM-003: contributors edit only Draft (or start a draft from
     * Published/Deprecated); publishers can also edit while in review. */
    private void requireEditable(KnowledgeActor actor, KnowledgeArticle a) {
        KnowledgeWorkflowState s = a.getWorkflowState();
        if (s == KnowledgeWorkflowState.ARCHIVED) {
            throw KnowledgeException.conflict("Archived content must be restored before it can be edited.");
        }
        boolean contributorMay = s == KnowledgeWorkflowState.DRAFT || s == KnowledgeWorkflowState.PUBLISHED
            || s == KnowledgeWorkflowState.DEPRECATED;
        if (!contributorMay && !actor.publisher()) {
            throw KnowledgeException.forbidden("Content in review, approved or scheduled can only be changed by a publisher.");
        }
    }

    private void apply(KnowledgeArticle a, KnowledgeContentRequest r, KnowledgeActor actor) {
        a.setTitle(r.title().trim());
        a.setShortDescription(blankToNull(r.shortDescription()));
        validateTaxonomy(r.productId(), r.moduleId(), r.categoryId());
        a.setProductId(r.productId());
        a.setModuleId(r.moduleId());
        a.setCategoryId(r.categoryId());
        a.setFeature(blankToNull(r.feature()));
        a.setTags(r.tags() == null ? null : blankToNull(r.tags().stream().map(String::trim).filter(t -> !t.isEmpty())
            .map(t -> t.replace(",", " ")).distinct().limit(30).collect(Collectors.joining(","))));
        a.setKeywords(blankToNull(r.keywords()));
        KnowledgeAudience audience = r.audience() == null ? KnowledgeAudience.PUBLIC : r.audience();
        if (audience == KnowledgeAudience.ORGANIZATION && (r.audienceOrganizationIds() == null
            || r.audienceOrganizationIds().isEmpty())) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Choose at least one organization for this audience.");
        }
        a.setAudience(audience);
        a.setAudienceOrgIds(audience == KnowledgeAudience.ORGANIZATION
            ? KnowledgeAccessService.formatIds(r.audienceOrganizationIds()) : null);
        a.setRequireProductAccess(Boolean.TRUE.equals(r.requireProductAccess()));
        if (a.isRequireProductAccess() && r.productId() == null) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Choose a product to restrict content to product access.");
        }
        a.setProductVersion(blankToNull(r.productVersion()));
        a.setDocumentationVersion(blankToNull(r.documentationVersion()));
        if (r.effectiveAt() != null && r.expiresAt() != null && !r.expiresAt().isAfter(r.effectiveAt())) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The expiry date must be after the effective date.");
        }
        a.setEffectiveAt(r.effectiveAt());
        a.setReviewAt(r.reviewAt());
        a.setExpiresAt(r.expiresAt());
        a.setFeatured(Boolean.TRUE.equals(r.featured()));
        a.setDifficulty(normalizeDifficulty(r.difficulty()));
        String route = blankToNull(r.directActionRoute());
        if (route != null && KnowledgeViewService.safeRoute(route) == null) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The direct action must be an EIS page, for example /subscriptions.");
        }
        a.setDirectActionRoute(route);
        a.setDirectActionLabel(blankToNull(r.directActionLabel()));
        a.setRelatedContentIds(KnowledgeAccessService.formatIds(r.relatedContentIds() == null ? List.of()
            : r.relatedContentIds().stream().filter(cid -> !Objects.equals(cid, a.getId())).toList()));
        String blocks = KnowledgeBlocks.validate(r.blocks());
        a.setBlocks(blocks);
        a.setTypeFields(KnowledgeBlocks.validateTypeFields(r.typeFields()));
        String text = KnowledgeBlocks.plainText(blocks);
        if (text.isBlank()) {
            text = Objects.requireNonNullElse(a.getShortDescription(), a.getTitle());
        }
        a.setBody(text);
        a.setUpdatedBySub(actor.keycloakSub());
        String slug = blankToNull(r.slug());
        if (slug != null) {
            String normalized = slugify(slug);
            boolean taken = a.getId() == null ? articleRepository.existsByContentTypeAndSlug(a.getContentType(), normalized)
                : articleRepository.existsByContentTypeAndSlugAndIdNot(a.getContentType(), normalized, a.getId());
            if (taken) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Another item of this type already uses this slug.");
            }
            a.setSlug(normalized);
        } else if (a.getId() != null && (a.getSlug() == null || a.getSlug().isBlank())) {
            a.setSlug(uniqueSlug(a.getContentType(), a.getTitle(), a.getId()));
        }
        refreshSearchText(a);
    }

    private void validateTaxonomy(Long productId, Long moduleId, Long categoryId) {
        if (productId != null && !productRepository.existsById(productId)) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The product does not exist.");
        }
        if (moduleId != null) {
            KnowledgeModule module = moduleRepository.findById(moduleId).orElseThrow(
                () -> KnowledgeException.badRequest("INVALID_CONTENT", "The module does not exist."));
            if (productId == null || !productId.equals(module.getProductId())) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "The module does not belong to the product.");
            }
        }
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The category does not exist.");
        }
    }

    /** Recomputes the search-only text (type fields, taxonomy names, transcript, chapters). */
    public void refreshSearchText(KnowledgeArticle a) {
        List<String> parts = new ArrayList<>();
        parts.add(a.getContentType().name().replace('_', ' ').toLowerCase(Locale.ROOT));
        parts.add(KnowledgeBlocks.typeFieldsText(a.getTypeFields()));
        parts.add(a.getFeature());
        parts.add(a.getTags() == null ? null : a.getTags().replace(',', ' '));
        parts.add(a.getKeywords());
        parts.add(a.getShortDescription());
        if (a.getProductId() != null) {
            productRepository.findById(a.getProductId()).ifPresent(p -> parts.add(p.getName()));
        }
        if (a.getModuleId() != null) {
            moduleRepository.findById(a.getModuleId()).ifPresent(m -> parts.add(m.getName()));
        }
        if (a.getCategoryId() != null) {
            categoryRepository.findById(a.getCategoryId()).ifPresent(c -> parts.add(c.getName()));
        }
        if (a.getId() != null) {
            videoRepository.findByContentId(a.getId()).ifPresent(v -> {
                parts.add(v.getChannel());
                parts.add(v.getTranscript());
                JsonNode chapters = KnowledgeBlocks.parse(v.getChapters());
                chapters.forEach(c -> parts.add(c.path("title").asText(null)));
            });
        }
        String text = parts.stream().filter(p -> p != null && !p.isBlank()).collect(Collectors.joining("\n"));
        a.setSearchText(text.length() > 100_000 ? text.substring(0, 100_000) : text);
    }

    // ---- workflow -----------------------------------------------------------

    @Transactional
    public KnowledgeContentDto submit(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.DRAFT);
        if (a.getContentType() == KnowledgeContentType.VIDEO && videoRepository.findByContentId(a.getId()).isEmpty()) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Add the video source before submitting.");
        }
        a.setWorkflowState(KnowledgeWorkflowState.IN_REVIEW);
        a.setReviewComment(null);
        audit(actor, "KNOWLEDGE_CONTENT_SUBMITTED", a, "Submitted for review");
        return toDto(articleRepository.save(a));
    }

    @Transactional
    public KnowledgeContentDto approve(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.IN_REVIEW);
        a.setWorkflowState(KnowledgeWorkflowState.APPROVED);
        a.setReviewerSub(actor.keycloakSub());
        a.setApproverSub(actor.keycloakSub());
        audit(actor, "KNOWLEDGE_CONTENT_APPROVED", a, "Approved");
        return toDto(articleRepository.save(a));
    }

    @Transactional
    public KnowledgeContentDto returnToDraft(KnowledgeActor actor, Long id, String comment) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.IN_REVIEW, KnowledgeWorkflowState.APPROVED);
        a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        a.setReviewerSub(actor.keycloakSub());
        a.setReviewComment(blankToNull(comment));
        audit(actor, "KNOWLEDGE_CONTENT_RETURNED", a, "Returned to draft" + (comment == null ? "" : ": " + comment));
        return toDto(articleRepository.save(a));
    }

    @Transactional
    public KnowledgeContentDto publish(KnowledgeActor actor, Long id, KnowledgePublishRequest request) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.APPROVED);
        boolean major = request != null && "MAJOR".equalsIgnoreCase(request.versionBump());
        Instant scheduleAt = request == null ? null : request.scheduleAt();
        if (scheduleAt != null && scheduleAt.isAfter(Instant.now())) {
            a.setWorkflowState(KnowledgeWorkflowState.SCHEDULED);
            a.setScheduledAt(scheduleAt);
            a.setScheduledBump(major ? "MAJOR" : "MINOR");
            a.setApproverSub(actor.keycloakSub());
            audit(actor, "KNOWLEDGE_CONTENT_SCHEDULED", a, "Scheduled for " + scheduleAt);
            return toDto(articleRepository.save(a));
        }
        publishNow(a, major, actor.keycloakSub());
        audit(actor, "KNOWLEDGE_CONTENT_PUBLISHED", a, "Published version " + a.getCurrentVersionLabel());
        return toDto(a);
    }

    @Transactional
    public KnowledgeContentDto unschedule(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.SCHEDULED);
        a.setWorkflowState(KnowledgeWorkflowState.APPROVED);
        a.setScheduledAt(null);
        a.setScheduledBump(null);
        audit(actor, "KNOWLEDGE_CONTENT_UNSCHEDULED", a, "Schedule cancelled");
        return toDto(articleRepository.save(a));
    }

    /** Publishes every scheduled item whose time has come (job). */
    @Transactional
    public int publishDue(Instant now) {
        int published = 0;
        for (KnowledgeArticle a : articleRepository.findByWorkflowStateAndScheduledAtLessThanEqual(
                KnowledgeWorkflowState.SCHEDULED, now)) {
            publishNow(a, "MAJOR".equals(a.getScheduledBump()), a.getApproverSub());
            auditService.recordSuccess("KNOWLEDGE_CONTENT_PUBLISHED", null, null, null, "KnowledgeContent",
                a.getId().toString(), null, "Scheduled publish of version " + a.getCurrentVersionLabel());
            published++;
        }
        return published;
    }

    void publishNow(KnowledgeArticle a, boolean major, String bySub) {
        String label = nextLabel(a.getId(), major);
        KnowledgeContentVersion v = snapshot(a, label, bySub);
        a.setLiveVersionId(v.getId());
        a.setCurrentVersionLabel(label);
        a.setWorkflowState(KnowledgeWorkflowState.PUBLISHED);
        a.setStatus(ArticleStatus.PUBLISHED);
        a.setPublishedAt(v.getPublishedAt());
        a.setDeprecatedAt(null);
        a.setScheduledAt(null);
        a.setScheduledBump(null);
        a.setReviewComment(null);
        articleRepository.save(a);
    }

    KnowledgeContentVersion snapshot(KnowledgeArticle a, String label, String bySub) {
        KnowledgeContentVersion v = new KnowledgeContentVersion();
        v.setContentId(a.getId());
        v.setVersionLabel(label);
        v.setTitle(a.getTitle());
        v.setShortDescription(a.getShortDescription());
        v.setBlocks(a.getBlocks() == null ? KnowledgeBlocks.paragraph(a.getBody()) : a.getBlocks());
        v.setTypeFields(a.getTypeFields());
        v.setBody(a.getBody());
        v.setSearchText(a.getSearchText());
        v.setAudience(a.getAudience());
        v.setAudienceOrgIds(a.getAudienceOrgIds());
        v.setRequireProductAccess(a.isRequireProductAccess());
        v.setEffectiveAt(a.getEffectiveAt());
        v.setExpiresAt(a.getExpiresAt());
        v.setPublishedBySub(bySub);
        v.setPublishedAt(Instant.now());
        return versionRepository.save(v);
    }

    /** 1.0 for the first publish; then minor (1.1) or major (2.0). */
    String nextLabel(Long contentId, boolean major) {
        int[] highest = versionRepository.findByContentIdOrderByPublishedAtDescIdDesc(contentId).stream()
            .map(v -> parseLabel(v.getVersionLabel()))
            .max(Comparator.<int[]>comparingInt(x -> x[0]).thenComparingInt(x -> x[1]))
            .orElse(null);
        if (highest == null) {
            return "1.0";
        }
        return major ? (highest[0] + 1) + ".0" : highest[0] + "." + (highest[1] + 1);
    }

    static int[] parseLabel(String label) {
        try {
            String[] parts = label.split("\\.");
            return new int[] {Integer.parseInt(parts[0]), parts.length > 1 ? Integer.parseInt(parts[1]) : 0};
        } catch (RuntimeException e) {
            return new int[] {0, 0};
        }
    }

    @Transactional
    public KnowledgeContentDto unpublish(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        if (a.getLiveVersionId() == null) {
            throw KnowledgeException.conflict("This content is not published.");
        }
        a.setLiveVersionId(null);
        a.setStatus(ArticleStatus.DRAFT);
        a.setDeprecatedAt(null);
        if (a.getWorkflowState() == KnowledgeWorkflowState.PUBLISHED || a.getWorkflowState() == KnowledgeWorkflowState.DEPRECATED) {
            a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        }
        audit(actor, "KNOWLEDGE_CONTENT_UNPUBLISHED", a, "Unpublished");
        return toDto(articleRepository.save(a));
    }

    @Transactional
    public KnowledgeContentDto deprecate(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.PUBLISHED);
        a.setWorkflowState(KnowledgeWorkflowState.DEPRECATED);
        a.setDeprecatedAt(Instant.now());
        audit(actor, "KNOWLEDGE_CONTENT_DEPRECATED", a, "Deprecated");
        return toDto(articleRepository.save(a));
    }

    @Transactional
    public KnowledgeContentDto archive(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.PUBLISHED, KnowledgeWorkflowState.DEPRECATED, KnowledgeWorkflowState.DRAFT);
        a.setWorkflowState(KnowledgeWorkflowState.ARCHIVED);
        a.setLiveVersionId(null);
        a.setStatus(ArticleStatus.DRAFT);
        audit(actor, "KNOWLEDGE_CONTENT_ARCHIVED", a, "Archived");
        return toDto(articleRepository.save(a));
    }

    @Transactional
    public KnowledgeContentDto restore(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        requireState(a, KnowledgeWorkflowState.ARCHIVED);
        a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        a.setDeprecatedAt(null);
        audit(actor, "KNOWLEDGE_CONTENT_RESTORED", a, "Restored to draft");
        return toDto(articleRepository.save(a));
    }

    @Transactional
    public void delete(KnowledgeActor actor, Long id) {
        KnowledgeArticle a = find(id);
        deleteCascade(a, actor);
        audit(actor, "KNOWLEDGE_CONTENT_DELETED", a, a.getContentType() + " deleted: " + a.getTitle());
    }

    /** Removes the item with its versions, video (and stored files), feedback, bookmarks, progress and events. */
    @Transactional
    public void deleteCascade(KnowledgeArticle a, KnowledgeActor actor) {
        Long id = a.getId();
        videoRepository.findByContentId(id).ifPresent(video -> {
            mediaService.deleteVideoFiles(video, actor);
            videoRepository.delete(video);
        });
        versionRepository.deleteByContentId(id);
        feedbackRepository.deleteByContentId(id);
        bookmarkRepository.deleteByContentId(id);
        progressRepository.deleteByContentId(id);
        eventRepository.deleteByContentId(id);
        articleRepository.delete(a);
    }

    // ---- versions -----------------------------------------------------------

    public List<KnowledgeVersionDto> versions(Long id) {
        KnowledgeArticle a = find(id);
        return versionRepository.findByContentIdOrderByPublishedAtDescIdDesc(id).stream()
            .map(v -> versionDto(v, v.getId().equals(a.getLiveVersionId()))).toList();
    }

    public KnowledgeVersionDto version(Long id, String label) {
        KnowledgeArticle a = find(id);
        KnowledgeContentVersion v = findVersion(id, label);
        return versionDto(v, v.getId().equals(a.getLiveVersionId()));
    }

    /** Block-by-block comparison; "draft" is the working copy. */
    public KnowledgeCompareDto compare(Long id, String from, String to) {
        KnowledgeArticle a = find(id);
        String[] left = sideOf(a, from);
        String[] right = sideOf(a, to);
        JsonNode before = KnowledgeBlocks.parse(left[1]);
        JsonNode after = KnowledgeBlocks.parse(right[1]);
        List<KnowledgeCompareDto.Change> changes = new ArrayList<>();
        int n = Math.max(before.size(), after.size());
        for (int i = 0; i < n; i++) {
            JsonNode b = i < before.size() ? before.get(i) : null;
            JsonNode c = i < after.size() ? after.get(i) : null;
            String kind = b == null ? "ADDED" : c == null ? "REMOVED" : b.equals(c) ? "SAME" : "CHANGED";
            changes.add(new KnowledgeCompareDto.Change(i, kind, b, c));
        }
        return new KnowledgeCompareDto(from, to, !Objects.equals(left[0], right[0]), left[0], right[0], changes);
    }

    private String[] sideOf(KnowledgeArticle a, String label) {
        if (label == null || label.isBlank() || "draft".equalsIgnoreCase(label)) {
            return new String[] {a.getTitle(), a.getBlocks()};
        }
        KnowledgeContentVersion v = findVersion(a.getId(), label);
        return new String[] {v.getTitle(), v.getBlocks()};
    }

    /** Copies a snapshot into a new draft; history is never changed (BR-KCON-004). */
    @Transactional
    public KnowledgeContentDto restoreVersion(KnowledgeActor actor, Long id, String label) {
        KnowledgeArticle a = find(id);
        if (a.getWorkflowState() == KnowledgeWorkflowState.ARCHIVED) {
            throw KnowledgeException.conflict("Archived content must be restored before a version can be restored.");
        }
        KnowledgeContentVersion v = findVersion(id, label);
        a.setTitle(v.getTitle());
        a.setShortDescription(v.getShortDescription());
        a.setBlocks(v.getBlocks());
        a.setTypeFields(v.getTypeFields());
        a.setBody(v.getBody());
        a.setAudience(v.getAudience());
        a.setAudienceOrgIds(v.getAudienceOrgIds());
        a.setRequireProductAccess(v.isRequireProductAccess());
        a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        a.setVersion(a.getVersion() + 1);
        refreshSearchText(a);
        audit(actor, "KNOWLEDGE_CONTENT_VERSION_RESTORED", a, "Version " + label + " restored into a draft");
        return toDto(articleRepository.save(a));
    }

    // ---- helpers ------------------------------------------------------------

    public KnowledgeArticle find(Long id) {
        return articleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Content not found"));
    }

    private KnowledgeContentVersion findVersion(Long id, String label) {
        return versionRepository.findByContentIdAndVersionLabel(id, label)
            .orElseThrow(() -> new ResourceNotFoundException("Version not found"));
    }

    private static void requireState(KnowledgeArticle a, KnowledgeWorkflowState... allowed) {
        for (KnowledgeWorkflowState s : allowed) {
            if (a.getWorkflowState() == s) {
                return;
            }
        }
        throw KnowledgeException.conflict("This action is not allowed while the content is "
            + a.getWorkflowState().name().toLowerCase(Locale.ROOT).replace('_', ' ') + ".");
    }

    private void audit(KnowledgeActor actor, String action, KnowledgeArticle a, String detail) {
        auditService.recordSuccess(action, actor.keycloakSub(), null, actor.email(), "KnowledgeContent",
            String.valueOf(a.getId()), null, detail);
    }

    public KnowledgeContentDto toDto(KnowledgeArticle a) {
        Instant now = Instant.now();
        String liveLabel = null;
        if (a.getLiveVersionId() != null) {
            liveLabel = versionRepository.findById(a.getLiveVersionId()).map(KnowledgeContentVersion::getVersionLabel).orElse(null);
        }
        KnowledgeVideo video = a.getId() == null ? null : videoRepository.findByContentId(a.getId()).orElse(null);
        return new KnowledgeContentDto(a.getId(), a.getContentType(), a.getTitle(), a.getShortDescription(), a.getSlug(),
            a.getProductId(), a.getModuleId(), a.getCategoryId(), a.getFeature(), KnowledgeBlocks.tagList(a.getTags()),
            a.getKeywords(), a.getAudience(), new ArrayList<>(KnowledgeAccessService.parseIds(a.getAudienceOrgIds())),
            a.isRequireProductAccess(), a.getProductVersion(), a.getDocumentationVersion(), a.getEffectiveAt(),
            a.getReviewAt(), a.getExpiresAt(), a.getScheduledAt(), a.isFeatured(), a.getDifficulty(),
            a.getDirectActionRoute(), a.getDirectActionLabel(),
            new ArrayList<>(KnowledgeAccessService.parseIds(a.getRelatedContentIds())),
            a.getBlocks() == null ? KnowledgeBlocks.parse(KnowledgeBlocks.paragraph(a.getBody())) : KnowledgeBlocks.parse(a.getBlocks()),
            a.getTypeFields() == null ? JsonNodeFactory.instance.objectNode() : KnowledgeBlocks.parse(a.getTypeFields()),
            a.getWorkflowState(), a.getLiveVersionId() != null, liveLabel, isExpired(a, now), requiresReview(a, now),
            a.getReviewComment(), a.getAuthorSub(), a.getReviewerSub(), a.getApproverSub(), a.getCreatedAt(),
            a.getUpdatedAt(), a.getPublishedAt(), video == null ? null : viewService.videoDto(video, false));
    }

    private KnowledgeContentRowDto row(KnowledgeArticle a, KnowledgeVideo video, long views, Instant now) {
        return new KnowledgeContentRowDto(a.getId(), a.getContentType(), a.getTitle(), a.getShortDescription(),
            a.getProductId(), a.getModuleId(), a.getCategoryId(), KnowledgeBlocks.tagList(a.getTags()), a.getAudience(),
            a.getWorkflowState(), a.getLiveVersionId() != null, a.getLiveVersionId() == null ? null : a.getCurrentVersionLabel(),
            isExpired(a, now), requiresReview(a, now), a.isFeatured(), views, a.getUpdatedAt(),
            video == null ? null : video.getSourceType().name(), video == null ? null : video.getDurationSeconds(),
            video == null ? null : viewService.thumbnail(video));
    }

    private KnowledgeVersionDto versionDto(KnowledgeContentVersion v, boolean live) {
        return new KnowledgeVersionDto(v.getId(), v.getVersionLabel(), v.getTitle(), v.getShortDescription(),
            KnowledgeBlocks.parse(v.getBlocks()), v.getTypeFields() == null ? null : KnowledgeBlocks.parse(v.getTypeFields()),
            v.getAudience(), live, v.getPublishedBySub(), v.getPublishedAt());
    }

    static boolean isExpired(KnowledgeArticle a, Instant now) {
        return a.getExpiresAt() != null && !a.getExpiresAt().isAfter(now);
    }

    static boolean requiresReview(KnowledgeArticle a, Instant now) {
        return a.getReviewAt() != null && !a.getReviewAt().isAfter(now)
            && a.getWorkflowState() != KnowledgeWorkflowState.ARCHIVED;
    }

    private String uniqueSlug(KnowledgeContentType type, String title, Long id) {
        String base = slugify(title);
        if (base.isEmpty()) {
            base = type.name().toLowerCase(Locale.ROOT).replace('_', '-');
        }
        if (!articleRepository.existsByContentTypeAndSlugAndIdNot(type, base, id == null ? -1L : id)) {
            return base;
        }
        return base + "-" + id;
    }

    static String slugify(String text) {
        if (text == null) {
            return "";
        }
        String folded = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = folded.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
        return slug.length() > 200 ? slug.substring(0, 200) : slug;
    }

    private static String normalizeDifficulty(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(upper)) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Difficulty must be Beginner, Intermediate or Advanced.");
        }
        return upper;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}
