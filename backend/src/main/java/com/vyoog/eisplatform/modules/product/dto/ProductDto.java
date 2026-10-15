package com.vyoog.eisplatform.modules.product.dto;

import com.vyoog.eisplatform.modules.platform.dto.PlatformSummaryDto;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    /** C95: the wide banner image of the catalog card; {@code imageUrl} is the app icon. */
    private String bannerImageUrl;
    private String launchUrl;
    private String category;
    private ProductStatus status;
    private boolean ssoConnected;
    private boolean featured;
    private List<PlatformSummaryDto> platforms;
    private List<ProductPlanDto> plans;
    private Integer version;
    private Long parentProductId;
    private String variantLabel;
    private List<Long> dependsOnProductIds;
    // C66 showcase fields.
    private String accentColor;
    private List<String> featureTags;
    private String documentationUrl;
    private String supportUrl;
    /** C102: free trial length in days; null = no trial offered. */
    private Integer trialDays;
    /** C94: when the product was added, for the catalog's New badge and Newest sort. */
    private java.time.Instant createdAt;
}
