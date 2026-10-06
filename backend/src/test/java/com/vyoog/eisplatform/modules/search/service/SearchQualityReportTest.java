package com.vyoog.eisplatform.modules.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.knowledgebase.dto.KnowledgeArticleRequest;
import com.vyoog.eisplatform.modules.knowledgebase.service.KnowledgeArticleService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.search.dto.SearchResultItemDto;
import com.vyoog.eisplatform.modules.search.model.SearchIndexRun;
import com.vyoog.eisplatform.modules.search.support.PostgresSearchTestSupport;
import com.vyoog.eisplatform.modules.search.support.StubEmbeddingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * C70 quality and speed report (not part of the normal build). Runs with
 * EIS_PG_TEST_URL and EIS_SEARCH_REPORT=1 set, on an empty database schema.
 * Loads the test set (search-quality/corpus.json, queries.json), compares the
 * search before C70 with keyword and hybrid search, tunes the thresholds,
 * measures speed on a larger synthetic index, and writes
 * target/search-quality-report.md.
 */
@SpringBootTest
@ActiveProfiles({"test", "pgtest"})
@EnabledIfEnvironmentVariable(named = "EIS_SEARCH_REPORT", matches = "1")
class SearchQualityReportTest {

    private static StubEmbeddingServer embeddingServer;

    @BeforeAll
    static void prepare() throws Exception {
        PostgresSearchTestSupport.prepareSchema();
    }

    @AfterAll
    static void stop() {
        if (embeddingServer != null) {
            embeddingServer.close();
        }
    }

    @DynamicPropertySource
    static void embeddingProperties(DynamicPropertyRegistry registry) throws Exception {
        if (embeddingServer == null) {
            embeddingServer = new StubEmbeddingServer();
        }
        registry.add("app.search.semantic.service-url", embeddingServer::url);
        // A search's model call goes over local HTTP to the stub; no real model latency.
        registry.add("app.search.semantic.timeout-ms", () -> "2000");
    }

    @Autowired private GlobalSearchService searchService;
    @Autowired private BasicKeywordSearch basicSearch;
    @Autowired private SearchIndexService indexService;
    @Autowired private SemanticIndexService semanticIndexService;
    @Autowired private SearchSettings settings;
    @Autowired private ProductRepository productRepository;
    @Autowired private KnowledgeArticleService articleService;
    @Autowired private JdbcTemplate jdbc;

    private final Map<String, String> keyByRecord = new LinkedHashMap<>();
    private final Map<String, String> recordByKey = new LinkedHashMap<>();

    record Query(String q, String lang, String category, List<String> expect) {
    }

    record Score(double hitAt1, double hitAt5, double mrr, double noiseAt5, Map<String, double[]> byCategory,
                 List<String> misses) {
    }

    @Test
    void writeReport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode corpus = mapper.readTree(new ClassPathResource("search-quality/corpus.json").getInputStream());
        for (JsonNode p : corpus.path("products")) {
            Product product = new Product();
            product.setName(p.path("name").asText());
            product.setDescription(p.path("description").asText());
            product.setCategory(p.path("category").asText());
            product.setFeatureTags(p.path("tags").asText());
            product.setPrice(BigDecimal.TEN);
            product.setStatus(ProductStatus.ACTIVE);
            remember(p.path("key").asText(), "PRODUCT", productRepository.save(product).getId());
        }
        for (JsonNode a : corpus.path("articles")) {
            var article = articleService.createArticle(new KnowledgeArticleRequest(a.path("title").asText(), a.path("body").asText()));
            articleService.setPublished(article.id(), true);
            remember(a.path("key").asText(), "KNOWLEDGE", article.id());
        }
        List<Query> queries = new ArrayList<>();
        for (JsonNode q : mapper.readTree(new ClassPathResource("search-quality/queries.json").getInputStream()).path("queries")) {
            String text = q.path("q").asText();
            if (text.startsWith("#ID:")) {
                text = "#" + recordByKey.get(text.substring(4)).split(":")[1];
            }
            List<String> expect = new ArrayList<>();
            q.path("expect").forEach(e -> expect.add(e.asText()));
            queries.add(new Query(text, q.path("lang").asText(), q.path("category").asText(), expect));
        }
        rebuild();

        StringBuilder report = new StringBuilder();
        report.append("# Search quality and speed report (C70)\n\n");
        report.append("Generated by `SearchQualityReportTest` on PostgreSQL ")
            .append(jdbc.queryForObject("SHOW server_version", String.class)).append(" with pg_trgm and pgvector ")
            .append(jdbc.queryForObject("SELECT extversion FROM pg_extension WHERE extname = 'vector'", String.class))
            .append(".\n\n");
        report.append("Test set: ").append(corpus.path("products").size()).append(" products and ")
            .append(corpus.path("articles").size()).append(" knowledge articles (English and Spanish), ")
            .append(queries.size()).append(" queries (")
            .append(queries.stream().filter(q -> q.lang().equals("en")).count()).append(" English, ")
            .append(queries.stream().filter(q -> q.lang().equals("es")).count()).append(" Spanish).\n\n");
        report.append("**Embedding model: the test model (stub, word hashing).** No real model could be downloaded in the "
            + "build environment. Semantic and hybrid figures therefore show the pipeline working, not the quality "
            + "a real multilingual model gives; the similarity threshold and chunk size must be tuned again with "
            + "the real model.\n\n");

        Function<Query, List<String>> before = q -> {
            BasicKeywordSearch.Grouped g = basicSearch.search(q.q(), null, null);
            return keys(Stream.of(g.products(), g.knowledgeArticles(), g.tickets()).flatMap(List::stream).toList());
        };
        Function<Query, List<String>> keyword = q -> keys(searchService.search(q.q(), null, null,
            GlobalSearchService.Mode.KEYWORD, null, false).results());
        Function<Query, List<String>> hybrid = q -> keys(searchService.search(q.q(), null, null,
            GlobalSearchService.Mode.HYBRID, null, false).results());

        // ---- tuning ----------------------------------------------------------
        report.append("## Tuning\n\n");
        report.append("Each value was tried on the whole test set. The value kept has the best hit@5, then the best MRR, "
            + "then the fewest unexpected results in the top 5. On an exact tie with the configured value, the "
            + "configured value is kept (the test set cannot tell them apart).\n\n");
        report.append("### Typo threshold (keyword search)\n\n| typo-threshold | hit@1 | hit@5 | MRR | unexpected in top 5 |\n|---|---|---|---|---|\n");
        tuningDefault = settings.getTypoThreshold();
        double bestTypo = tune(report, new double[] {0.3, 0.4, 0.5, 0.6, 0.7}, v -> set("typoThreshold", v), queries, keyword);
        set("typoThreshold", bestTypo);
        report.append("\nKept: **").append(bestTypo).append("**.\n\n");

        report.append("### \"Did you mean\" threshold (keyword search)\n\n| correction-threshold | hit@1 | hit@5 | MRR | unexpected in top 5 |\n|---|---|---|---|---|\n");
        tuningDefault = settings.getCorrectionThreshold();
        double bestCorrection = tune(report, new double[] {0.3, 0.35, 0.45, 0.55}, v -> set("correctionThreshold", v), queries, keyword);
        set("correctionThreshold", bestCorrection);
        report.append("\nKept: **").append(bestCorrection).append("**.\n\n");

        report.append("### Similarity threshold (hybrid search, stub model)\n\n| similarity-threshold | hit@1 | hit@5 | MRR | unexpected in top 5 |\n|---|---|---|---|---|\n");
        tuningDefault = settings.getSimilarityThreshold();
        double bestSimilarity = tune(report, new double[] {0.2, 0.25, 0.35, 0.45, 0.55}, v -> set("similarityThreshold", v), queries, hybrid);
        set("similarityThreshold", bestSimilarity);
        report.append("\nKept: **").append(bestSimilarity).append("**.\n\n");

        report.append("### Chunk size (hybrid search, stub model)\n\n| chunk-size | hit@1 | hit@5 | MRR | unexpected in top 5 |\n|---|---|---|---|---|\n");
        tuningDefault = settings.getChunkSize();
        double bestChunk = tune(report, new double[] {300, 600, 900}, v -> {
            set("chunkSize", (int) v);
            rebuild();
        }, queries, hybrid);
        set("chunkSize", (int) bestChunk);
        rebuild();
        report.append("\nKept: **").append((int) bestChunk).append("** characters (overlap ")
            .append(settings.getChunkOverlap()).append(").\n\n");

        // ---- before / after ---------------------------------------------------
        Score sBefore = score(queries, before);
        Score sKeyword = score(queries, keyword);
        Score sHybrid = score(queries, hybrid);
        report.append("## Before and after\n\n| Search | hit@1 | hit@5 | MRR | unexpected in top 5 |\n|---|---|---|---|---|\n");
        row(report, "Before C70 (contains-matching)", sBefore);
        row(report, "Keyword search (C70)", sKeyword);
        row(report, "Hybrid search (C70, stub model)", sHybrid);
        report.append("\n**hit@1**: share of queries whose expected record is first. **hit@5**: in the first five. "
            + "**MRR**: mean of 1/rank of the first expected record (0 when not found). **unexpected in top 5**: "
            + "average number of results in the first five that the query did not expect (lower is better; with "
            + "one expected record per query, some are normal).\n\n");

        report.append("### hit@5 by query type\n\n| Query type | Queries | Before | Keyword | Hybrid |\n|---|---|---|---|---|\n");
        for (String category : sKeyword.byCategory().keySet()) {
            report.append("| ").append(category).append(" | ").append((int) sKeyword.byCategory().get(category)[1])
                .append(" | ").append(pct(sBefore.byCategory().getOrDefault(category, new double[] {0, 1})))
                .append(" | ").append(pct(sKeyword.byCategory().get(category)))
                .append(" | ").append(pct(sHybrid.byCategory().get(category))).append(" |\n");
        }
        report.append("\n### Queries hybrid search does not find in the top 5\n\n");
        if (sHybrid.misses().isEmpty()) {
            report.append("None.\n\n");
        } else {
            sHybrid.misses().forEach(m -> report.append("- ").append(m).append('\n'));
            report.append('\n');
        }

        // ---- speed ------------------------------------------------------------
        int[] synthetic = addSyntheticDocuments(2000, 1000, 10000);
        report.append("## Speed\n\n");
        report.append("Index for the measurement: the test set plus ").append(synthetic[0]).append(" synthetic articles, ")
            .append(synthetic[1]).append(" synthetic products and ").append(synthetic[2])
            .append(" synthetic tickets (").append(jdbc.queryForObject("SELECT count(*) FROM search_document", Long.class))
            .append(" records, ").append(jdbc.queryForObject("SELECT count(*) FROM search_chunk", Long.class))
            .append(" embedded passages). Each of the ").append(queries.size())
            .append(" queries ran 10 times after a warm-up, signed in as a customer with tickets, measured inside the "
                + "backend (search service call, including the database and the HTTP call to the embedding service).\n\n");
        Long customer = jdbc.queryForObject("SELECT owner_customer_id FROM search_document WHERE visibility = 'OWNER' LIMIT 1", Long.class);
        double[] keywordTimes = time(queries, q -> searchService.search(q.q(), null, customer, GlobalSearchService.Mode.KEYWORD, null, false));
        double[] hybridTimes = time(queries, q -> searchService.search(q.q(), null, customer, GlobalSearchService.Mode.HYBRID, null, false));
        report.append("| Search | p50 | p95 | max | Target p95 |\n|---|---|---|---|---|\n");
        report.append(String.format(Locale.ROOT, "| Keyword | %.1f ms | %.1f ms | %.1f ms | under 500 ms |%n", keywordTimes[0], keywordTimes[1], keywordTimes[2]));
        report.append(String.format(Locale.ROOT, "| Hybrid (stub model) | %.1f ms | %.1f ms | %.1f ms | under 800 ms |%n", hybridTimes[0], hybridTimes[1], hybridTimes[2]));
        report.append("\nThe hybrid figure excludes a real model's own time to embed the query. A small multilingual model on "
            + "CPU typically adds tens of milliseconds; this was **not measured** because no real model was available. "
            + "The backend gives the model ").append(400).append(" ms (app.search.semantic.timeout-ms) and falls back to "
            + "keyword results after that.\n\n");

        report.append("## Final values\n\n| Setting | Value |\n|---|---|\n");
        report.append("| app.search.typo-threshold | ").append(bestTypo).append(" |\n");
        report.append("| app.search.correction-threshold | ").append(bestCorrection).append(" |\n");
        report.append("| app.search.semantic.similarity-threshold | ").append(bestSimilarity).append(" (stub model; re-tune) |\n");
        report.append("| app.search.semantic.chunk-size | ").append((int) bestChunk).append(" (stub model; re-tune) |\n");
        report.append("| app.search.semantic.chunk-overlap | ").append(settings.getChunkOverlap()).append(" |\n");
        report.append("| app.search.semantic.rrf-k | ").append(settings.getRrfK()).append(" |\n");

        Path out = Path.of("target/search-quality-report.md");
        Files.writeString(out, report.toString());
        System.out.println(report);
        assertThat(sKeyword.hitAt5()).isGreaterThanOrEqualTo(sBefore.hitAt5());
        assertThat(keywordTimes[1]).isLessThan(500);
        assertThat(hybridTimes[1]).isLessThan(800);
    }

    // ---- helpers -----------------------------------------------------------------

    private void remember(String key, String type, Long id) {
        keyByRecord.put(type + ":" + id, key);
        recordByKey.put(key, type + ":" + id);
    }

    private List<String> keys(List<SearchResultItemDto> results) {
        return results.stream().map(r -> keyByRecord.getOrDefault(r.type() + ":" + r.id(), "other")).toList();
    }

    private void set(String field, Object value) {
        ReflectionTestUtils.setField(settings, field, value);
    }

    private void rebuild() {
        assertThat(indexService.rebuildAll(SearchIndexRun.Trigger.ADMIN).getStatus()).isEqualTo(SearchIndexRun.Status.DONE);
        semanticIndexService.embedPending(Integer.MAX_VALUE);
        indexService.refreshTerms();
    }

    private double tune(StringBuilder report, double[] values, java.util.function.DoubleConsumer apply, List<Query> queries,
                        Function<Query, List<String>> engine) {
        double current = tuningDefault;
        double best = values[0];
        Score bestScore = null;
        Map<Double, Score> scores = new LinkedHashMap<>();
        for (double value : values) {
            apply.accept(value);
            Score s = score(queries, engine);
            scores.put(value, s);
            report.append("| ").append(value % 1 == 0 ? String.valueOf((int) value) : String.valueOf(value)).append(" | ")
                .append(fmt(s.hitAt1())).append(" | ").append(fmt(s.hitAt5())).append(" | ").append(fmt(s.mrr()))
                .append(" | ").append(String.format(Locale.ROOT, "%.2f", s.noiseAt5())).append(" |\n");
            if (bestScore == null || s.hitAt5() > bestScore.hitAt5()
                || (s.hitAt5() == bestScore.hitAt5() && s.mrr() > bestScore.mrr() + 1e-9)
                || (s.hitAt5() == bestScore.hitAt5() && Math.abs(s.mrr() - bestScore.mrr()) < 1e-9 && s.noiseAt5() < bestScore.noiseAt5())) {
                best = value;
                bestScore = s;
            }
        }
        // On an exact tie with the value already configured, keep the configured value.
        Score atCurrent = scores.get(current);
        if (atCurrent != null && atCurrent.hitAt5() == bestScore.hitAt5() && Math.abs(atCurrent.mrr() - bestScore.mrr()) < 1e-9
            && Math.abs(atCurrent.noiseAt5() - bestScore.noiseAt5()) < 1e-9) {
            report.append("\nTie with the configured value ").append(current).append(", which is kept.\n");
            return current;
        }
        return best;
    }

    /** The configured value of the setting being tuned. */
    private double tuningDefault;

    private Score score(List<Query> queries, Function<Query, List<String>> engine) {
        double hit1 = 0, hit5 = 0, mrr = 0, noise = 0;
        Map<String, double[]> byCategory = new LinkedHashMap<>();
        List<String> misses = new ArrayList<>();
        for (Query q : queries) {
            List<String> ranked = engine.apply(q);
            int rank = -1;
            for (int i = 0; i < ranked.size(); i++) {
                if (q.expect().contains(ranked.get(i))) {
                    rank = i + 1;
                    break;
                }
            }
            if (rank == 1) {
                hit1++;
            }
            boolean inTop5 = rank > 0 && rank <= 5;
            if (inTop5) {
                hit5++;
            } else {
                misses.add("`" + q.q() + "` (" + q.lang() + ", " + q.category() + ")");
            }
            mrr += rank > 0 ? 1.0 / rank : 0;
            noise += ranked.stream().limit(5).filter(k -> !q.expect().contains(k)).count();
            double[] c = byCategory.computeIfAbsent(q.category(), k -> new double[2]);
            c[0] += inTop5 ? 1 : 0;
            c[1] += 1;
        }
        int n = queries.size();
        return new Score(hit1 / n, hit5 / n, mrr / n, noise / n, byCategory, misses);
    }

    private static void row(StringBuilder report, String name, Score s) {
        report.append("| ").append(name).append(" | ").append(fmt(s.hitAt1())).append(" | ").append(fmt(s.hitAt5()))
            .append(" | ").append(fmt(s.mrr())).append(" | ").append(String.format(Locale.ROOT, "%.2f", s.noiseAt5())).append(" |\n");
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.0f%%", v * 100).replace("%%", "%");
    }

    private static String pct(double[] c) {
        return String.format(Locale.ROOT, "%.0f%%", 100 * c[0] / c[1]);
    }

    private double[] time(List<Query> queries, java.util.function.Consumer<Query> run) {
        queries.forEach(run);
        List<Double> times = new ArrayList<>();
        for (int round = 0; round < 10; round++) {
            for (Query q : queries) {
                long start = System.nanoTime();
                run.accept(q);
                times.add((System.nanoTime() - start) / 1_000_000.0);
            }
        }
        times.sort(Double::compare);
        return new double[] {times.get(times.size() / 2), times.get((int) Math.ceil(times.size() * 0.95) - 1),
            times.get(times.size() - 1)};
    }

    /** Inserts synthetic index rows directly (no source records), with stub embeddings for public passages. */
    private int[] addSyntheticDocuments(int articles, int products, int tickets) {
        String[] words = ("invoice payment receipt billing subscription renewal seats members organization admin "
            + "password sign single saml mfa reset export report csv dashboard analytics orders inventory store "
            + "payroll attendance leave helpdesk ticket chat pipeline warehouse data api key header limit status "
            + "incident maintenance refund cancel time zone date format factura pago recibo suscripcion renovacion "
            + "miembros contrasena exportar informe almacen pedidos nomina vacaciones soporte estado clave").split(" ");
        Random random = new Random(42);
        Function<Integer, String> text = n -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append(words[random.nextInt(words.length)]).append(i % 12 == 11 ? ". " : " ");
            }
            return sb.toString().trim();
        };
        long base = 10_000_000L;
        List<Object[]> docs = new ArrayList<>();
        for (int i = 0; i < articles; i++) {
            docs.add(new Object[] {"KNOWLEDGE", base + i, "PUBLIC", null, "Synthetic guide " + i + " " + text.apply(4), text.apply(160)});
        }
        for (int i = 0; i < products; i++) {
            docs.add(new Object[] {"PRODUCT", base + i, "PUBLIC", null, "Synthetic app " + i + " " + text.apply(2), text.apply(30)});
        }
        for (int i = 0; i < tickets; i++) {
            docs.add(new Object[] {"TICKET", base + i, "OWNER", 900_000L + (i % 500), "Synthetic ticket " + i + " " + text.apply(4), text.apply(40)});
        }
        jdbc.batchUpdate("INSERT INTO search_document (source_type, source_id, visibility, owner_customer_id, reference, "
                + "title, body, title_folded, body_folded, indexed_at) VALUES (?, ?, ?, ?, ?, ?, ?, lower(?), lower(?), now())",
            docs.stream().map(d -> new Object[] {d[0], d[1], d[2], d[3], "#" + d[1], d[4], d[5], d[4], d[5]}).toList());
        List<Object[]> chunks = new ArrayList<>();
        jdbc.query("SELECT id, title, body FROM search_document WHERE source_id >= ? AND visibility = 'PUBLIC'", rs -> {
            List<String> parts = Chunker.chunk(rs.getString(2), rs.getString(3), settings.getChunkSize(), settings.getChunkOverlap());
            for (int i = 0; i < parts.size(); i++) {
                chunks.add(new Object[] {rs.getLong(1), i, parts.get(i), SemanticIndexService.sha256(parts.get(i)),
                    SearchSqlTestAccess.vector(StubEmbeddingServer.embed(parts.get(i)))});
            }
        }, base);
        jdbc.batchUpdate("INSERT INTO search_chunk (document_id, chunk_index, content, content_hash, embedding, embedded_at) "
            + "VALUES (?, ?, ?, ?, ?::vector, now())", chunks);
        jdbc.execute("ANALYZE search_document");
        jdbc.execute("ANALYZE search_chunk");
        return new int[] {articles, products, tickets};
    }

    /** Vector literal for JDBC inserts. */
    static final class SearchSqlTestAccess {
        static String vector(float[] v) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < v.length; i++) {
                sb.append(i == 0 ? "" : ",").append(v[i]);
            }
            return sb.append(']').toString();
        }
    }
}
