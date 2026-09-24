package com.vyoog.eisplatform.modules.platform.repository;

import com.vyoog.eisplatform.modules.platform.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
}
