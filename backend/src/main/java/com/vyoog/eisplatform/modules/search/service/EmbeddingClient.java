package com.vyoog.eisplatform.modules.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.search.dto.EmbeddingStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Calls the embedding model in ai-service (REQ-PRT-003, C70): POST
 * {@code /embed} turns texts into vectors, GET {@code /embed/info} reports
 * the model. Queries use a short timeout (the search falls back to keyword
 * results when the model does not answer in time); indexing uses a longer one.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingClient {

    public enum Kind { QUERY, PASSAGE }

    /** The model did not answer, answered with an error, or is not configured. */
    public static class EmbeddingUnavailableException extends RuntimeException {
        public EmbeddingUnavailableException(String message) {
            super(message);
        }
    }

    private static final Duration INDEX_TIMEOUT = Duration.ofSeconds(60);

    private final SearchSettings settings;
    private final ObjectMapper objectMapper;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    public float[] embedQuery(String text) {
        return embed(List.of(text), Kind.QUERY, Duration.ofMillis(settings.getEmbeddingTimeoutMs())).get(0);
    }

    public List<float[]> embedPassages(List<String> texts) {
        return embed(texts, Kind.PASSAGE, INDEX_TIMEOUT);
    }

    public EmbeddingStatusDto status() {
        if (!settings.embeddingConfigured()) {
            return new EmbeddingStatusDto(false, false, null, null, null, "EMBEDDING_SERVICE_URL is not set.");
        }
        try {
            HttpResponse<String> response = http.send(HttpRequest.newBuilder(uri("/embed/info"))
                .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());
            boolean enabled = body.path("enabled").asBoolean(false);
            Integer dimension = body.hasNonNull("dimension") ? body.get("dimension").asInt() : null;
            String message = body.path("message").asText(null);
            boolean dimensionOk = dimension != null && dimension == SearchSettings.EMBEDDING_DIMENSION;
            if (enabled && !dimensionOk) {
                message = "The model's dimension (" + dimension + ") is not " + SearchSettings.EMBEDDING_DIMENSION + ".";
            }
            return new EmbeddingStatusDto(true, enabled && dimensionOk, body.path("provider").asText(null),
                body.path("model").asText(null), dimension, message);
        } catch (IOException | RuntimeException e) {
            return new EmbeddingStatusDto(true, false, null, null, null, "ai-service did not answer: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new EmbeddingStatusDto(true, false, null, null, null, "Interrupted.");
        }
    }

    private List<float[]> embed(List<String> texts, Kind kind, Duration timeout) {
        if (!settings.embeddingConfigured()) {
            throw new EmbeddingUnavailableException("Not configured");
        }
        try {
            String json = objectMapper.writeValueAsString(Map.of("texts", texts, "kind", kind.name().toLowerCase()));
            HttpResponse<String> response = http.send(HttpRequest.newBuilder(uri("/embed"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new EmbeddingUnavailableException("ai-service answered " + response.statusCode());
            }
            JsonNode vectors = objectMapper.readTree(response.body()).path("vectors");
            if (!vectors.isArray() || vectors.size() != texts.size()) {
                throw new EmbeddingUnavailableException("ai-service returned the wrong number of vectors");
            }
            List<float[]> out = new ArrayList<>(texts.size());
            for (JsonNode vector : vectors) {
                if (vector.size() != SearchSettings.EMBEDDING_DIMENSION) {
                    throw new EmbeddingUnavailableException("Vector dimension " + vector.size() + " is not "
                        + SearchSettings.EMBEDDING_DIMENSION);
                }
                float[] values = new float[vector.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = (float) vector.get(i).asDouble();
                }
                out.add(values);
            }
            return out;
        } catch (IOException e) {
            throw new EmbeddingUnavailableException(e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmbeddingUnavailableException("Interrupted");
        }
    }

    private URI uri(String path) {
        String base = settings.getEmbeddingServiceUrl().replaceAll("/+$", "");
        return URI.create(base + path);
    }
}
