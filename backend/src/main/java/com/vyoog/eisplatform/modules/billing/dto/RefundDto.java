package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

public record RefundDto(Long id, long amount, String reason, String status, Instant createdAt) {
}
