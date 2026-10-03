package com.vyoog.eisplatform.modules.integration.repository;

import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long>, JpaSpecificationExecutor<OutboxEvent> {

    @Query("select e from OutboxEvent e where e.status = :status and (e.nextAttemptAt is null or e.nextAttemptAt <= :now) "
        + "order by e.occurredAt asc, e.id asc")
    List<OutboxEvent> findDue(@Param("status") OutboxEventStatus status, @Param("now") Instant now, Pageable pageable);

    /** REQ-INT-002.5: an earlier event of the same aggregate is not yet delivered. */
    @Query("select count(e) > 0 from OutboxEvent e where e.aggregateType = :aggregateType and e.aggregateId = :aggregateId "
        + "and e.status <> com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus.DELIVERED "
        + "and (e.occurredAt < :occurredAt or (e.occurredAt = :occurredAt and e.id < :id))")
    boolean existsEarlierUndelivered(@Param("aggregateType") String aggregateType, @Param("aggregateId") String aggregateId,
                                     @Param("occurredAt") Instant occurredAt, @Param("id") Long id);

    @Query("select distinct e.eventType from OutboxEvent e order by e.eventType")
    List<String> findDistinctEventTypes();

    Optional<OutboxEvent> findByEventId(String eventId);

    List<OutboxEvent> findByEventTypeOrderByIdAsc(String eventType);

    List<OutboxEvent> findByAggregateTypeAndAggregateIdOrderByIdAsc(String aggregateType, String aggregateId);

    @Modifying
    @Query("delete from OutboxEvent e where e.status = com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus.DELIVERED "
        + "and e.deliveredAt < :cutoff")
    int deleteDeliveredBefore(@Param("cutoff") Instant cutoff);
}
