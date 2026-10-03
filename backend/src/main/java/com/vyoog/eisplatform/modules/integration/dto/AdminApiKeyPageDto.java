package com.vyoog.eisplatform.modules.integration.dto;

import java.util.List;

public record AdminApiKeyPageDto(List<AdminApiKeyDto> items, long totalElements, int page, int size) {
}
