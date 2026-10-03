package com.vyoog.eisplatform.modules.product.mapper;

import com.vyoog.eisplatform.modules.platform.mapper.PlatformMapper;
import com.vyoog.eisplatform.modules.product.dto.ProductCreateRequest;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.dto.ProductPlanCreateRequest;
import com.vyoog.eisplatform.modules.product.dto.ProductPlanDto;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import org.mapstruct.Mapper;

import java.util.List;
import org.mapstruct.Mapping;

// uses = PlatformMapper.class: pulls PlatformMapper#toSummary into scope so
// toDto can auto-map Product.platforms (Set<Platform>) into
// ProductDto.platforms (List<PlatformSummaryDto>) element-by-element.
@Mapper(uses = PlatformMapper.class)
public interface ProductMapper {

    // dependsOnProductIds: same reason as platforms below — Product.dependsOn
    // (Set<Product>) needs a manual (Set<Product> -> List<Long> of id) walk,
    // simplest done by hand alongside the mapper call in ProductService.
    @Mapping(target = "dependsOnProductIds", ignore = true)
    @Mapping(target = "featureTags", ignore = true)
    ProductDto toDto(Product product);

    // request.platformIds()/dependsOnProductIds() (List<Long>) have no
    // automatic path to Product.platforms/dependsOn (Set<...>) — those need a
    // repository lookup, done manually in ProductService — so both fields are
    // left for it to set.
    @Mapping(target = "platforms", ignore = true)
    @Mapping(target = "dependsOn", ignore = true)
    @Mapping(target = "featureTags", ignore = true)
    Product toEntity(ProductCreateRequest request);

    // Declaring these lets MapStruct auto-generate the List<ProductPlan> <->
    // List<ProductPlanDto>/List<ProductPlanCreateRequest> mapping used by the
    // methods above — it maps element-by-element once it has one of these.
    ProductPlanDto toDto(ProductPlan plan);

    /** C66: Product.featureTags is stored comma-separated; the DTO carries a list. */
    @org.mapstruct.AfterMapping
    default void fillFeatureTags(Product product, @org.mapstruct.MappingTarget ProductDto dto) {
        dto.setFeatureTags(featureTagList(product.getFeatureTags()));
    }

    static List<String> featureTagList(String featureTags) {
        if (featureTags == null || featureTags.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(featureTags.split(",")).map(String::trim).filter(t -> !t.isEmpty()).toList();
    }

    ProductPlan toEntity(ProductPlanCreateRequest request);
}
