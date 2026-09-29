package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.OrderStatus;

import java.time.Instant;

public record OrderDto(
    Long id,
    Long productId,
    String productName,
    Long planId,
    String planName,
    OrderStatus status,
    Long requestedByCustomerId,
    String requestedByName,
    Long decidedByCustomerId,
    String decidedByName,
    Instant decidedAt,
    String decisionNote,
    Instant createdAt
) {
}
