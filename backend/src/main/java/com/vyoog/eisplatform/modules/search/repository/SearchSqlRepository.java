package com.vyoog.eisplatform.modules.search.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * PostgreSQL-only search queries (C70): full-text (tsvector, English and
 * Spanish), trigram (pg_trgm) and vector (pgvector). Every query here filters
 * on visibility/owner and type before it ranks: a row the caller may not see
 * is never scored, so it can never be returned (REQ-PRT-002 BR-SRCH-001).
 * Only used when {@link #detectCapabilities()} says the index is installed.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchSqlRepository {

    private final JdbcTemplate jdbc;

    public SearchCapabilities detectCapabilities() {
        try {
            String product = jdbc.execute((Connection c) -> c.getMetaData().getDatabaseProductName());
            if (product == null || !product.toLowerCase(Locale.ROOT).contains("postgres")) {
                return SearchCapabilities.NONE;
            }
            Integer tsv = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.columns WHERE table_name = 'search_document' "
                    + "AND column_name = 'tsv' AND table_schema = ANY (current_schemas(false))", Integer.class);
            Integer trgm = jdbc.queryForObject("SELECT count(*) FROM pg_extension WHERE extname = 'pg_trgm'", Integer.class);
            Integer chunk = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'search_chunk' "
                    + "AND table_schema = ANY (current_schemas(false))", Integer.class);
            Integer vector = jdbc.queryForObject("SELECT count(*) FROM pg_extension WHERE extname = 'vector'", Integer.class);
            boolean keyword = tsv != null && tsv > 0 && trgm != null && trgm > 0;
            return new SearchCapabilities(true, keyword, keyword && chunk != null && chunk > 0 && vector != null && vector > 0);
        } catch (RuntimeException e) {
            log.warn("Could not detect search index capabilities: {}", e.getMessage());
            return SearchCapabilities.NONE;
        }
    }

    private static final String KEYWORD_SQL = """
        WITH q AS (
            SELECT to_tsquery('english', ?) AS qe, to_tsquery('spanish', ?) AS qs,
                   to_tsquery('simple', ?) AS qp, to_tsquery('simple', ?) AS qph,
                   to_tsquery('english', ?) AS qce, to_tsquery('spanish', ?) AS qcs,
                   ?::text AS fq, ?::text[] AS terms, ?::text AS ref, ?::boolean AS loose, ?::float8 AS typo
        ), visible AS (
            SELECT d.* FROM search_document d
            WHERE (d.visibility = 'PUBLIC' OR d.owner_customer_id = ?)
              AND (?::text IS NULL OR d.source_type = ?::text)
        ), candidates AS (
            SELECT v.*, q.* FROM visible v, q
            WHERE (q.ref IS NOT NULL AND v.reference = q.ref)
               OR v.tsv @@ q.qe OR v.tsv @@ q.qs OR v.tsv @@ q.qph
               OR (q.loose AND (v.tsv @@ q.qp OR v.tsv @@ q.qce OR v.tsv @@ q.qcs
                                OR (length(q.fq) >= 3 AND q.fq <% v.title_folded)))
        ), scored AS (
            SELECT c.id, c.source_type, c.source_id, c.reference, c.title, c.body,
                CASE
                    WHEN c.ref IS NOT NULL AND c.reference = c.ref THEN 1
                    WHEN numnode(c.qph) > 1 AND c.tsv @@ c.qph THEN 2
                    WHEN c.tsv @@ c.qe OR c.tsv @@ c.qs THEN 3
                    WHEN c.loose AND c.tsv @@ c.qp THEN 4
                    WHEN c.loose AND (c.tsv @@ c.qce OR c.tsv @@ c.qcs
                         OR (cardinality(c.terms) > 0 AND NOT EXISTS (
                             SELECT 1 FROM unnest(c.terms) t WHERE word_similarity(t, c.title_folded) < c.typo))) THEN 5
                END AS tier,
                greatest(ts_rank_cd(c.tsv, c.qe, 32), ts_rank_cd(c.tsv, c.qs, 32), ts_rank_cd(c.tsv, c.qph, 32),
                         0.5 * ts_rank_cd(c.tsv, c.qp, 32), 0.5 * ts_rank_cd(c.tsv, c.qce, 32),
                         0.5 * ts_rank_cd(c.tsv, c.qcs, 32))
                    + 0.3 * word_similarity(c.fq, c.title_folded) AS score
            FROM candidates c
        )
        SELECT * FROM scored WHERE tier IS NOT NULL
        ORDER BY tier, score DESC, title
        LIMIT ?
        """;

    private static final RowMapper<SearchRow> KEYWORD_ROW = (rs, n) -> new SearchRow(
        rs.getLong("id"), rs.getString("source_type"), rs.getLong("source_id"), rs.getString("reference"),
        rs.getString("title"), rs.getString("body"), rs.getInt("tier"), rs.getDouble("score"), null);

    public List<SearchRow> keywordSearch(KeywordSqlQuery query) {
        // <% (used by the candidate filter so the trigram index applies) reads
        // this setting; it is local to the surrounding transaction.
        jdbc.queryForObject("SELECT set_config('pg_trgm.word_similarity_threshold', ?, true)", String.class,
            String.valueOf(query.typoThreshold()));
        return jdbc.query(KEYWORD_SQL, KEYWORD_ROW,
            query.englishQuery(), query.spanishQuery(), query.prefixQuery(), query.phraseQuery(),
            query.correctedEnglishQuery(), query.correctedSpanishQuery(),
            query.folded(), query.terms().toArray(new String[0]), query.exactReference(), query.loose(),
            query.typoThreshold(),
            query.customerId(), query.sourceType(), query.sourceType(), query.limit());
    }

    /** Type-ahead: visible records whose title contains the typed text or a
     * close spelling of it; titles starting with it first. */
    public List<SearchRow> suggest(String folded, Long customerId, String sourceType, double threshold, int limit) {
        jdbc.queryForObject("SELECT set_config('pg_trgm.word_similarity_threshold', ?, true)", String.class,
            String.valueOf(threshold));
        String like = folded.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return jdbc.query("""
            SELECT d.id, d.source_type, d.source_id, d.reference, d.title, NULL AS body,
                   CASE WHEN d.title_folded LIKE ? || '%' THEN 1
                        WHEN d.title_folded LIKE '%' || ? || '%' THEN 2 ELSE 3 END AS tier,
                   word_similarity(?, d.title_folded) AS score
            FROM search_document d
            WHERE (d.visibility = 'PUBLIC' OR d.owner_customer_id = ?)
              AND (?::text IS NULL OR d.source_type = ?::text)
              AND (d.title_folded LIKE '%' || ? || '%' OR ? <% d.title_folded)
            ORDER BY tier, score DESC, d.title
            LIMIT ?
            """, KEYWORD_ROW, like, like, folded, customerId, sourceType, sourceType, like, folded, limit);
    }

    /** Which of these words appear in public content. */
    public Set<String> knownTerms(List<String> terms) {
        if (terms.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(jdbc.queryForList("SELECT term FROM search_term WHERE term = ANY (?)", String.class,
            (Object) terms.toArray(new String[0])));
    }

    /** The closest word in public content, if it is similar enough. */
    public Optional<String> closestTerm(String term, double threshold) {
        jdbc.queryForObject("SELECT set_config('pg_trgm.similarity_threshold', ?, true)", String.class,
            String.valueOf(threshold));
        List<String> found = jdbc.queryForList("""
            SELECT term FROM search_term WHERE term % ?
            ORDER BY similarity(term, ?) DESC, doc_count DESC, term LIMIT 1
            """, String.class, term, term);
        return found.stream().findFirst();
    }

    /** Rebuilds the word list for "Did you mean" from PUBLIC documents only. */
    public void refreshTerms() {
        jdbc.update("DELETE FROM search_term");
        jdbc.update("""
            INSERT INTO search_term (term, doc_count)
            SELECT word, ndoc FROM ts_stat(
                'SELECT to_tsvector(''simple'', title_folded || '' '' || coalesce(keywords_folded, '''') '
                || '|| '' '' || coalesce(body_folded, '''')) FROM search_document WHERE visibility = ''PUBLIC''')
            WHERE length(word) BETWEEN 3 AND 100 AND word !~ '^[0-9]+$'
            """);
    }

    // ---- semantic (pgvector) ------------------------------------------------

    public List<ChunkRef> chunksOf(long documentId) {
        return jdbc.query("SELECT id, document_id, chunk_index, content, content_hash FROM search_chunk "
                + "WHERE document_id = ? ORDER BY chunk_index",
            (rs, n) -> new ChunkRef(rs.getLong(1), rs.getLong(2), rs.getInt(3), rs.getString(4), rs.getString(5)),
            documentId);
    }

    /** Inserts or replaces a passage; a changed passage loses its embedding
     * until it is embedded again. */
    public void upsertChunk(long documentId, int index, String content, String hash) {
        jdbc.update("""
            INSERT INTO search_chunk (document_id, chunk_index, content, content_hash)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (document_id, chunk_index) DO UPDATE
                SET content = EXCLUDED.content, content_hash = EXCLUDED.content_hash,
                    embedding = NULL, embedded_at = NULL
                WHERE search_chunk.content_hash <> EXCLUDED.content_hash
            """, documentId, index, content, hash);
    }

    public void deleteChunksFrom(long documentId, int fromIndex) {
        jdbc.update("DELETE FROM search_chunk WHERE document_id = ? AND chunk_index >= ?", documentId, fromIndex);
    }

    public void deleteChunksOfNonPublicDocuments() {
        jdbc.update("DELETE FROM search_chunk c USING search_document d WHERE d.id = c.document_id "
            + "AND d.visibility <> 'PUBLIC'");
    }

    public List<ChunkRef> pendingChunks(int limit) {
        return jdbc.query("SELECT id, document_id, chunk_index, content, content_hash FROM search_chunk "
                + "WHERE embedding IS NULL ORDER BY id LIMIT ?",
            (rs, n) -> new ChunkRef(rs.getLong(1), rs.getLong(2), rs.getInt(3), rs.getString(4), rs.getString(5)),
            limit);
    }

    public List<ChunkRef> pendingChunksOf(long documentId) {
        return jdbc.query("SELECT id, document_id, chunk_index, content, content_hash FROM search_chunk "
                + "WHERE document_id = ? AND embedding IS NULL ORDER BY chunk_index",
            (rs, n) -> new ChunkRef(rs.getLong(1), rs.getLong(2), rs.getInt(3), rs.getString(4), rs.getString(5)),
            documentId);
    }

    /** Stores an embedding only if the passage did not change meanwhile. */
    public void setEmbedding(long chunkId, String contentHash, float[] vector) {
        jdbc.update("UPDATE search_chunk SET embedding = ?::vector, embedded_at = now() "
            + "WHERE id = ? AND content_hash = ?", toVectorLiteral(vector), chunkId, contentHash);
    }

    /** Clears every embedding, so all passages are embedded again (after a model change). */
    public int clearEmbeddings() {
        return jdbc.update("UPDATE search_chunk SET embedding = NULL, embedded_at = NULL WHERE embedding IS NOT NULL");
    }

    public long[] chunkCounts() {
        return jdbc.queryForObject("SELECT count(*), count(embedding), count(*) - count(embedding) FROM search_chunk",
            (rs, n) -> new long[] {rs.getLong(1), rs.getLong(2), rs.getLong(3)});
    }

    /** Nearest passages of PUBLIC documents. Private records never have
     * passages (BR-SEM-001), and the visibility filter is applied again here. */
    public List<SearchRow> semanticSearch(float[] queryVector, String sourceType, int candidates) {
        String vector = toVectorLiteral(queryVector);
        return jdbc.query("""
            SELECT d.id, d.source_type, d.source_id, d.reference, d.title, d.body, c.content,
                   1 - (c.embedding <=> ?::vector) AS similarity
            FROM search_chunk c JOIN search_document d ON d.id = c.document_id
            WHERE d.visibility = 'PUBLIC' AND c.embedding IS NOT NULL
              AND (?::text IS NULL OR d.source_type = ?::text)
            ORDER BY c.embedding <=> ?::vector
            LIMIT ?
            """, (rs, n) -> new SearchRow(rs.getLong("id"), rs.getString("source_type"), rs.getLong("source_id"),
                rs.getString("reference"), rs.getString("title"), rs.getString("body"), 6,
                rs.getDouble("similarity"), rs.getString("content")),
            vector, sourceType, sourceType, vector, candidates);
    }

    static String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 10).append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }
}
