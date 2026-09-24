package com.vyoog.eisplatform.modules.dashboard.repository;

import com.vyoog.eisplatform.modules.dashboard.model.ProductUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductUsageRepository extends JpaRepository<ProductUsage, Long> {

    List<ProductUsage> findByCustomerId(Long customerId);

    Optional<ProductUsage> findByCustomerIdAndProductId(Long customerId, Long productId);

    boolean existsByProductId(Long productId);

    /** Phase 19: one query for every member's usage across the whole
     * organization, instead of one findByCustomerId call per member — see
     * BusinessDashboardService. */
    List<ProductUsage> findByCustomerIdIn(Collection<Long> customerIds);
}
