package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

/** Videos hosted elsewhere (C73): an https link, played in a player or opened (REQ-KNW-004.6). */
@Service
public class ExternalVideoService implements VideoSourceProvider {

    @Override
    public VideoSourceType type() {
        return VideoSourceType.EXTERNAL_URL;
    }

    @Override
    public void apply(KnowledgeVideo video, KnowledgeVideoDtos.VideoRequest request) {
        String url = request.url() == null ? "" : request.url().trim();
        if (!isHttps(url)) {
            throw KnowledgeException.badRequest("INVALID_CONTENT", "The video link must start with https://.");
        }
        video.setVideoUrl(url);
        video.setVideoId(null);
        video.setMediaId(null);
        String thumb = request.thumbnailUrl();
        video.setThumbnailUrl(thumb != null && isHttps(thumb.trim()) ? thumb.trim() : null);
    }

    @Override
    public KnowledgeVideoDtos.PlayInfo playInfo(KnowledgeVideo video) {
        return new KnowledgeVideoDtos.PlayInfo(VideoSourceType.EXTERNAL_URL, null, video.getVideoUrl(), null, Map.of());
    }

    static boolean isHttps(String url) {
        try {
            URI uri = URI.create(url);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && url.length() <= 1000;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
