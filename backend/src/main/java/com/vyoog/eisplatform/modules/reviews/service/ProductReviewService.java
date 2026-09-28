package com.vyoog.eisplatform.modules.reviews.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.reviews.dto.ProductRatingSummaryDto;
import com.vyoog.eisplatform.modules.reviews.dto.ProductReviewDto;
import com.vyoog.eisplatform.modules.reviews.dto.SubmitReviewRequest;
import com.vyoog.eisplatform.modules.reviews.model.ProductReview;
import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;
import com.vyoog.eisplatform.modules.reviews.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 03.04.01 Reviews & Ratings (sprint 2027.1.3) — every review is PENDING
 * until a platform admin ({@code MANAGE_REVIEWS}) approves it; only
 * APPROVED reviews are ever shown publicly or counted in the average.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    /** 03.04.01.01/.02 Submit review / Rate product — upserts the caller's
     * own review for this product, always (re)setting it to PENDING. */
    @Transactional
    public ProductReviewDto submitReview(Long customerId, Long productId, SubmitReviewRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductReview review = reviewRepository.findByProductIdAndCustomerId(productId, customerId)
            .orElseGet(() -> {
                ProductReview created = new ProductReview();
                created.setProductId(productId);
                created.setCustomerId(customerId);
                return created;
            });
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setStatus(ReviewStatus.PENDING);
        review = reviewRepository.save(review);

        auditService.recordSuccess("REVIEW_SUBMITTED", null, customerId, null,
            "ProductReview", review.getId().toString(), null, "Review submitted for product " + productId);
        return toDto(review, product.getName());
    }

    /** 03.04.01.04 View ratings — public, APPROVED reviews only. */
    public ProductRatingSummaryDto getRatings(Long productId) {
        List<ProductReview> approved = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.APPROVED);
        Double average = approved.isEmpty() ? null
            : approved.stream().mapToInt(ProductReview::getRating).average().orElse(0);
        String productName = productRepository.findById(productId).map(Product::getName).orElse("Unknown product");
        List<ProductReviewDto> dtos = approved.stream().map(r -> toDto(r, productName)).toList();
        return new ProductRatingSummaryDto(average, approved.size(), dtos);
    }

    /** The caller's own review for this product, if any (to prefill an edit form). */
    public ProductReviewDto getMyReview(Long customerId, Long productId) {
        ProductReview review = reviewRepository.findByProductIdAndCustomerId(productId, customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        String productName = productRepository.findById(productId).map(Product::getName).orElse("Unknown product");
        return toDto(review, productName);
    }

    /** Admin view — every review, any status. */
    public List<ProductReviewDto> listAll() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream().map(r -> toDto(r, productName(r))).toList();
    }

    /** 03.04.01.03 Moderate review — approve. */
    @Transactional
    public ProductReviewDto approveReview(Long reviewId) {
        return decide(reviewId, ReviewStatus.APPROVED, "REVIEW_APPROVED");
    }

    /** 03.04.01.03 Moderate review — reject. */
    @Transactional
    public ProductReviewDto rejectReview(Long reviewId) {
        return decide(reviewId, ReviewStatus.REJECTED, "REVIEW_REJECTED");
    }

    private ProductReviewDto decide(Long reviewId, ReviewStatus status, String auditAction) {
        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalArgumentException("This review has already been moderated.");
        }
        review.setStatus(status);
        review = reviewRepository.save(review);
        auditService.recordSuccess(auditAction, null, null, null,
            "ProductReview", reviewId.toString(), null, "Review " + status.name().toLowerCase());
        return toDto(review, productName(review));
    }

    private String productName(ProductReview review) {
        return productRepository.findById(review.getProductId()).map(Product::getName).orElse("Unknown product");
    }

    private ProductReviewDto toDto(ProductReview review, String productName) {
        String customerName = customerRepository.findById(review.getCustomerId())
            .map(this::fullName).orElse("Unknown");
        return new ProductReviewDto(
            review.getId(), review.getProductId(), productName,
            review.getCustomerId(), customerName,
            review.getRating(), review.getComment(), review.getStatus(), review.getCreatedAt()
        );
    }

    private String fullName(Customer customer) {
        return customer.getFirstName() + " " + customer.getLastName();
    }
}
