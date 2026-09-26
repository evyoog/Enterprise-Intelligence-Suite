package com.vyoog.eisplatform.modules.servicestatus.dto;

import java.util.List;

/** {@code enabled}: the {@code app.status-page.enabled} setting (C26). When off,
 * both lists are empty. {@code incidents}: only for purchased products. */
public record ServiceStatusPageDto(
    boolean enabled,
    List<ProductStatusDto> products,
    List<IncidentDto> incidents
) {
}
