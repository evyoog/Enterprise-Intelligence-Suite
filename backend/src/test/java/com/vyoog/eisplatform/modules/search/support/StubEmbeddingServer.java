package com.vyoog.eisplatform.modules.search.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * C70: an in-process copy of ai-service's {@code stub} embedding provider
 * (same algorithm as ai-service/app/embeddings.py StubEmbedder: hashed words
 * and 5-letter word beginnings, 384 dimensions, L2-normalised), served over
 * HTTP like the real service, so the backend's HTTP client, timeouts and
 * fallback are exercised. It matches shared words only; it is not a real model.
 */
public final class StubEmbeddingServer implements AutoCloseable {

    public static final int DIMENSION = 384;
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");

    private final HttpServer server;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile boolean available = true;
    private volatile long delayMs;

    public StubEmbeddingServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/embed/info", this::info);
        server.createContext("/embed", this::embed);
        server.start();
    }

    public String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    /** false: answer 503, as ai-service does without a model. */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /** Wait this long before answering (to test the query timeout). */
    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    private void info(HttpExchange exchange) throws IOException {
        respond(exchange, 200, Map.of("enabled", available, "provider", "stub", "model", "stub-hashing-v1",
            "dimension", DIMENSION));
    }

    private void embed(HttpExchange exchange) throws IOException {
        if (!"/embed".equals(exchange.getRequestURI().getPath())) {
            respond(exchange, 404, Map.of());
            return;
        }
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (!available) {
            respond(exchange, 503, Map.of("detail", "not available"));
            return;
        }
        JsonNode body = mapper.readTree(exchange.getRequestBody());
        List<float[]> vectors = new ArrayList<>();
        for (JsonNode text : body.path("texts")) {
            vectors.add(embed(text.asText()));
        }
        respond(exchange, 200, Map.of("provider", "stub", "model", "stub-hashing-v1", "dimension", DIMENSION,
            "vectors", vectors));
    }

    public static float[] embed(String text) {
        double[] vector = new double[DIMENSION];
        String folded = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        Matcher matcher = WORD.matcher(folded);
        while (matcher.find()) {
            String word = matcher.group();
            add(vector, word, 1.0);
            add(vector, word.substring(0, Math.min(5, word.length())), 0.6);
        }
        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        float[] out = new float[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            out[i] = norm == 0 ? 0 : (float) (vector[i] / norm);
        }
        return out;
    }

    private static void add(double[] vector, String feature, double weight) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(feature.getBytes(StandardCharsets.UTF_8));
            long index = ((digest[0] & 0xFFL) << 24 | (digest[1] & 0xFFL) << 16 | (digest[2] & 0xFFL) << 8
                | (digest[3] & 0xFFL)) % DIMENSION;
            double sign = (digest[4] & 1) == 1 ? 1.0 : -1.0;
            vector[(int) index] += sign * weight;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void respond(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
