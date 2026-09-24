package com.vyoog.eisplatform.modules.audit.dto;

import java.util.List;

public record AuditLogPageDto(
    List<AuditLogDto> items,
    long totalElements,
    int page,
    int size
) {
}
