package com.vyoog.eisplatform.modules.dashboard.repository;

import com.vyoog.eisplatform.modules.dashboard.model.FavoriteProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, Long> {

    List<FavoriteProduct> findByCustomerId(Long customerId);

    Optional<FavoriteProduct> findByCustomerIdAndProductId(Long customerId, Long productId);

    boolean existsByProductId(Long productId);
}
