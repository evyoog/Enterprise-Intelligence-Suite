package com.vyoog.eisplatform.modules.wishlist.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.reviews.dto.ProductRatingBriefDto;
import com.vyoog.eisplatform.modules.reviews.model.ProductReview;
import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;
import com.vyoog.eisplatform.modules.reviews.repository.ProductReviewRepository;
import com.vyoog.eisplatform.modules.reviews.service.ProductReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-MKT-004 and REQ-CAT-003.13 (C94): the wishlist, the catalog ratings summary and the product's added date. TC-MKT-031..035. */
@SpringBootTest
@ActiveProfiles("test")
class WishlistServiceTest {

    @Autowired private WishlistService wishlistService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductReviewRepository reviewRepository;
    @Autowired private ProductReviewService reviewService;
    @Autowired private ProductService productService;

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("wish-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Wish");
        customer.setLastName("List");
        return customerRepository.save(customer);
    }

    private Product newProduct(ProductStatus status) {
        Product product = new Product();
        product.setName("Wish product " + System.nanoTime());
        product.setPrice(BigDecimal.ZERO);
        product.setStatus(status);
        return productRepository.save(product);
    }

    private void review(Product product, Customer customer, int rating, ReviewStatus status) {
        ProductReview review = new ProductReview();
        review.setProductId(product.getId());
        review.setCustomerId(customer.getId());
        review.setRating(rating);
        review.setStatus(status);
        reviewRepository.save(review);
    }

    // TC-MKT-031
    @Test
    void savingAProductIsIdempotentAndTheListIsNewestFirst() {
        Customer customer = newCustomer();
        Product first = newProduct(ProductStatus.ACTIVE);
        Product second = newProduct(ProductStatus.ACTIVE);

        assertThat(wishlistService.list(customer.getId())).isEmpty();
        wishlistService.add(customer.getId(), first.getId());
        wishlistService.add(customer.getId(), first.getId());
        List<Long> after = wishlistService.add(customer.getId(), second.getId());

        assertThat(after).containsExactly(second.getId(), first.getId());
    }

    // TC-MKT-032
    @Test
    void removingAProductLeavesTheOthersAndRemovingAnUnsavedOneIsHarmless() {
        Customer customer = newCustomer();
        Product a = newProduct(ProductStatus.ACTIVE);
        Product b = newProduct(ProductStatus.ACTIVE);
        wishlistService.add(customer.getId(), a.getId());
        wishlistService.add(customer.getId(), b.getId());

        assertThat(wishlistService.remove(customer.getId(), a.getId())).containsExactly(b.getId());
        assertThat(wishlistService.remove(customer.getId(), a.getId())).containsExactly(b.getId());
    }

    // TC-MKT-033
    @Test
    void aCustomerOnlySeesTheirOwnListAndOnlyActiveProductsCanBeSaved() {
        Customer mine = newCustomer();
        Customer other = newCustomer();
        Product active = newProduct(ProductStatus.ACTIVE);
        Product retired = newProduct(ProductStatus.RETIRED);

        wishlistService.add(mine.getId(), active.getId());

        assertThat(wishlistService.list(other.getId())).isEmpty();
        assertThatThrownBy(() -> wishlistService.add(mine.getId(), retired.getId())).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> wishlistService.add(mine.getId(), -1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // TC-MKT-034
    @Test
    void theCatalogRatingsCountApprovedReviewsOnlyAndLeaveOutUnreviewedProducts() {
        Product rated = newProduct(ProductStatus.ACTIVE);
        Product unreviewed = newProduct(ProductStatus.ACTIVE);
        Product onlyPending = newProduct(ProductStatus.ACTIVE);
        review(rated, newCustomer(), 5, ReviewStatus.APPROVED);
        review(rated, newCustomer(), 4, ReviewStatus.APPROVED);
        review(rated, newCustomer(), 1, ReviewStatus.PENDING);
        review(onlyPending, newCustomer(), 3, ReviewStatus.PENDING);

        List<ProductRatingBriefDto> ratings = reviewService.listRatings();

        ProductRatingBriefDto brief = ratings.stream().filter(r -> r.productId().equals(rated.getId())).findFirst().orElseThrow();
        assertThat(brief.averageRating()).isEqualTo(4.5);
        assertThat(brief.reviewCount()).isEqualTo(2);
        assertThat(ratings).noneMatch(r -> r.productId().equals(unreviewed.getId()) || r.productId().equals(onlyPending.getId()));
    }

    // TC-MKT-035
    @Test
    void theProductListCarriesWhenEachProductWasAdded() {
        Product product = newProduct(ProductStatus.ACTIVE);

        ProductDto dto = productService.getProduct(product.getId());

        assertThat(dto.getCreatedAt()).isNotNull();
    }
}
