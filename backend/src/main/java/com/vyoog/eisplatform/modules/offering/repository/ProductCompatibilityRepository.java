package com.vyoog.eisplatform.modules.offering.repository;

import com.vyoog.eisplatform.modules.offering.model.ProductCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCompatibilityRepository extends JpaRepository<ProductCompatibility, Long> {

    List<ProductCompatibility> findByProductId(Long productId);

    void deleteByProductId(Long productId);

    void deleteByWorksWithProductId(Long worksWithProductId);
}
