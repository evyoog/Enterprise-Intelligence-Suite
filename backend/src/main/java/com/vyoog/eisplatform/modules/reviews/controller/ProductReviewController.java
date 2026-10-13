package com.vyoog.eisplatform.modules.reviews.controller;

import com.vyoog.eisplatform.modules.reviews.dto.ProductRatingSummaryDto;
import com.vyoog.eisplatform.modules.reviews.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** 03.04.01.04 View ratings — public, same reasoning as the product catalog. */
@RestController
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    /** C94: ratings of all products in one call, for the catalog cards (public, like the product list). */
    @GetMapping("/products/ratings")
    public java.util.List<com.vyoog.eisplatform.modules.reviews.dto.ProductRatingBriefDto> listRatings() {
        return reviewService.listRatings();
    }

    @GetMapping("/products/{productId}/reviews")
    public ProductRatingSummaryDto getRatings(@PathVariable("productId") Long productId) {
        return reviewService.getRatings(productId);
    }
}
