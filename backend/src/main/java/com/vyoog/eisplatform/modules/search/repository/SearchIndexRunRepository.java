package com.vyoog.eisplatform.modules.search.repository;

import com.vyoog.eisplatform.modules.search.model.SearchIndexRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchIndexRunRepository extends JpaRepository<SearchIndexRun, Long> {

    Optional<SearchIndexRun> findTopByOrderByStartedAtDesc();
}
