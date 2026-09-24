package com.vyoog.eisplatform.modules.auth.dto;

import java.time.Instant;
import java.util.List;

public record SessionDto(String id, String ipAddress, Instant startedAt, Instant lastAccessAt,
                          List<String> clients, boolean current) {
}
