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

    @GetMapping("/products/{productId}/reviews")
    public ProductRatingSummaryDto getRatings(@PathVariable("productId") Long productId) {
        return reviewService.getRatings(productId);
    }
}
