package com.vyoog.eisplatform.modules.reviews.dto;

import java.util.List;

/** 03.04.01.04 View ratings (sprint 2027.1.3) — APPROVED reviews only; see
 * ProductReviewService#getRatings. */
public record ProductRatingSummaryDto(
    Double averageRating,
    Integer reviewCount,
    List<ProductReviewDto> reviews
) {
}
