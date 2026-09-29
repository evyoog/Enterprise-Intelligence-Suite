package com.vyoog.eisplatform.modules.reviews.model;

/** 03.04.01.03 Moderate review (sprint 2027.1.3). A review is never shown
 * publicly (see {@code ProductReviewService#getRatings}) until an admin
 * approves it — submitting or editing a review always (re)starts it at
 * PENDING, even an edit of a previously APPROVED one. */
public enum ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED
}
