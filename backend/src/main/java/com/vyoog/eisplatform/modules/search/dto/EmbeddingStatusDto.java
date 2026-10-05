package com.vyoog.eisplatform.modules.search.dto;

/**
 * @param configured EMBEDDING_SERVICE_URL is set
 * @param available  ai-service answered and has a model loaded with the expected dimension
 * @param provider   "sentence-transformers" (a real model) or "stub" (test model)
 * @param model      the model name ai-service reports
 * @param dimension  the model's vector size (the index expects 384)
 * @param message    why it is not available, when it is not
 */
public record EmbeddingStatusDto(
    boolean configured,
    boolean available,
    String provider,
    String model,
    Integer dimension,
    String message
) {
}
