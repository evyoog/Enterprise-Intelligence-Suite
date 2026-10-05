package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.dto.SearchHighlightDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds which words of a title or snippet match the query, for highlighting
 * (REQ-PRT-002 BR-SRCH-007). Works on the original text, comparing folded
 * words, so accented words are highlighted too. A text word matches a query
 * word when one starts with the other's stem (the query word minus up to two
 * trailing letters, at least 3 letters): "invoice" highlights "invoices" and
 * "invoicing"; "fact" highlights "facturación".
 */
public final class Highlighter {

    public static final int SNIPPET_LENGTH = 200;
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");

    private Highlighter() {
    }

    public static List<SearchHighlightDto> highlight(String text, Collection<String> foldedTerms) {
        List<SearchHighlightDto> ranges = new ArrayList<>();
        if (text == null || foldedTerms.isEmpty()) {
            return ranges;
        }
        Matcher word = WORD.matcher(text);
        while (word.find()) {
            String folded = TextFolding.fold(word.group());
            if (matchesAny(folded, foldedTerms)) {
                ranges.add(new SearchHighlightDto(word.start(), word.end() - word.start()));
            }
        }
        return ranges;
    }

    /** About {@value #SNIPPET_LENGTH} characters of {@code body}, starting
     * shortly before the first matching word (or at the start when none
     * matches), cut at word boundaries, with "…" where text was cut. */
    public static String snippet(String body, Collection<String> foldedTerms) {
        if (body == null || body.isBlank()) {
            return null;
        }
        String text = body.replaceAll("\\s+", " ").trim();
        if (text.length() <= SNIPPET_LENGTH) {
            return text;
        }
        int start = 0;
        Matcher word = WORD.matcher(text);
        while (word.find()) {
            if (matchesAny(TextFolding.fold(word.group()), foldedTerms)) {
                start = Math.max(0, word.start() - 40);
                break;
            }
        }
        if (start > 0) {
            int space = text.indexOf(' ', start);
            start = space < 0 || space > start + 20 ? start : space + 1;
        }
        int end = Math.min(text.length(), start + SNIPPET_LENGTH);
        if (end < text.length()) {
            int space = text.lastIndexOf(' ', end);
            end = space > start + SNIPPET_LENGTH / 2 ? space : end;
        }
        return (start > 0 ? "…" : "") + text.substring(start, end) + (end < text.length() ? "…" : "");
    }

    static boolean matchesAny(String foldedWord, Collection<String> foldedTerms) {
        for (String term : foldedTerms) {
            if (term.length() < 2) {
                continue;
            }
            String stem = stem(term);
            if (foldedWord.equals(term) || foldedWord.startsWith(stem)
                || (foldedWord.length() >= 3 && term.startsWith(stem(foldedWord)))) {
                return true;
            }
        }
        return false;
    }

    private static String stem(String word) {
        if (word.length() <= 3) {
            return word;
        }
        return word.substring(0, Math.max(3, word.length() - 2));
    }
}
