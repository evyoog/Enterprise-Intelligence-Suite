package com.vyoog.eisplatform.modules.integration.repository;

import com.vyoog.eisplatform.modules.integration.model.EventPlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPlatformSettingRepository extends JpaRepository<EventPlatformSetting, Long> {
}
