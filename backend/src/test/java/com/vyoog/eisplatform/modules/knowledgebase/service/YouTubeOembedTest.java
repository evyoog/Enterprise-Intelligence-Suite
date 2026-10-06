package com.vyoog.eisplatform.modules.knowledgebase.service;

import com.sun.net.httpserver.HttpServer;
import com.vyoog.eisplatform.common.exception.KnowledgeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C73 / youtube.md: without an API key, details come from oEmbed; failures are friendly. */
class YouTubeOembedTest {

    private HttpServer server;
    private String base;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/oembed", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            boolean known = query.contains("dQw4w9WgXcQ");
            byte[] body = (known ? "{\"title\":\"Inventory basics\",\"author_name\":\"eVyoog\","
                + "\"thumbnail_url\":\"https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg\"}" : "Not Found")
                .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(known ? 200 : 404, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/data", exchange -> {
            byte[] body = ("{\"items\":[{\"snippet\":{\"title\":\"Inventory basics\",\"description\":\"Long text\","
                + "\"channelTitle\":\"eVyoog\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/x/hq.jpg\"}}},"
                + "\"contentDetails\":{\"duration\":\"PT4M5S\"}}]}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private YouTubeVideoService service() {
        KnowledgeSettings settings = new KnowledgeSettings();
        ReflectionTestUtils.setField(settings, "youtubeApiKey", "");
        ReflectionTestUtils.setField(settings, "youtubeTimeoutMs", 2000);
        return new YouTubeVideoService(settings, base + "/oembed", base + "/data");
    }

    @Test
    void oembedFillsTitleChannelAndThumbnail() {
        var details = service().fetchDetails("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        assertThat(details.source()).isEqualTo("OEMBED");
        assertThat(details.title()).isEqualTo("Inventory basics");
        assertThat(details.channel()).isEqualTo("eVyoog");
        assertThat(details.durationSeconds()).isNull();
    }

    @Test
    void withAnApiKeyTheDataApiFillsDurationAndDescription() {
        KnowledgeSettings settings = new KnowledgeSettings();
        ReflectionTestUtils.setField(settings, "youtubeApiKey", "test-key");
        ReflectionTestUtils.setField(settings, "youtubeTimeoutMs", 2000);
        var details = new YouTubeVideoService(settings, base + "/oembed", base + "/data").fetchDetails("dQw4w9WgXcQ");
        assertThat(details.source()).isEqualTo("DATA_API");
        assertThat(details.durationSeconds()).isEqualTo(245);
        assertThat(details.description()).isEqualTo("Long text");
    }

    @Test
    void unknownVideoAndUnreachableYouTubeGiveFriendlyErrors() {
        assertThatThrownBy(() -> service().fetchDetails("https://youtu.be/AAAAAAAAAAA"))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("YOUTUBE_NOT_FOUND");
        server.stop(0);
        assertThatThrownBy(() -> service().fetchDetails("https://youtu.be/dQw4w9WgXcQ"))
            .isInstanceOf(KnowledgeException.class).extracting("code").isEqualTo("YOUTUBE_UNAVAILABLE");
    }
}
