package com.vyoog.eisplatform.modules.dashboard.controller;

import com.vyoog.eisplatform.modules.dashboard.dto.RecommendationsDto;
import com.vyoog.eisplatform.modules.dashboard.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public storefront reads — permitAll in SecurityConfig, same as the rest
 * of the product catalog under /products/**. */
@RestController
@RequestMapping("/products/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public RecommendationsDto get() {
        return recommendationService.getRecommendations();
    }
}
