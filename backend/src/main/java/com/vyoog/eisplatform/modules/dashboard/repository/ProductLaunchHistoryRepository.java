package com.vyoog.eisplatform.modules.dashboard.repository;

import com.vyoog.eisplatform.modules.dashboard.model.ProductLaunchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ProductLaunchHistoryRepository extends JpaRepository<ProductLaunchHistory, Long> {

    List<ProductLaunchHistory> findByProductIdOrderByLaunchedAtDesc(Long productId);

    /** C104 / AN-1: rows older than the 13-month retention window. */
    @Modifying
    @Query("DELETE FROM ProductLaunchHistory h WHERE h.launchedAt < :cutoff")
    int deleteByLaunchedAtBefore(Instant cutoff);
}
