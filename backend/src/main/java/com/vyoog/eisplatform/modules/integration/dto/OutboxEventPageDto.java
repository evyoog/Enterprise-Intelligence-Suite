package com.vyoog.eisplatform.modules.integration.dto;

import java.util.List;

public record OutboxEventPageDto(
    List<OutboxEventSummaryDto> items,
    long totalElements,
    int page,
    int size
) {
}
