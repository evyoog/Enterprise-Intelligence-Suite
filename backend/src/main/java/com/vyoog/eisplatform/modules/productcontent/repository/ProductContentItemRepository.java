package com.vyoog.eisplatform.modules.productcontent.repository;

import com.vyoog.eisplatform.modules.productcontent.model.ProductContentItem;
import com.vyoog.eisplatform.modules.productcontent.model.ProductContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductContentItemRepository extends JpaRepository<ProductContentItem, Long> {

    List<ProductContentItem> findByProductIdOrderByDisplayOrderAscIdAsc(Long productId);

    List<ProductContentItem> findByProductIdAndStatusOrderByDisplayOrderAscIdAsc(Long productId, ProductContentStatus status);

    boolean existsByObjectKeyOrLogoObjectKey(String objectKey, String logoObjectKey);
}
