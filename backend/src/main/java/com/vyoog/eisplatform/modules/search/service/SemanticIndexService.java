package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.model.SearchDocument;
import com.vyoog.eisplatform.modules.search.model.SearchVisibility;
import com.vyoog.eisplatform.modules.search.repository.ChunkRef;
import com.vyoog.eisplatform.modules.search.repository.SearchSqlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Passages and embeddings for semantic search (REQ-PRT-003, C70). Only PUBLIC
 * documents (catalog and documentation) get passages (BR-SEM-001); a
 * passage whose text did not change keeps its embedding. Embedding happens
 * outside the database transaction; a passage whose embedding failed stays
 * pending and is retried by {@link SearchIndexJobs}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticIndexService {

    private static final int EMBED_BATCH = 32;

    private final SearchSqlRepository sqlRepository;
    private final SearchCapabilityService capabilityService;
    private final EmbeddingClient embeddingClient;
    private final SearchSettings settings;

    /** Writes the document's passages (call inside a transaction). Returns the passage count. */
    public int syncChunks(SearchDocument doc) {
        if (!capabilityService.get().semantic()) {
            return 0;
        }
        if (doc.getVisibility() != SearchVisibility.PUBLIC) {
            sqlRepository.deleteChunksFrom(doc.getId(), 0);
            return 0;
        }
        String body = doc.getKeywords() == null ? doc.getBody()
            : (doc.getBody() == null ? doc.getKeywords() : doc.getKeywords() + ". " + doc.getBody());
        List<String> chunks = Chunker.chunk(doc.getTitle(), body, settings.getChunkSize(), settings.getChunkOverlap());
        for (int i = 0; i < chunks.size(); i++) {
            sqlRepository.upsertChunk(doc.getId(), i, chunks.get(i), sha256(chunks.get(i)));
        }
        sqlRepository.deleteChunksFrom(doc.getId(), chunks.size());
        return chunks.size();
    }

    public void removeChunks(long documentId) {
        if (capabilityService.get().semantic()) {
            sqlRepository.deleteChunksFrom(documentId, 0);
        }
    }

    /** Embeds the pending passages of one document; false if the model was not available. */
    public boolean embedDocument(long documentId) {
        if (!capabilityService.get().semantic() || !settings.embeddingConfigured()) {
            return false;
        }
        return embed(sqlRepository.pendingChunksOf(documentId)) >= 0;
    }

    /** Embeds up to {@code max} pending passages; returns how many were embedded, or -1 if the model failed. */
    public int embedPending(int max) {
        if (!capabilityService.get().semantic() || !settings.embeddingConfigured()) {
            return 0;
        }
        int done = 0;
        while (done < max) {
            List<ChunkRef> batch = sqlRepository.pendingChunks(Math.min(EMBED_BATCH, max - done));
            if (batch.isEmpty()) {
                break;
            }
            int embedded = embed(batch);
            if (embedded < 0) {
                return done == 0 ? -1 : done;
            }
            done += embedded;
        }
        return done;
    }

    private int embed(List<ChunkRef> chunks) {
        int done = 0;
        for (int start = 0; start < chunks.size(); start += EMBED_BATCH) {
            List<ChunkRef> batch = chunks.subList(start, Math.min(chunks.size(), start + EMBED_BATCH));
            try {
                List<float[]> vectors = embeddingClient.embedPassages(batch.stream().map(ChunkRef::content).toList());
                for (int i = 0; i < batch.size(); i++) {
                    sqlRepository.setEmbedding(batch.get(i).id(), batch.get(i).contentHash(), vectors.get(i));
                }
                done += batch.size();
            } catch (EmbeddingClient.EmbeddingUnavailableException e) {
                log.warn("Embedding model unavailable; {} passages stay pending: {}", chunks.size() - done, e.getMessage());
                return -1;
            }
        }
        return done;
    }

    static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
