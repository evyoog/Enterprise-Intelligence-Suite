package com.vyoog.eisplatform.modules.reviews.controller;

import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.reviews.dto.ProductReviewDto;
import com.vyoog.eisplatform.modules.reviews.dto.SubmitReviewRequest;
import com.vyoog.eisplatform.modules.reviews.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** 03.04.01.01/.02 Submit review / Rate product — the caller's own review.
 * Covered by the existing "/me/**" authenticated() rule in SecurityConfig. */
@RestController
@RequestMapping("/me/products/{productId}/review")
@RequiredArgsConstructor
public class MyProductReviewController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final ProductReviewService reviewService;

    @PutMapping
    public ProductReviewDto submit(@AuthenticationPrincipal Jwt jwt, @PathVariable("productId") Long productId,
                                    @Valid @RequestBody SubmitReviewRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return reviewService.submitReview(customer.getId(), productId, request);
    }

    @GetMapping
    public ProductReviewDto getMine(@AuthenticationPrincipal Jwt jwt, @PathVariable("productId") Long productId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return reviewService.getMyReview(customer.getId(), productId);
    }
}
