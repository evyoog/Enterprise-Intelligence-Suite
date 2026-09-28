package com.vyoog.eisplatform.modules.reviews.dto;

import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;

import java.time.Instant;

public record ProductReviewDto(
    Long id,
    Long productId,
    String productName,
    Long customerId,
    String customerName,
    Integer rating,
    String comment,
    ReviewStatus status,
    Instant createdAt
) {
}
