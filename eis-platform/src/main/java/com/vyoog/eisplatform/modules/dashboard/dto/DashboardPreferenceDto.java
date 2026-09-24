package com.vyoog.eisplatform.modules.dashboard.dto;

import java.util.List;

public record DashboardPreferenceDto(
    List<String> widgetOrder,
    List<String> hiddenWidgets
) {
}
