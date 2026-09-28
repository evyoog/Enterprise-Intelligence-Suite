package com.vyoog.eisplatform.modules.reviews.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.reviews.dto.ProductRatingSummaryDto;
import com.vyoog.eisplatform.modules.reviews.dto.ProductReviewDto;
import com.vyoog.eisplatform.modules.reviews.dto.SubmitReviewRequest;
import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 03.04.01 Reviews & Ratings (sprint 2027.1.3). */
@SpringBootTest
@ActiveProfiles("test")
class ProductReviewServiceTest {

    @Autowired
    private ProductReviewService reviewService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Long newProduct() {
        Product product = new Product();
        product.setName("Review Test Product " + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product).getId();
    }

    private Long newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("review-test-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer).getId();
    }

    @Test
    void aSubmittedReviewStartsPendingAndIsInvisibleUntilApproved() {
        Long productId = newProduct();
        Long customerId = newCustomer();

        ProductReviewDto submitted = reviewService.submitReview(customerId, productId, new SubmitReviewRequest(4, "Pretty good."));
        assertThat(submitted.status()).isEqualTo(ReviewStatus.PENDING);

        ProductRatingSummaryDto ratings = reviewService.getRatings(productId);
        assertThat(ratings.reviewCount()).isZero();
        assertThat(ratings.averageRating()).isNull();

        reviewService.approveReview(submitted.id());
        ProductRatingSummaryDto afterApproval = reviewService.getRatings(productId);
        assertThat(afterApproval.reviewCount()).isEqualTo(1);
        assertThat(afterApproval.averageRating()).isEqualTo(4.0);
    }

    @Test
    void submittingAgainEditsTheSameReviewAndResetsItToPending() {
        Long productId = newProduct();
        Long customerId = newCustomer();

        ProductReviewDto first = reviewService.submitReview(customerId, productId, new SubmitReviewRequest(3, "Okay."));
        reviewService.approveReview(first.id());

        ProductReviewDto edited = reviewService.submitReview(customerId, productId, new SubmitReviewRequest(5, "Actually great."));
        assertThat(edited.id()).isEqualTo(first.id());
        assertThat(edited.status()).isEqualTo(ReviewStatus.PENDING);
        assertThat(reviewService.getRatings(productId).reviewCount()).isZero();
    }

    @Test
    void rejectingAReviewKeepsItOutOfRatings() {
        Long productId = newProduct();
        Long customerId = newCustomer();
        ProductReviewDto submitted = reviewService.submitReview(customerId, productId, new SubmitReviewRequest(1, "Not for me."));

        reviewService.rejectReview(submitted.id());
        assertThat(reviewService.getRatings(productId).reviewCount()).isZero();
    }

    @Test
    void moderatingAnAlreadyDecidedReviewIsRefused() {
        Long productId = newProduct();
        Long customerId = newCustomer();
        ProductReviewDto submitted = reviewService.submitReview(customerId, productId, new SubmitReviewRequest(2, null));

        reviewService.approveReview(submitted.id());
        assertThatThrownBy(() -> reviewService.approveReview(submitted.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reviewService.rejectReview(submitted.id())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void averageRatingReflectsOnlyApprovedReviews() {
        Long productId = newProduct();
        ProductReviewDto a = reviewService.submitReview(newCustomer(), productId, new SubmitReviewRequest(5, null));
        ProductReviewDto b = reviewService.submitReview(newCustomer(), productId, new SubmitReviewRequest(1, null));
        reviewService.submitReview(newCustomer(), productId, new SubmitReviewRequest(3, null)); // left PENDING

        reviewService.approveReview(a.id());
        reviewService.approveReview(b.id());

        ProductRatingSummaryDto ratings = reviewService.getRatings(productId);
        assertThat(ratings.reviewCount()).isEqualTo(2);
        assertThat(ratings.averageRating()).isEqualTo(3.0);
    }

    @Test
    void submittingForANonexistentProductIsRefused() {
        Long customerId = newCustomer();
        assertThatThrownBy(() -> reviewService.submitReview(customerId, 999_999_999L, new SubmitReviewRequest(3, null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
