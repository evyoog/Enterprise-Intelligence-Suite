package com.vyoog.eisplatform.modules.reviews.dto;

/** C94 (REQ-CAT-003.13): a product's average rating (APPROVED reviews only) and review count, for the catalog cards. */
public record ProductRatingBriefDto(Long productId, Double averageRating, Integer reviewCount) {
}
