package com.vyoog.eisplatform.modules.search.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lower-case, accent-free text for the search index and for queries
 * (REQ-PRT-002 BR-SRCH-004): "Facturación" and "facturacion" fold to the same
 * word, so Spanish users find content whether or not they type accents.
 */
public final class TextFolding {

    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}]+");

    private TextFolding() {
    }

    public static String fold(String text) {
        if (text == null) {
            return null;
        }
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return MARKS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
    }

    private static final Pattern PUNCTUATION = Pattern.compile("[^\\p{L}\\p{N}\\s]+");

    /** Folded text with punctuation turned into spaces, as stored in the
     * index: "Sign-on" and "sign on" become the same two words, so phrase
     * matching does not depend on hyphens. */
    public static String searchable(String text) {
        String folded = fold(text);
        return folded == null ? null : PUNCTUATION.matcher(folded).replaceAll(" ").replaceAll("\\s+", " ").trim();
    }

    /** Words (letters and digits) of already-folded text, in order. */
    public static List<String> tokens(String foldedText) {
        List<String> tokens = new ArrayList<>();
        if (foldedText == null) {
            return tokens;
        }
        Matcher matcher = TOKEN.matcher(foldedText);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }
}
