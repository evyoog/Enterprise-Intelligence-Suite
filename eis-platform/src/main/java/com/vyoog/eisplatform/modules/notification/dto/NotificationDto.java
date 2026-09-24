package com.vyoog.eisplatform.modules.notification.dto;

import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;

import java.time.Instant;

public record NotificationDto(
    Long id,
    NotificationCategory category,
    NotificationSeverity severity,
    String title,
    String message,
    boolean read,
    Instant createdAt
) {
}
