package com.vyoog.eisplatform.modules.administration.repository;

import com.vyoog.eisplatform.modules.administration.model.PlatformFeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformFeatureFlagRepository extends JpaRepository<PlatformFeatureFlag, String> {
}
