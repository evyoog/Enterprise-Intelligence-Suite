package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRequest;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeArticle;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMedia;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeWorkflowState;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeArticleRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeMediaRepository;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Video management (REQ-KNW-004). A video is a VIDEO content item (title,
 * taxonomy, audience, workflow — REQ-KNW-002) plus its source and text
 * tracks. Sources are {@link VideoSourceProvider}s (YouTube, S3, external).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KnowledgeVideoService {

    private static final Pattern CHAPTER = Pattern.compile("^\\s*(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})\\s+(.+?)\\s*$");
    private static final int MAX_TRANSCRIPT = 500_000;

    private final KnowledgeContentService contentService;
    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeVideoRepository videoRepository;
    private final KnowledgeMediaRepository mediaRepository;
    private final KnowledgeMediaService mediaService;
    private final KnowledgeViewService viewService;
    private final YouTubeVideoService youTube;
    private final List<VideoSourceProvider> providerList;
    private final AuditService auditService;

    private Map<VideoSourceType, VideoSourceProvider> providers() {
        Map<VideoSourceType, VideoSourceProvider> map = new EnumMap<>(VideoSourceType.class);
        providerList.forEach(p -> map.put(p.type(), p));
        return map;
    }

    @Transactional
    public KnowledgeContentDto create(KnowledgeActor actor, KnowledgeVideoDtos.VideoRequest request) {
        requireVideoMetadata(request.content());
        KnowledgeVideo video = new KnowledgeVideo();
        video.setSourceType(request.sourceType());
        applySource(video, request);
        KnowledgeContentDto created = contentService.create(actor, request.content());
        video.setContentId(created.id());
        videoRepository.save(video);
        KnowledgeArticle a = contentService.find(created.id());
        contentService.refreshSearchText(a);
        articleRepository.save(a);
        return contentService.toDto(a);
    }

    @Transactional
    public KnowledgeContentDto update(KnowledgeActor actor, Long contentId, KnowledgeVideoDtos.VideoRequest request) {
        requireVideoMetadata(request.content());
        KnowledgeVideo video = findVideo(contentId);
        if (video.getSourceType() == VideoSourceType.AWS_S3 && request.sourceType() != VideoSourceType.AWS_S3
            && video.getMediaId() != null && !actor.publisher()) {
            throw KnowledgeException.forbidden("Only a publisher can replace a hosted video.");
        }
        Long previousMedia = video.getMediaId();
        video.setSourceType(request.sourceType());
        applySource(video, request);
        contentService.update(actor, contentId, request.content());
        videoRepository.save(video);
        if (previousMedia != null && !previousMedia.equals(video.getMediaId())) {
            mediaRepository.findById(previousMedia).ifPresent(m -> mediaService.delete(actor, m.getId(), true));
        }
        return touch(contentId);
    }

    private void applySource(KnowledgeVideo video, KnowledgeVideoDtos.VideoRequest request) {
        VideoSourceProvider provider = providers().get(request.sourceType());
        if (provider == null) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Unknown video source.");
        }
        provider.apply(video, request);
        if (request.durationSeconds() != null && request.durationSeconds() < 0) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The duration cannot be negative.");
        }
        video.setDurationSeconds(request.durationSeconds());
        video.setChannel(request.channel() == null || request.channel().isBlank() ? null : request.channel().trim());
        if (request.thumbnailMediaId() != null) {
            video.setThumbnailMediaId(requireReadyMedia(request.thumbnailMediaId(), KnowledgeMediaKind.THUMBNAIL,
                KnowledgeMediaKind.IMAGE).getId());
        } else {
            video.setThumbnailMediaId(null);
        }
        if (request.transcript() != null) {
            video.setTranscript(cleanTranscript(request.transcript()));
        }
        if (request.chapters() != null) {
            video.setChapters(parseChapters(request.chapters(), video.getDurationSeconds()));
        }
    }

    /** REQ-KNW-004.4: title, product, module and category are required for videos. */
    private static void requireVideoMetadata(KnowledgeContentRequest content) {
        if (content.contentType() != KnowledgeContentType.VIDEO) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "A video must have content type VIDEO.");
        }
        if (content.productId() == null || content.moduleId() == null || content.categoryId() == null) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Choose a product, module and category for the video.");
        }
    }

    public KnowledgeVideoDtos.YouTubeDetails fetchYouTube(String url) {
        return youTube.fetchDetails(url);
    }

    @Transactional
    public KnowledgeContentDto setTranscript(KnowledgeActor actor, Long contentId, String text) {
        KnowledgeVideo video = findVideo(contentId);
        requireEditable(actor, contentId);
        video.setTranscript(cleanTranscript(text));
        videoRepository.save(video);
        return touch(contentId);
    }

    @Transactional
    public KnowledgeContentDto setChapters(KnowledgeActor actor, Long contentId, String text) {
        KnowledgeVideo video = findVideo(contentId);
        requireEditable(actor, contentId);
        video.setChapters(parseChapters(text, video.getDurationSeconds()));
        videoRepository.save(video);
        return touch(contentId);
    }

    @Transactional
    public KnowledgeContentDto setSubtitle(KnowledgeActor actor, Long contentId, String language, Long mediaId) {
        KnowledgeVideo video = findVideo(contentId);
        requireEditable(actor, contentId);
        String lang = language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
        if (!lang.matches("^[a-z]{2}(-[a-z]{2})?$")) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "Use a language code such as en or es.");
        }
        ObjectNode tracks = KnowledgeBlocks.parse(video.getSubtitles()).isObject()
            ? (ObjectNode) KnowledgeBlocks.parse(video.getSubtitles()) : JsonNodeFactory.instance.objectNode();
        if (mediaId == null) {
            tracks.remove(lang);
        } else {
            tracks.put(lang, requireReadyMedia(mediaId, KnowledgeMediaKind.SUBTITLE).getId());
        }
        video.setSubtitles(tracks.isEmpty() ? null : tracks.toString());
        videoRepository.save(video);
        return touch(contentId);
    }

    /** Publisher: replace the hosted file (REQ-KNW-004.9). */
    @Transactional
    public KnowledgeMediaDtos.UploadTicket replaceFile(KnowledgeActor actor, Long contentId,
                                                       KnowledgeMediaDtos.UploadRequest request) {
        KnowledgeVideo video = findVideo(contentId);
        if (video.getSourceType() != VideoSourceType.AWS_S3 || video.getMediaId() == null) {
            throw KnowledgeException.conflict("Only a hosted (AWS S3) video has a file to replace.");
        }
        auditService.recordSuccess("KNOWLEDGE_VIDEO_REPLACE_STARTED", actor.keycloakSub(), null, actor.email(),
            "KnowledgeContent", contentId.toString(), null, "Replacement upload started");
        return mediaService.requestReplace(actor, video.getMediaId(), request);
    }

    public KnowledgeVideoDtos.Summary summary() {
        List<KnowledgeArticle> videos = articleRepository.findByContentTypeOrderByUpdatedAtDesc(KnowledgeContentType.VIDEO);
        Map<Long, Long> views = viewService.viewCounts();
        long published = videos.stream().filter(a -> a.getLiveVersionId() != null
            && a.getWorkflowState() != KnowledgeWorkflowState.ARCHIVED).count();
        long draft = videos.stream().filter(a -> a.getWorkflowState() == KnowledgeWorkflowState.DRAFT).count();
        long review = videos.stream().filter(a -> a.getWorkflowState() == KnowledgeWorkflowState.IN_REVIEW).count();
        long totalViews = videos.stream().mapToLong(a -> views.getOrDefault(a.getId(), 0L)).sum();
        return new KnowledgeVideoDtos.Summary(videos.size(), published, draft, review, totalViews);
    }

    /** BR-KVID-003: play info only for a reader who may see the video; otherwise 404. */
    public KnowledgeVideoDtos.PlayInfo playInfo(KnowledgeReader reader, Long contentId) {
        KnowledgeArticle a = articleRepository.findById(contentId)
            .filter(x -> x.getContentType() == KnowledgeContentType.VIDEO)
            .orElseThrow(() -> new ResourceNotFoundException("Video not found"));
        if (viewService.visibleItem(a, reader).isEmpty()) {
            throw new ResourceNotFoundException("Video not found");
        }
        KnowledgeVideo video = findVideo(contentId);
        return providers().get(video.getSourceType()).playInfo(video);
    }

    /** Staff preview of any video, live or not. */
    public KnowledgeVideoDtos.PlayInfo previewPlayInfo(Long contentId) {
        KnowledgeVideo video = findVideo(contentId);
        return providers().get(video.getSourceType()).playInfo(video);
    }

    private KnowledgeContentDto touch(Long contentId) {
        KnowledgeArticle a = contentService.find(contentId);
        contentService.refreshSearchText(a);
        a.setVersion(a.getVersion() + 1);
        if (a.getWorkflowState() == KnowledgeWorkflowState.PUBLISHED || a.getWorkflowState() == KnowledgeWorkflowState.DEPRECATED) {
            a.setWorkflowState(KnowledgeWorkflowState.DRAFT);
        }
        articleRepository.save(a);
        return contentService.toDto(a);
    }

    private void requireEditable(KnowledgeActor actor, Long contentId) {
        KnowledgeArticle a = contentService.find(contentId);
        KnowledgeWorkflowState s = a.getWorkflowState();
        if (s == KnowledgeWorkflowState.ARCHIVED) {
            throw KnowledgeException.conflict("Archived content must be restored before it can be edited.");
        }
        if (!actor.publisher() && s != KnowledgeWorkflowState.DRAFT && s != KnowledgeWorkflowState.PUBLISHED
            && s != KnowledgeWorkflowState.DEPRECATED) {
            throw KnowledgeException.forbidden("Content in review, approved or scheduled can only be changed by a publisher.");
        }
    }

    private KnowledgeVideo findVideo(Long contentId) {
        return videoRepository.findByContentId(contentId).orElseThrow(() -> new ResourceNotFoundException("Video not found"));
    }

    private KnowledgeMedia requireReadyMedia(Long mediaId, KnowledgeMediaKind... kinds) {
        KnowledgeMedia media = mediaRepository.findById(mediaId).orElseThrow(
            () -> KnowledgeException.badRequest("UPLOAD_NOT_FOUND", "The file was not found."));
        boolean kindOk = java.util.Arrays.asList(kinds).contains(media.getKind());
        if (!kindOk || media.getStatus() != KnowledgeMediaStatus.READY) {
            throw KnowledgeException.badRequest("UPLOAD_NOT_FOUND", "The file is not a completed upload of the right kind.");
        }
        return media;
    }

    /** BR-KVID-005: VTT/SRT cue numbers and times are dropped; text stays text. */
    static String cleanTranscript(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String cleaned = text.lines()
            .filter(line -> !line.trim().equals("WEBVTT"))
            .filter(line -> !line.trim().matches("^\\d+$"))
            .filter(line -> !line.contains("-->"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(Collectors.joining("\n"));
        return cleaned.length() > MAX_TRANSCRIPT ? cleaned.substring(0, MAX_TRANSCRIPT) : cleaned;
    }

    /** BR-KVID-004: "mm:ss Title" or "hh:mm:ss Title" lines, increasing, within the duration. */
    static String parseChapters(String text, Integer durationSeconds) {
        if (text == null || text.isBlank()) {
            return null;
        }
        ArrayNode chapters = JsonNodeFactory.instance.arrayNode();
        int previous = -1;
        int lineNumber = 0;
        for (String line : text.lines().toList()) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            Matcher m = CHAPTER.matcher(line);
            if (!m.matches()) {
                throw KnowledgeException.badRequest("INVALID_CONTENT",
                    "Chapter line " + lineNumber + " must look like \"02:15 Inventory setup\".");
            }
            int h = m.group(1) == null ? 0 : Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            int sec = Integer.parseInt(m.group(3));
            if (sec > 59 || (m.group(1) != null && min > 59)) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Chapter line " + lineNumber + " has an invalid time.");
            }
            int seconds = h * 3600 + min * 60 + sec;
            if (seconds <= previous) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Chapters must be in increasing time order.");
            }
            if (durationSeconds != null && seconds > durationSeconds) {
                throw KnowledgeException.badRequest("INVALID_CONTENT", "Chapter line " + lineNumber + " is after the end of the video.");
            }
            previous = seconds;
            ObjectNode chapter = chapters.addObject();
            chapter.put("seconds", seconds);
            String title = m.group(4);
            chapter.put("title", title.length() > 200 ? title.substring(0, 200) : title);
        }
        return chapters.isEmpty() ? null : chapters.toString();
    }

    Map<Long, KnowledgeVideo> byContent(List<Long> ids) {
        return videoRepository.findByContentIdIn(ids).stream().collect(Collectors.toMap(KnowledgeVideo::getContentId, Function.identity()));
    }
}
