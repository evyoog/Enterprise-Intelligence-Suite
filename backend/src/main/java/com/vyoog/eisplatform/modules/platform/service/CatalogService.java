package com.vyoog.eisplatform.modules.platform.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.platform.dto.CatalogPlatformDto;
import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.platform.repository.PlatformRepository;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * C66: the public Product Catalog's platform view. A platform is listed when
 * it is ACTIVE and "show in catalog" is on, ordered by display order then
 * name. Its app count, categories and feature tags are computed from its
 * ACTIVE apps (the same set the public app listing returns), so the catalog
 * always reflects the current configuration — nothing is stored twice.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    /** Feature tags shown on a platform card (the detail view shows all). */
    static final int CARD_FEATURE_TAGS = 6;

    private final PlatformRepository platformRepository;
    private final ProductService productService;

    public List<CatalogPlatformDto> listPlatforms() {
        List<ProductDto> activeApps = productService.listProducts();
        return platformRepository.findAll().stream()
            .filter(CatalogService::visible)
            .sorted(Comparator.comparingInt(Platform::getDisplayOrder)
                .thenComparing(p -> p.getName().toLowerCase(Locale.ROOT)))
            .map(p -> toDto(p, appsOf(p, activeApps), false))
            .toList();
    }

    public CatalogPlatformDto getPlatform(Long id) {
        Platform platform = platformRepository.findById(id)
            .filter(CatalogService::visible)
            .orElseThrow(() -> new ResourceNotFoundException("Platform not found: " + id));
        return toDto(platform, appsOf(platform, productService.listProducts()), true);
    }

    private static boolean visible(Platform platform) {
        return "ACTIVE".equals(platform.getStatus()) && platform.isShowInCatalog();
    }

    private static List<ProductDto> appsOf(Platform platform, List<ProductDto> activeApps) {
        return activeApps.stream()
            .filter(app -> app.getPlatforms() != null && app.getPlatforms().stream().anyMatch(s -> s.getId().equals(platform.getId())))
            .sorted(Comparator.comparing(app -> app.getName().toLowerCase(Locale.ROOT)))
            .toList();
    }

    private static CatalogPlatformDto toDto(Platform platform, List<ProductDto> apps, boolean detail) {
        LinkedHashSet<String> categories = new LinkedHashSet<>();
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        for (ProductDto app : apps) {
            if (app.getCategory() != null && !app.getCategory().isBlank()) {
                categories.add(app.getCategory().trim());
            }
            if (app.getFeatureTags() != null) {
                tags.addAll(app.getFeatureTags());
            }
        }
        List<String> featureTags = detail ? List.copyOf(tags) : tags.stream().limit(CARD_FEATURE_TAGS).toList();
        return new CatalogPlatformDto(platform.getId(), platform.getName(), platform.getDescription(), platform.getImageUrl(),
            platform.getPrimaryColor(), apps.size(), List.copyOf(categories), featureTags, detail ? apps : null);
    }
}
