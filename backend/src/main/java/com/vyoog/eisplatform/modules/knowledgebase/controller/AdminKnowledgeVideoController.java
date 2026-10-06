package com.vyoog.eisplatform.modules.knowledgebase.controller;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeContentRowDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePageDto;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentType;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAccessService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeActor;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeContentService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeMediaService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeVideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Video management (REQ-KNW-004): YouTube, hosted (S3) and external videos. */
@RestController
@RequestMapping("/admin/knowledge/videos")
@RequiredArgsConstructor
public class AdminKnowledgeVideoController {

    private final KnowledgeAccessService accessService;
    private final KnowledgeVideoService videoService;
    private final KnowledgeContentService contentService;
    private final KnowledgeMediaService mediaService;

    @GetMapping
    public KnowledgePageDto<KnowledgeContentRowDto> list(@RequestParam(value = "status", required = false) String status,
                                                         @RequestParam(value = "product", required = false) Long product,
                                                         @RequestParam(value = "source", required = false) String source,
                                                         @RequestParam(value = "q", required = false) String q,
                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "25") int size,
                                                         Authentication auth) {
        accessService.actor(auth);
        return contentService.list(KnowledgeContentType.VIDEO, status, product, q, source, page, size);
    }

    @GetMapping("/summary")
    public KnowledgeVideoDtos.Summary summary(Authentication auth) {
        accessService.actor(auth);
        return videoService.summary();
    }

    /** Upload URL for a video file (kind VIDEO_FILE; MP4, WebM, MOV). */
    @PostMapping("/upload-url")
    public KnowledgeMediaDtos.UploadTicket uploadUrl(@RequestBody KnowledgeMediaDtos.UploadRequest body, Authentication auth) {
        KnowledgeMediaDtos.UploadRequest video = new KnowledgeMediaDtos.UploadRequest(body.fileName(), body.contentType(),
            body.size(), KnowledgeMediaKind.VIDEO_FILE, body.productId(), body.moduleId(), body.folder());
        return mediaService.requestUpload(accessService.actor(auth), video);
    }

    @PostMapping("/uploads/{mediaId}/complete-upload")
    public KnowledgeMediaDtos.Media completeUpload(@PathVariable("mediaId") Long mediaId,
                                                   @RequestBody(required = false) KnowledgeMediaDtos.CompleteRequest body,
                                                   Authentication auth) {
        return mediaService.completeUpload(accessService.actor(auth), mediaId, body);
    }

    @PostMapping
    public KnowledgeContentDto create(@Valid @RequestBody KnowledgeVideoDtos.VideoRequest body, Authentication auth) {
        return videoService.create(accessService.actor(auth), body);
    }

    @PutMapping("/{id}")
    public KnowledgeContentDto update(@PathVariable("id") Long id, @Valid @RequestBody KnowledgeVideoDtos.VideoRequest body,
                                      Authentication auth) {
        return videoService.update(accessService.actor(auth), id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, Authentication auth) {
        KnowledgeActor actor = accessService.actor(auth);
        accessService.requirePublisher(actor);
        contentService.delete(actor, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/replace")
    public KnowledgeMediaDtos.UploadTicket replace(@PathVariable("id") Long id,
                                                   @RequestBody KnowledgeMediaDtos.UploadRequest body, Authentication auth) {
        KnowledgeActor actor = accessService.actor(auth);
        accessService.requirePublisher(actor);
        return videoService.replaceFile(actor, id, body);
    }

    @PostMapping("/youtube/fetch-details")
    public KnowledgeVideoDtos.YouTubeDetails fetchYouTube(@RequestBody KnowledgeVideoDtos.YouTubeFetchRequest body,
                                                          Authentication auth) {
        accessService.actor(auth);
        return videoService.fetchYouTube(body.url());
    }

    @PutMapping("/{id}/transcript")
    public KnowledgeContentDto transcript(@PathVariable("id") Long id, @RequestBody KnowledgeVideoDtos.TextTrackRequest body,
                                          Authentication auth) {
        return videoService.setTranscript(accessService.actor(auth), id, body.text());
    }

    @PutMapping("/{id}/chapters")
    public KnowledgeContentDto chapters(@PathVariable("id") Long id, @RequestBody KnowledgeVideoDtos.TextTrackRequest body,
                                        Authentication auth) {
        return videoService.setChapters(accessService.actor(auth), id, body.text());
    }

    @PutMapping("/{id}/subtitles/{language}")
    public KnowledgeContentDto subtitle(@PathVariable("id") Long id, @PathVariable("language") String language,
                                        @RequestBody KnowledgeVideoDtos.SubtitleRequest body, Authentication auth) {
        return videoService.setSubtitle(accessService.actor(auth), id, language, body.mediaId());
    }

    /** Staff preview playback of any video (live or not). */
    @GetMapping("/{id}/preview-play-url")
    public KnowledgeVideoDtos.PlayInfo previewPlay(@PathVariable("id") Long id, Authentication auth) {
        accessService.actor(auth);
        return videoService.previewPlayInfo(id);
    }
}
