package com.vyoog.eisplatform.modules.integration.dto;

import java.util.List;

public record UpdateEventPlatformSettingsRequest(
    Integer retentionDays,
    List<String> alertRecipients,
    Boolean alertInApp,
    Boolean alertEmail
) {
}
