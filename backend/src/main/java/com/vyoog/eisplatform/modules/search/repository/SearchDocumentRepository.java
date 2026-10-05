package com.vyoog.eisplatform.modules.search.repository;

import com.vyoog.eisplatform.modules.search.model.SearchDocument;
import com.vyoog.eisplatform.modules.search.model.SearchSourceType;
import com.vyoog.eisplatform.modules.search.model.SearchVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SearchDocumentRepository extends JpaRepository<SearchDocument, Long> {

    Optional<SearchDocument> findBySourceTypeAndSourceId(SearchSourceType sourceType, Long sourceId);

    long countBySourceType(SearchSourceType sourceType);

    long countByVisibility(SearchVisibility visibility);

    @Modifying
    @Query("DELETE FROM SearchDocument d WHERE d.sourceType = :type AND d.sourceId = :id")
    int deleteBySource(@Param("type") SearchSourceType type, @Param("id") Long id);

    @Modifying
    @Query("DELETE FROM SearchDocument d WHERE d.sourceType = :type AND d.indexedAt < :before")
    int deleteIndexedBefore(@Param("type") SearchSourceType type, @Param("before") Instant before);

    List<SearchDocument> findByVisibility(SearchVisibility visibility);
}
