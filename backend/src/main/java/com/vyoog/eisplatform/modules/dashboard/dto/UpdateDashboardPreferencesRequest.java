package com.vyoog.eisplatform.modules.dashboard.dto;

import java.util.List;

public record UpdateDashboardPreferencesRequest(
    List<String> widgetOrder,
    List<String> hiddenWidgets
) {
}
