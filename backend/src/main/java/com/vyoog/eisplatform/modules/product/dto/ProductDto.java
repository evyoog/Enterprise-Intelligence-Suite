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
}
