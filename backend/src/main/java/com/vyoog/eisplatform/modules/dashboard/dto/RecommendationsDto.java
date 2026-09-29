package com.vyoog.eisplatform.modules.dashboard.dto;

import java.util.List;

public record RecommendationsDto(
    List<RecommendedProductDto> featured,
    List<RecommendedProductDto> popular
) {
}
