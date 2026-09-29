package com.vyoog.eisplatform.modules.reviews.controller;

import com.vyoog.eisplatform.modules.reviews.dto.ProductReviewDto;
import com.vyoog.eisplatform.modules.reviews.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 03.04.01.03 Moderate review — MANAGE_REVIEWS-gated in SecurityConfig. */
@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping
    public List<ProductReviewDto> listAll() {
        return reviewService.listAll();
    }

    @PostMapping("/{id}/approve")
    public ProductReviewDto approve(@PathVariable("id") Long id) {
        return reviewService.approveReview(id);
    }

    @PostMapping("/{id}/reject")
    public ProductReviewDto reject(@PathVariable("id") Long id) {
        return reviewService.rejectReview(id);
    }
}
