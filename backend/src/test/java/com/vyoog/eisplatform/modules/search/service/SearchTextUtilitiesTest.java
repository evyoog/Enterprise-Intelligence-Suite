package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.dto.SearchHighlightDto;
import com.vyoog.eisplatform.modules.search.repository.SearchRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** C70: query parsing, tsquery building, highlighting, chunking and hybrid merging. */
class SearchTextUtilitiesTest {

    @Test
    void foldsCaseAndAccents() {
        assertThat(TextFolding.fold("Facturación ÑANDÚ")).isEqualTo("facturacion nandu");
        assertThat(TextFolding.tokens("a-b c_d e.f")).containsExactly("a", "b", "c", "d", "e", "f");
    }

    @Test
    void parsesPhrasesIdsAndLimits() {
        ParsedQuery q = QueryParser.parse("  \"Single Sign-On\" setup ");
        assertThat(q.terms()).containsExactly("single", "sign", "on", "setup");
        assertThat(q.phrases()).containsExactly(List.of("single", "sign", "on"));
        assertThat(QueryParser.parse("#0042").exactReference()).isEqualTo("#42");
        assertThat(QueryParser.parse("42").exactReference()).isEqualTo("#42");
        assertThat(QueryParser.parse("42 apps").exactReference()).isNull();
        assertThat(QueryParser.parse("a b c d e f g h i j k l m n").terms()).hasSize(QueryParser.MAX_TERMS);
        assertThat(QueryParser.parse("   ").isBlank()).isTrue();
        assertThat(QueryParser.parse("'); DROP TABLE x; --").terms()).containsExactly("drop", "table", "x");
    }

    @Test
    void buildsTsQueriesFromWordsOnly() {
        ParsedQuery q = QueryParser.parse("pay \"credit note\"");
        String all = TsQueryBuilder.allWords(q, q.terms(),
            t -> t.equals("pay") ? List.of(List.of("pay"), List.of("settle", "up")) : List.of(List.of(t)));
        assertThat(all).isEqualTo("(credit <-> note) & (pay | settle <-> up)");
        assertThat(TsQueryBuilder.prefixes(List.of("inv", "fac"))).isEqualTo("inv:* & fac:*");
        assertThat(TsQueryBuilder.phrase(QueryParser.parse("reset password"))).isEqualTo("reset <-> password");
        assertThat(TsQueryBuilder.phrase(QueryParser.parse("reset"))).isEmpty();
    }

    @Test
    void highlightsWordFormsAndAccentedWords() {
        String title = "Facturación: paying invoices";
        List<SearchHighlightDto> ranges = Highlighter.highlight(title, List.of("factura", "invoice"));
        assertThat(ranges).extracting(r -> title.substring(r.start(), r.start() + r.length()))
            .containsExactly("Facturación", "invoices");
        assertThat(Highlighter.highlight(title, List.of())).isEmpty();
    }

    @Test
    void snippetStartsNearTheFirstMatch() {
        String body = "Intro. ".repeat(60) + "The renewal date is shown on the invoice. " + "More text. ".repeat(30);
        String snippet = Highlighter.snippet(body, List.of("renewal"));
        assertThat(snippet).startsWith("…").contains("renewal date").endsWith("…");
        assertThat(snippet.length()).isLessThanOrEqualTo(Highlighter.SNIPPET_LENGTH + 2);
        assertThat(Highlighter.snippet("Short.", List.of("x"))).isEqualTo("Short.");
        assertThat(Highlighter.snippet(null, List.of("x"))).isNull();
    }

    @Test
    void chunksWithOverlapAndTitle() {
        String text = "One sentence here. ".repeat(40);
        List<String> chunks = Chunker.chunk("Guide", text, 120, 40);
        assertThat(chunks).hasSizeGreaterThan(3);
        assertThat(chunks).allMatch(c -> c.startsWith("Guide\n") && c.length() <= 120 + 6);
        String firstEnd = chunks.get(0).substring(chunks.get(0).lastIndexOf("One"));
        assertThat(chunks.get(1).substring(6)).startsWith(firstEnd.trim());
        assertThat(Chunker.chunk("Only title", null, 100, 10)).containsExactly("Only title");
        assertThat(Chunker.chunk("T", "x".repeat(250), 100, 10)).hasSize(3);
    }

    @Test
    void hybridMergeKeepsExactIdFirstAndRewardsBothLists() {
        SearchRow exact = row(1, 1);
        SearchRow keywordOnly = row(2, 3);
        SearchRow both = row(3, 3);
        SearchRow semanticOnly = row(4, 6);
        List<HybridMerger.Merged> merged = HybridMerger.merge(List.of(exact, keywordOnly, both),
            List.of(row(3, 6), semanticOnly), 60, 10);
        assertThat(merged).extracting(m -> m.row().documentId()).containsExactly(1L, 3L, 2L, 4L);
        assertThat(merged.get(1).row().tier()).isEqualTo(3);
        assertThat(merged.get(3).row().tier()).isEqualTo(6);
        assertThat(HybridMerger.merge(List.of(exact, keywordOnly), List.of(), 60, 1)).hasSize(1);
    }

    private static SearchRow row(long id, int tier) {
        return new SearchRow(id, "KNOWLEDGE", id, "#" + id, "T" + id, null, tier, 0, tier == 6 ? "T\npassage" : null);
    }
}
