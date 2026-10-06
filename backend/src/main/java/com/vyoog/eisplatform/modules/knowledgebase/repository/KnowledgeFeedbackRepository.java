package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface KnowledgeFeedbackRepository extends JpaRepository<KnowledgeFeedback, Long> {

    Optional<KnowledgeFeedback> findFirstByContentIdAndVersionLabelAndVoterSubAndKind(
        Long contentId, String versionLabel, String voterSub, KnowledgeFeedback.Kind kind);

    /** [contentId, helpfulVotes, totalVotes] for votes since a time. */
    @Query("SELECT f.contentId, SUM(CASE WHEN f.helpful = true THEN 1 ELSE 0 END), COUNT(f) FROM KnowledgeFeedback f "
        + "WHERE f.kind = 'VOTE' AND f.createdAt >= :since GROUP BY f.contentId")
    List<Object[]> voteTotalsSince(@Param("since") Instant since);

    List<KnowledgeFeedback> findByKindInAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
        List<KnowledgeFeedback.Kind> kinds, Instant since);

    void deleteByContentId(Long contentId);
}
