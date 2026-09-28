package com.vyoog.eisplatform.modules.reviews.repository;

import com.vyoog.eisplatform.modules.reviews.model.ProductReview;
import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Optional<ProductReview> findByProductIdAndCustomerId(Long productId, Long customerId);

    List<ProductReview> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status);

    List<ProductReview> findAllByOrderByCreatedAtDesc();
}
