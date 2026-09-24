package com.vyoog.eisplatform.modules.dashboard.repository;

import com.vyoog.eisplatform.modules.dashboard.model.DashboardPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardPreferenceRepository extends JpaRepository<DashboardPreference, Long> {
}
