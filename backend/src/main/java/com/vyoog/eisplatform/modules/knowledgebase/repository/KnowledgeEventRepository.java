package com.vyoog.eisplatform.modules.knowledgebase.repository;

import com.vyoog.eisplatform.modules.knowledgebase.model.KnowledgeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface KnowledgeEventRepository extends JpaRepository<KnowledgeEvent, Long> {

    boolean existsByEventTypeAndContentIdAndViewerHashAndCreatedAtAfter(
        KnowledgeEvent.Type type, Long contentId, String viewerHash, Instant after);

    /** [eventType, count] since a time. */
    @Query("SELECT e.eventType, COUNT(e) FROM KnowledgeEvent e WHERE e.createdAt >= :since GROUP BY e.eventType")
    List<Object[]> countByTypeSince(@Param("since") Instant since);

    /** [contentId, count] of one event type since a time, most first. */
    @Query("SELECT e.contentId, COUNT(e) FROM KnowledgeEvent e WHERE e.eventType = :type AND e.createdAt >= :since "
        + "GROUP BY e.contentId ORDER BY COUNT(e) DESC")
    List<Object[]> topContent(@Param("type") KnowledgeEvent.Type type, @Param("since") Instant since);

    /** [contentId, count] of one event type, all time (popularity). */
    @Query("SELECT e.contentId, COUNT(e) FROM KnowledgeEvent e WHERE e.eventType = :type GROUP BY e.contentId")
    List<Object[]> totals(@Param("type") KnowledgeEvent.Type type);

    /** [sourceType, count] of video plays since a time. */
    @Query("SELECT e.sourceType, COUNT(e) FROM KnowledgeEvent e WHERE e.eventType = 'VIDEO_PLAYED' "
        + "AND e.createdAt >= :since GROUP BY e.sourceType")
    List<Object[]> playsBySource(@Param("since") Instant since);

    @Query("SELECT COUNT(e) FROM KnowledgeEvent e WHERE e.eventType = 'VIDEO_PROGRESS' AND e.percent >= 95 "
        + "AND e.createdAt >= :since")
    long completionsSince(@Param("since") Instant since);

    @Query("SELECT AVG(e.seconds) FROM KnowledgeEvent e WHERE e.eventType = 'VIDEO_PROGRESS' AND e.seconds IS NOT NULL "
        + "AND e.createdAt >= :since")
    Double averageWatchSecondsSince(@Param("since") Instant since);

    void deleteByContentId(Long contentId);
}
