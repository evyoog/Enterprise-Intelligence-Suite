package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vyoog.eisplatform.common.exception.KnowledgeException;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeVideoDtos;
import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeVideo;
import com.vyoog.eisplatform.modules.knowledgebase.model.VideoSourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTube videos (C73, docs/09-integrations/youtube.md). Details come from
 * the YouTube Data API when EIS_YOUTUBE_API_KEY is set, otherwise from
 * oEmbed (title, thumbnail, channel; the admin types duration and
 * description). The key is used on the server only and never returned.
 */
@Service
@Slf4j
public class YouTubeVideoService implements VideoSourceProvider {

    private static final Pattern ID = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Pattern URL_ID = Pattern.compile(
        "^https?://(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|embed/|shorts/|live/)|youtu\\.be/|youtube-nocookie\\.com/embed/)([A-Za-z0-9_-]{11})(?:[?&#/].*)?$");
    private static final Pattern ISO_DURATION = Pattern.compile("^PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?$");

    private final KnowledgeSettings settings;
    private final String oembedUrl;
    private final String dataApiUrl;
    private final HttpClient http;

    public YouTubeVideoService(KnowledgeSettings settings,
                               @Value("${eis.knowledge.youtube.oembed-url:https://www.youtube.com/oembed}") String oembedUrl,
                               @Value("${eis.knowledge.youtube.data-api-url:https://www.googleapis.com/youtube/v3/videos}") String dataApiUrl) {
        this.settings = settings;
        this.oembedUrl = oembedUrl;
        this.dataApiUrl = dataApiUrl;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(settings.getYoutubeTimeoutMs())).build();
    }

    @Override
    public VideoSourceType type() {
        return VideoSourceType.YOUTUBE;
    }

    /** The 11-character id from a watch, youtu.be, embed, shorts or live URL (BR-KVID-001). */
    public static String parseVideoId(String url) {
        if (url == null) {
            return null;
        }
        String trimmed = url.trim();
        if (ID.matcher(trimmed).matches()) {
            return trimmed;
        }
        Matcher m = URL_ID.matcher(trimmed);
        return m.matches() ? m.group(1) : null;
    }

    @Override
    public void apply(KnowledgeVideo video, KnowledgeVideoDtos.VideoRequest request) {
        String id = parseVideoId(request.youtubeUrl());
        if (id == null) {
            throw KnowledgeException.badRequest("YOUTUBE_NOT_FOUND", "This is not a valid YouTube video link.");
        }
        video.setVideoId(id);
        video.setVideoUrl("https://www.youtube.com/watch?v=" + id);
        video.setMediaId(null);
        String thumb = request.thumbnailUrl();
        video.setThumbnailUrl(thumb != null && thumb.startsWith("https://") ? thumb : null);
    }

    @Override
    public KnowledgeVideoDtos.PlayInfo playInfo(KnowledgeVideo video) {
        return new KnowledgeVideoDtos.PlayInfo(VideoSourceType.YOUTUBE, video.getVideoId(), null, null, Map.of());
    }

    /** Fetch video details (REQ-KNW-004.3): Data API with a key, oEmbed without. */
    public KnowledgeVideoDtos.YouTubeDetails fetchDetails(String url) {
        String id = parseVideoId(url);
        if (id == null) {
            throw KnowledgeException.badRequest("YOUTUBE_NOT_FOUND", "This is not a valid YouTube video link.");
        }
        String key = settings.getYoutubeApiKey();
        if (key != null && !key.isBlank()) {
            try {
                return fromDataApi(id, key);
            } catch (KnowledgeException e) {
                throw e;
            } catch (RuntimeException e) {
                log.warn("YouTube Data API failed, falling back to oEmbed: {}", e.getMessage());
            }
        }
        return fromOembed(id);
    }

    private KnowledgeVideoDtos.YouTubeDetails fromDataApi(String id, String key) {
        String uri = dataApiUrl + "?part=snippet,contentDetails&id=" + id + "&key=" + URLEncoder.encode(key, StandardCharsets.UTF_8);
        JsonNode body = get(uri);
        JsonNode items = body.path("items");
        if (!items.isArray() || items.isEmpty()) {
            throw KnowledgeException.badRequest("YOUTUBE_NOT_FOUND", "The YouTube video was not found or is private.");
        }
        JsonNode item = items.get(0);
        JsonNode snippet = item.path("snippet");
        String thumbnail = snippet.path("thumbnails").path("high").path("url").asText(null);
        return new KnowledgeVideoDtos.YouTubeDetails(id, snippet.path("title").asText(null),
            snippet.path("description").asText(null), parseIsoDuration(item.path("contentDetails").path("duration").asText(null)),
            snippet.path("channelTitle").asText(null),
            thumbnail != null ? thumbnail : "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg", "DATA_API");
    }

    private KnowledgeVideoDtos.YouTubeDetails fromOembed(String id) {
        String watch = "https://www.youtube.com/watch?v=" + id;
        JsonNode body = get(oembedUrl + "?format=json&url=" + URLEncoder.encode(watch, StandardCharsets.UTF_8));
        String thumbnail = body.path("thumbnail_url").asText(null);
        return new KnowledgeVideoDtos.YouTubeDetails(id, body.path("title").asText(null), null, null,
            body.path("author_name").asText(null),
            thumbnail != null && thumbnail.startsWith("https://") ? thumbnail : "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg",
            "OEMBED");
    }

    private JsonNode get(String uri) {
        try {
            HttpResponse<String> response = http.send(HttpRequest.newBuilder(URI.create(uri))
                .timeout(Duration.ofMillis(settings.getYoutubeTimeoutMs())).GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404 || response.statusCode() == 401 || response.statusCode() == 403
                || response.statusCode() == 400) {
                throw KnowledgeException.badRequest("YOUTUBE_NOT_FOUND", "The YouTube video was not found, is private or cannot be embedded.");
            }
            if (response.statusCode() >= 300) {
                throw unavailable("status " + response.statusCode());
            }
            return KnowledgeBlocks.JSON.readTree(response.body());
        } catch (KnowledgeException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable(e.getMessage());
        } catch (Exception e) {
            throw unavailable(e.getClass().getSimpleName());
        }
    }

    private KnowledgeException unavailable(String reason) {
        // The request URL may contain the API key, so it is never logged.
        log.warn("YouTube details unavailable: {}", reason);
        return new KnowledgeException(HttpStatus.SERVICE_UNAVAILABLE, "YOUTUBE_UNAVAILABLE",
            "YouTube could not be reached. Enter the details by hand or try again later.");
    }

    static Integer parseIsoDuration(String iso) {
        if (iso == null) {
            return null;
        }
        Matcher m = ISO_DURATION.matcher(iso);
        if (!m.matches()) {
            return null;
        }
        int h = m.group(1) == null ? 0 : Integer.parseInt(m.group(1));
        int min = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        int s = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
        return h * 3600 + min * 60 + s;
    }
}
