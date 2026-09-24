package com.vyoog.eisplatform.modules.notification.dto;

import java.util.List;

public record NotificationSummaryDto(
    long unreadCount,
    List<NotificationDto> notifications
) {
}
