package com.vyoog.eisplatform.modules.notification.repository;

import com.vyoog.eisplatform.modules.notification.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
}
