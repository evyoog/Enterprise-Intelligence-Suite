package com.vyoog.eisplatform.modules.search.service;

import java.util.List;

/**
 * A search query split into its parts (REQ-PRT-002).
 *
 * @param raw       what the user typed, trimmed
 * @param folded    the query words, lower-case and accent-free, separated by single spaces
 * @param terms     every word, folded, in order (phrase words included)
 * @param phrases   word lists the user put in double quotes
 * @param exactReference "#123" when the whole query is an ID ("#123" or "123")
 */
public record ParsedQuery(
    String raw,
    String folded,
    List<String> terms,
    List<List<String>> phrases,
    String exactReference
) {

    public boolean isBlank() {
        return terms.isEmpty() && exactReference == null;
    }

    public boolean hasQuotedPhrase() {
        return !phrases.isEmpty();
    }
}
