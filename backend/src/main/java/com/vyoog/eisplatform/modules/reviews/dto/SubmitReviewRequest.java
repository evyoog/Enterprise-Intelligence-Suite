package com.vyoog.eisplatform.modules.reviews.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 03.04.01.01/.02 Submit review / Rate product (sprint 2027.1.3) — one
 * action, folded (see ProductReview's own javadoc). */
public record SubmitReviewRequest(@NotNull @Min(1) @Max(5) Integer rating, String comment) {
}
