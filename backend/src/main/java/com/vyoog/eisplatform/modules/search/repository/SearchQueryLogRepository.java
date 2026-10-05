package com.vyoog.eisplatform.modules.search.repository;

import com.vyoog.eisplatform.modules.search.model.SearchQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SearchQueryLogRepository extends JpaRepository<SearchQueryLog, Long> {

    List<SearchQueryLog> findBySearchedAtAfter(Instant since);

    List<SearchQueryLog> findByScopeAndSearchedAtAfter(String scope, Instant since);
}
