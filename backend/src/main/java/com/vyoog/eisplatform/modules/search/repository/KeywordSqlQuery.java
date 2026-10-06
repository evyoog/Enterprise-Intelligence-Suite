package com.vyoog.eisplatform.modules.search.repository;

/**
 * Inputs of one keyword query (C70). The *Query strings are PostgreSQL
 * to_tsquery expressions built by the service from sanitised words only;
 * an empty string means "not used".
 *
 * @param englishQuery / spanishQuery all words (with synonyms), stemmed by that language
 * @param prefixQuery  every word as a prefix ("invo:*"), unstemmed
 * @param phraseQuery  the words in order, unstemmed ("a <-> b")
 * @param correctedEnglishQuery / correctedSpanishQuery the same after typo correction
 * @param folded       the folded query text, for title similarity
 * @param terms        the query words; a typo match on the title needs every one of them
 * @param exactReference "#123" or null
 * @param customerId   the caller; null when signed out
 * @param sourceType   PRODUCT, KNOWLEDGE, TICKET or null for all
 * @param loose        allow the partial-word and typo tiers (false for quoted phrases)
 */
public record KeywordSqlQuery(
    String englishQuery,
    String spanishQuery,
    String prefixQuery,
    String phraseQuery,
    String correctedEnglishQuery,
    String correctedSpanishQuery,
    String folded,
    java.util.List<String> terms,
    String exactReference,
    Long customerId,
    String sourceType,
    boolean loose,
    double typoThreshold,
    int limit
) {
}
