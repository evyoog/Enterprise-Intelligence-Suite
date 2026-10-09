package com.vyoog.eisplatform.modules.toolsync.repository;

import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ToolDeliveryRepository extends JpaRepository<ToolDelivery, Long> {

    /** Due deliveries, oldest first. */
    @Query("select d from ToolDelivery d where d.status = com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus.PENDING "
        + "and (d.nextAttemptAt is null or d.nextAttemptAt <= :now) order by d.id")
    List<ToolDelivery> findDue(@Param("now") Instant now, Pageable pageable);

    /** A waiting delivery of this aggregate to this tool: it will carry the current state, so another one is not needed. */
    boolean existsByToolConnectorIdAndOrganizationIdAndAggregateTypeAndAggregateIdAndStatusAndAttempts(
        Long toolConnectorId, Long organizationId, String aggregateType, String aggregateId, DeliveryStatus status, int attempts);

    List<ToolDelivery> findByToolConnectorIdAndOrganizationIdAndStatus(Long toolConnectorId, Long organizationId, DeliveryStatus status);

    Page<ToolDelivery> findByStatusOrderByIdDesc(DeliveryStatus status, Pageable pageable);

    Page<ToolDelivery> findAllByOrderByIdDesc(Pageable pageable);

    Page<ToolDelivery> findByOrganizationIdOrderByIdDesc(Long organizationId, Pageable pageable);

    /** [connectorId, organizationId, status, count] for every waiting or failed delivery: the monitor's per-tenant counters in one query. */
    @Query("select d.toolConnectorId, d.organizationId, d.status, count(d) from ToolDelivery d "
        + "where d.status in (com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus.PENDING, com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus.FAILED) "
        + "group by d.toolConnectorId, d.organizationId, d.status")
    List<Object[]> countOpenByTenant();

    @Query("select d.toolConnectorId, d.organizationId, max(d.deliveredAt) from ToolDelivery d "
        + "where d.status = com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus.DELIVERED group by d.toolConnectorId, d.organizationId")
    List<Object[]> lastDeliveredByTenant();

    Page<ToolDelivery> findByToolConnectorIdOrderByIdDesc(Long toolConnectorId, Pageable pageable);

    Page<ToolDelivery> findByStatusAndOrganizationIdOrderByIdDesc(DeliveryStatus status, Long organizationId, Pageable pageable);

    long countByToolConnectorIdAndOrganizationIdAndStatus(Long toolConnectorId, Long organizationId, DeliveryStatus status);

    @Query("select max(d.deliveredAt) from ToolDelivery d where d.toolConnectorId = :connector and d.organizationId = :org and d.status = com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus.DELIVERED")
    Instant lastDelivered(@Param("connector") Long toolConnectorId, @Param("org") Long organizationId);

    @Query("select d from ToolDelivery d where d.toolConnectorId = :connector and d.organizationId = :org and d.status = com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus.FAILED order by d.id desc")
    List<ToolDelivery> findFailed(@Param("connector") Long toolConnectorId, @Param("org") Long organizationId, Pageable pageable);
}
