package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeMediaDtos;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMedia;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaKind;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeMediaStatus;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;
import com.vyoog.eisplatform.modules.knowledgebase.repository.KnowledgeMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hosted videos in the private bucket (C72/C73). Playback uses a
 * short-lived presigned GET issued per request (BR-KVID-003); no permanent
 * URL is ever stored or returned.
 */
@Service
@RequiredArgsConstructor
public class S3VideoService implements VideoSourceProvider {

    private final KnowledgeMediaRepository mediaRepository;
    private final KnowledgeMediaService mediaService;
    private final KnowledgeSettings settings;

    @Override
    public VideoSourceType type() {
        return VideoSourceType.AWS_S3;
    }

    @Override
    public void apply(KnowledgeVideo video, KnowledgeVideoDtos.VideoRequest request) {
        if (request.mediaId() == null) {
            throw KnowledgeException.badRequest("UPLOAD_NOT_FOUND", "Upload the video file first.");
        }
        KnowledgeMedia media = mediaRepository.findById(request.mediaId()).orElseThrow(
            () -> KnowledgeException.badRequest("UPLOAD_NOT_FOUND", "The uploaded video was not found."));
        if (media.getKind() != KnowledgeMediaKind.VIDEO_FILE || media.getStatus() != KnowledgeMediaStatus.READY) {
            throw KnowledgeException.badRequest("UPLOAD_NOT_FOUND", "The video upload is not complete yet.");
        }
        video.setMediaId(media.getId());
        video.setVideoId(null);
        video.setVideoUrl(null);
        video.setThumbnailUrl(null);
    }

    @Override
    public KnowledgeVideoDtos.PlayInfo playInfo(KnowledgeVideo video) {
        KnowledgeMedia media = mediaService.find(video.getMediaId());
        KnowledgeMediaDtos.TemporaryUrl url = mediaService.temporaryUrl(media, false, settings.getPlaybackUrlExpiry());
        Map<String, String> subtitles = new LinkedHashMap<>();
        var tracks = KnowledgeBlocks.parse(video.getSubtitles());
        Iterator<String> languages = tracks.fieldNames();
        while (languages.hasNext()) {
            String language = languages.next();
            mediaRepository.findById(tracks.get(language).asLong())
                .filter(m -> m.getStatus() == KnowledgeMediaStatus.READY)
                .ifPresent(m -> subtitles.put(language,
                    mediaService.temporaryUrl(m, false, settings.getPlaybackUrlExpiry()).url()));
        }
        return new KnowledgeVideoDtos.PlayInfo(VideoSourceType.AWS_S3, null, url.url(), url.expiresAt(), subtitles);
    }
}
