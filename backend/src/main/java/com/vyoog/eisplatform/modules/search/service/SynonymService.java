package com.vyoog.eisplatform.modules.search.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.search.dto.SynonymDto;
import com.vyoog.eisplatform.modules.search.model.SearchSynonym;
import com.vyoog.eisplatform.modules.search.repository.SearchSynonymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Admin-managed synonyms (C70). A group lists equivalent terms; searching any
 * of them also finds the others ("invoice, bill, factura"). Terms are stored
 * folded (lower case, no accents). A term may be several words
 * ("credit note"); it then matches those words next to each other.
 */
@Service
@RequiredArgsConstructor
public class SynonymService {

    private final SearchSynonymRepository synonymRepository;
    private volatile Map<String, List<List<String>>> cache;

    @Transactional(readOnly = true)
    public List<SynonymDto> list() {
        return synonymRepository.findAllByOrderByCreatedAtAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public SynonymDto create(List<String> terms) {
        List<String> clean = new ArrayList<>(new LinkedHashSet<>(terms.stream()
            .map(t -> String.join(" ", TextFolding.tokens(TextFolding.fold(t))))
            .filter(t -> !t.isBlank())
            .toList()));
        if (clean.size() < 2) {
            throw new IllegalArgumentException("A synonym group needs at least two different terms.");
        }
        SearchSynonym synonym = new SearchSynonym();
        synonym.setTerms(String.join(",", clean));
        synonym.setCreatedAt(Instant.now());
        SynonymDto dto = toDto(synonymRepository.save(synonym));
        cache = null;
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        if (!synonymRepository.existsById(id)) {
            throw new ResourceNotFoundException("Synonym group not found: " + id);
        }
        synonymRepository.deleteById(id);
        cache = null;
    }

    /** For a single query word: the word itself plus every synonym of it,
     * each as a list of words. */
    public List<List<String>> alternativesFor(String term) {
        List<List<String>> alternatives = new ArrayList<>();
        alternatives.add(List.of(term));
        for (List<String> synonym : groups().getOrDefault(term, List.of())) {
            if (!synonym.equals(List.of(term))) {
                alternatives.add(synonym);
            }
        }
        return alternatives;
    }

    /** The words plus every word of their synonyms (for highlighting). */
    public Set<String> expandWords(Collection<String> terms) {
        Set<String> out = new LinkedHashSet<>(terms);
        for (String term : terms) {
            groups().getOrDefault(term, List.of()).forEach(out::addAll);
        }
        return out;
    }

    private Map<String, List<List<String>>> groups() {
        Map<String, List<List<String>>> current = cache;
        if (current == null) {
            current = new ConcurrentHashMap<>();
            for (SearchSynonym synonym : synonymRepository.findAll()) {
                List<List<String>> members = Arrays.stream(synonym.getTerms().split(","))
                    .map(t -> TextFolding.tokens(t.trim()))
                    .filter(t -> !t.isEmpty())
                    .toList();
                for (List<String> member : members) {
                    if (member.size() == 1) {
                        current.computeIfAbsent(member.get(0), k -> new ArrayList<>()).addAll(members);
                    }
                }
            }
            cache = current;
        }
        return current;
    }

    private SynonymDto toDto(SearchSynonym synonym) {
        return new SynonymDto(synonym.getId(), Arrays.stream(synonym.getTerms().split(",")).map(String::trim).toList(),
            synonym.getCreatedAt());
    }
}
