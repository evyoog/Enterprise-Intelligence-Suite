package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.dashboard.dto.RecommendationsDto;
import com.vyoog.eisplatform.modules.dashboard.dto.RecommendedProductDto;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 03.01.02 Recommendations (sprint 2027.1.2). Rule-based, not AI ([C40]):
 * "featured" is a plain admin-set flag ({@link Product#isFeatured}); "popular"
 * is read straight off the existing launch-count usage data ({@code
 * ProductUsage}, Phase 16) — no separate recommendation model or AI service
 * is invented, since neither is needed to satisfy these three functions.
 * "Recommend products" (03.01.02.01) is these two lists together — there is
 * no third, personalized list; nothing in this codebase has a basis for one
 * yet (no purchase-history/collaborative-filtering data exists).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int POPULAR_LIMIT = 8;

    private final ProductRepository productRepository;
    private final ProductUsageRepository productUsageRepository;

    public RecommendationsDto getRecommendations() {
        List<RecommendedProductDto> featured = productRepository.findByStatusAndFeaturedTrue(ProductStatus.ACTIVE).stream()
            .map(this::toDto)
            .toList();

        List<Long> popularIds = productUsageRepository.topProductIdsByTotalLaunches(PageRequest.of(0, POPULAR_LIMIT));
        Map<Long, Product> byId = new LinkedHashMap<>();
        productRepository.findAllById(popularIds).forEach(p -> byId.put(p.getId(), p));
        List<RecommendedProductDto> popular = popularIds.stream()
            .map(byId::get)
            .filter(p -> p != null && p.getStatus() == ProductStatus.ACTIVE)
            .map(this::toDto)
            .toList();

        return new RecommendationsDto(featured, popular);
    }

    private RecommendedProductDto toDto(Product product) {
        return new RecommendedProductDto(product.getId(), product.getName(), product.getCategory(), product.getImageUrl(), product.getLaunchUrl());
    }
}
