package com.vyoog.eisplatform.modules.search.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds PostgreSQL to_tsquery expressions (C70). Input words come from
 * {@link TextFolding#tokens} — letters and digits only — so no user text can
 * reach the query syntax.
 */
final class TsQueryBuilder {

    private TsQueryBuilder() {
    }

    /** All words must match (any synonym of a word counts); quoted phrases must
     * match as phrases. Stemming is applied by the to_tsquery configuration. */
    static String allWords(ParsedQuery query, List<String> terms, Function<String, List<List<String>>> alternatives) {
        List<String> groups = new ArrayList<>();
        Set<String> inPhrases = new HashSet<>();
        for (List<String> phrase : query.phrases()) {
            groups.add("(" + String.join(" <-> ", phrase) + ")");
            inPhrases.addAll(phrase);
        }
        for (String term : terms) {
            if (inPhrases.contains(term)) {
                continue;
            }
            List<List<String>> alts = alternatives.apply(term);
            groups.add(alts.stream().map(words -> String.join(" <-> ", words))
                .collect(Collectors.joining(" | ", "(", ")")));
        }
        return String.join(" & ", groups);
    }

    /** Every word as a prefix: "inv:* & fact:*". */
    static String prefixes(List<String> terms) {
        return terms.stream().map(t -> t + ":*").collect(Collectors.joining(" & "));
    }

    /** The words in the order typed, next to each other. Quoted phrases use
     * their own words; otherwise the whole query (two words or more). */
    static String phrase(ParsedQuery query) {
        if (query.hasQuotedPhrase()) {
            return query.phrases().stream().map(p -> "(" + String.join(" <-> ", p) + ")")
                .collect(Collectors.joining(" & "));
        }
        return query.terms().size() < 2 ? "" : String.join(" <-> ", query.terms());
    }
}
