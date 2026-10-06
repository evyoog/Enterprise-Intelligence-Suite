package com.vyoog.eisplatform.modules.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Splits a query into words, quoted phrases and an exact ID (REQ-PRT-002). */
public final class QueryParser {

    /** At most this many words are used; the rest of a very long query is ignored. */
    public static final int MAX_TERMS = 12;
    private static final int MAX_TERM_LENGTH = 50;
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern EXACT_ID = Pattern.compile("^#?(\\d{1,18})$");

    private QueryParser() {
    }

    public static ParsedQuery parse(String query) {
        String raw = query == null ? "" : query.trim();
        if (raw.length() > 200) {
            raw = raw.substring(0, 200);
        }
        String folded = TextFolding.fold(raw);

        Matcher id = EXACT_ID.matcher(raw);
        String exactReference = id.matches() ? "#" + Long.parseLong(id.group(1)) : null;

        List<List<String>> phrases = new ArrayList<>();
        Matcher quoted = QUOTED.matcher(folded);
        while (quoted.find()) {
            List<String> words = limit(TextFolding.tokens(quoted.group(1)));
            if (words.size() > 1) {
                phrases.add(words);
            }
        }
        List<String> terms = limit(TextFolding.tokens(folded));
        return new ParsedQuery(raw, String.join(" ", terms), terms, phrases, exactReference);
    }

    private static List<String> limit(List<String> tokens) {
        List<String> out = new ArrayList<>();
        for (String token : tokens) {
            if (out.size() == MAX_TERMS) {
                break;
            }
            out.add(token.length() > MAX_TERM_LENGTH ? token.substring(0, MAX_TERM_LENGTH) : token);
        }
        return out;
    }
}
