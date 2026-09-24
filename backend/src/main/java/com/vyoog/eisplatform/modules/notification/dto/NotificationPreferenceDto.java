package com.vyoog.eisplatform.modules.notification.dto;

import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;

import java.util.List;

public record NotificationPreferenceDto(List<NotificationCategory> emailDisabledCategories) {
}
