package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.modules.search.repository.KeywordSqlQuery;
import com.vyoog.eisplatform.modules.search.repository.SearchRow;
import com.vyoog.eisplatform.modules.search.repository.SearchSqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Keyword search on the PostgreSQL index (REQ-PRT-002, C70). Match tiers, best
 * first: exact ID ("#42"), exact phrase, all words (English or Spanish word
 * forms, synonyms), partial words (prefixes), typos (similar title, or the
 * query with misspelt words corrected). Quoted phrases turn off the partial
 * and typo tiers. When the first pass finds nothing, misspelt words are
 * corrected against the words of public content and the query runs again;
 * the corrected query is returned as "Did you mean".
 */
@Service
@RequiredArgsConstructor
public class PostgresKeywordSearch {

    private final SearchSqlRepository sqlRepository;
    private final SynonymService synonymService;
    private final SearchSettings settings;

    @Transactional(readOnly = true)
    public KeywordSearchOutcome search(ParsedQuery query, String sourceType, Long customerId, int limit) {
        boolean loose = !query.hasQuotedPhrase();
        Set<String> highlight = synonymService.expandWords(query.terms());
        String all = TsQueryBuilder.allWords(query, query.terms(), synonymService::alternativesFor);
        KeywordSqlQuery sql = new KeywordSqlQuery(all, all, loose ? TsQueryBuilder.prefixes(query.terms()) : "",
            TsQueryBuilder.phrase(query), "", "", query.folded(), query.terms(), query.exactReference(), customerId, sourceType,
            loose, settings.getTypoThreshold(), limit);
        List<SearchRow> rows = sqlRepository.keywordSearch(sql);
        if (!rows.isEmpty() || !loose || query.terms().isEmpty()) {
            return new KeywordSearchOutcome(rows, null, highlight);
        }

        Optional<List<String>> corrected = correct(query.terms());
        if (corrected.isEmpty()) {
            return new KeywordSearchOutcome(rows, null, highlight);
        }
        List<String> fixed = corrected.get();
        String correctedAll = TsQueryBuilder.allWords(query, fixed, synonymService::alternativesFor);
        rows = sqlRepository.keywordSearch(new KeywordSqlQuery("", "", "", "", correctedAll, correctedAll,
            String.join(" ", fixed), fixed, null, customerId, sourceType, true, settings.getTypoThreshold(), limit));
        Set<String> withFixes = new LinkedHashSet<>(highlight);
        withFixes.addAll(synonymService.expandWords(fixed));
        return new KeywordSearchOutcome(rows, rows.isEmpty() ? null : String.join(" ", fixed), withFixes);
    }

    /** Replaces words not found in public content with the closest word that is. */
    private Optional<List<String>> correct(List<String> terms) {
        Set<String> known = sqlRepository.knownTerms(terms);
        List<String> out = new ArrayList<>(terms.size());
        boolean changed = false;
        for (String term : terms) {
            if (term.length() < 3 || known.contains(term) || term.chars().allMatch(Character::isDigit)) {
                out.add(term);
                continue;
            }
            Optional<String> closest = sqlRepository.closestTerm(term, settings.getCorrectionThreshold());
            if (closest.isPresent() && !closest.get().equals(term)) {
                out.add(closest.get());
                changed = true;
            } else {
                out.add(term);
            }
        }
        return changed ? Optional.of(out) : Optional.empty();
    }
}
