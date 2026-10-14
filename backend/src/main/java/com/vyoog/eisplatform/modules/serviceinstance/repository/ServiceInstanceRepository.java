package com.vyoog.eisplatform.modules.serviceinstance.repository;

import com.vyoog.eisplatform.modules.serviceinstance.model.ServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceInstanceRepository extends JpaRepository<ServiceInstance, Long> {
    Optional<ServiceInstance> findBySubscriptionId(Long subscriptionId);

    List<ServiceInstance> findByProductId(Long productId);

    List<ServiceInstance> findByOwnerOrganizationIdOrderByProductIdAscIdAsc(Long organizationId);

    List<ServiceInstance> findAllByOrderByProductIdAscIdAsc();
}
