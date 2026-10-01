package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingSettingsRepository extends JpaRepository<BillingSettings, Long> {
    Optional<BillingSettings> findFirstByOrderByIdAsc();
}
