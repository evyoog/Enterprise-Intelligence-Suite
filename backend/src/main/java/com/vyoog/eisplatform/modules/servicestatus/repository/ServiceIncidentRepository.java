package com.vyoog.eisplatform.modules.servicestatus.repository;

import com.vyoog.eisplatform.modules.servicestatus.model.ServiceIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ServiceIncidentRepository extends JpaRepository<ServiceIncident, Long> {

    List<ServiceIncident> findTop200ByOrderByStartedAtDesc();

    List<ServiceIncident> findTop50ByProductIdInOrderByStartedAtDesc(Collection<Long> productIds);

    long countByProductIdAndEndedAtIsNull(Long productId);
}
