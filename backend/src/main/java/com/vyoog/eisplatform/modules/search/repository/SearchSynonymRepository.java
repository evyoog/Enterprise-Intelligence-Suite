package com.vyoog.eisplatform.modules.search.repository;

import com.vyoog.eisplatform.modules.search.model.SearchSynonym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchSynonymRepository extends JpaRepository<SearchSynonym, Long> {

    List<SearchSynonym> findAllByOrderByCreatedAtAsc();
}
