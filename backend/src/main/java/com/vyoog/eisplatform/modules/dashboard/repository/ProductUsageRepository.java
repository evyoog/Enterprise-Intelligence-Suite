package com.vyoog.eisplatform.modules.dashboard.repository;

import com.vyoog.eisplatform.modules.dashboard.model.ProductUsage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    /** 03.01.02 Show popular products (sprint 2027.1.2): every customer's
     * launches summed per product, most-launched first — the same honest
     * usage metric {@link ProductUsage}'s own javadoc describes, just
     * aggregated across every customer instead of one. */
    @Query("SELECT u.productId FROM ProductUsage u GROUP BY u.productId ORDER BY SUM(u.launchCount) DESC")
    List<Long> topProductIdsByTotalLaunches(Pageable pageable);
}
