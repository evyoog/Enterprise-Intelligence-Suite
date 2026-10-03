package com.vyoog.eisplatform.modules.integration.dto;

import java.time.Instant;

public record EventReceiptDto(String handler, Instant processedAt) {
}
