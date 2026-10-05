package com.vyoog.eisplatform.modules.search.repository;

public record ChunkRef(long id, long documentId, int chunkIndex, String content, String contentHash) {
}
