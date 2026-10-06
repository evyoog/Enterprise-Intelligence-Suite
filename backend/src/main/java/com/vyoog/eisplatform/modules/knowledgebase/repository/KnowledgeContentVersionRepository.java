package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeContentVersionRepository extends JpaRepository<KnowledgeContentVersion, Long> {

    List<KnowledgeContentVersion> findByContentIdOrderByPublishedAtDescIdDesc(Long contentId);

    Optional<KnowledgeContentVersion> findByContentIdAndVersionLabel(Long contentId, String versionLabel);

    void deleteByContentId(Long contentId);

    /** Versions whose effective or expiry time fell in (from, to] — they enter or leave search. */
    @org.springframework.data.jpa.repository.Query("SELECT v.contentId FROM KnowledgeContentVersion v WHERE "
        + "(v.effectiveAt > :from AND v.effectiveAt <= :to) OR (v.expiresAt > :from AND v.expiresAt <= :to)")
    List<Long> contentWithTimeChanges(@org.springframework.data.repository.query.Param("from") java.time.Instant from,
                                      @org.springframework.data.repository.query.Param("to") java.time.Instant to);
}
