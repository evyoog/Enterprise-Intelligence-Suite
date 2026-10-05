package com.vyoog.eisplatform.modules.knowledgebase.controller;

import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgePageDto;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeAccessService;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeActor;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Media library (REQ-KNW-003). Uploads go browser → S3 with presigned URLs;
 * this controller only hands out short-lived URLs and verifies, replaces and
 * deletes. Replace and delete are publisher-only (BR-KPRM-002).
 */
@RestController
@RequestMapping("/admin/knowledge/media")
@RequiredArgsConstructor
public class AdminKnowledgeMediaController {

    private final KnowledgeAccessService accessService;
    private final KnowledgeMediaService mediaService;

    @GetMapping("/storage")
    public KnowledgeMediaDtos.StorageStatus storage(Authentication auth) {
        accessService.actor(auth);
        return mediaService.storageStatus();
    }

    @PostMapping("/upload-url")
    public KnowledgeMediaDtos.UploadTicket uploadUrl(@RequestBody KnowledgeMediaDtos.UploadRequest body, Authentication auth) {
        return mediaService.requestUpload(accessService.actor(auth), body);
    }

    @PostMapping("/{id}/complete-upload")
    public KnowledgeMediaDtos.Media complete(@PathVariable("id") Long id,
                                             @RequestBody(required = false) KnowledgeMediaDtos.CompleteRequest body,
                                             Authentication auth) {
        return mediaService.completeUpload(accessService.actor(auth), id, body);
    }

    @PostMapping("/{id}/abort")
    public ResponseEntity<Void> abort(@PathVariable("id") Long id, Authentication auth) {
        mediaService.abort(accessService.actor(auth), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public KnowledgePageDto<KnowledgeMediaDtos.Media> list(@RequestParam(value = "kind", required = false) KnowledgeMediaKind kind,
                                                          @RequestParam(value = "q", required = false) String q,
                                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "size", defaultValue = "25") int size,
                                                          Authentication auth) {
        KnowledgeActor actor = accessService.actor(auth);
        return mediaService.list(kind, q, page, size, actor.publisher());
    }

    @GetMapping("/{id}")
    public KnowledgeMediaDtos.Media get(@PathVariable("id") Long id, Authentication auth) {
        KnowledgeActor actor = accessService.actor(auth);
        return mediaService.get(id, actor.publisher());
    }

    @GetMapping("/{id}/preview-url")
    public KnowledgeMediaDtos.TemporaryUrl preview(@PathVariable("id") Long id, Authentication auth) {
        accessService.actor(auth);
        return mediaService.previewUrl(id);
    }

    @PostMapping("/{id}/replace")
    public KnowledgeMediaDtos.UploadTicket replace(@PathVariable("id") Long id,
                                                   @RequestBody KnowledgeMediaDtos.UploadRequest body, Authentication auth) {
        return mediaService.requestReplace(publisher(auth), id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id,
                                       @RequestParam(value = "confirm", defaultValue = "false") boolean confirm,
                                       Authentication auth) {
        mediaService.delete(publisher(auth), id, confirm);
        return ResponseEntity.noContent().build();
    }

    private KnowledgeActor publisher(Authentication auth) {
        KnowledgeActor actor = accessService.actor(auth);
        accessService.requirePublisher(actor);
        return actor;
    }
}
