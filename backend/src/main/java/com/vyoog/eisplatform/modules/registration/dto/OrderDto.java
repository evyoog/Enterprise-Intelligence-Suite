package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.OrderStatus;

import java.time.Instant;
import java.util.List;

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
    Instant createdAt,
    /** C100 CA-1 B: every product of the order (the fields above are the first). */
    List<Item> items,
    /** C100 CA-2: why the member asked for the purchase. */
    String requestReason
) {
    public record Item(Long productId, String productName, Long planId, String planName) {
    }
}
