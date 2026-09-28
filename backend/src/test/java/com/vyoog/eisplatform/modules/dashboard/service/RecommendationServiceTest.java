package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.dashboard.dto.RecommendationsDto;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** 03.01.02 Recommendations (sprint 2027.1.2) — rule-based, no AI. */
@SpringBootTest
@ActiveProfiles("test")
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Product newProduct(boolean featured) {
        Product product = new Product();
        product.setName("Recommend Test " + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        product.setFeatured(featured);
        return productRepository.save(product);
    }

    private Long newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("rec-test-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer).getId();
    }

    @Test
    void featuredProductsAreReturned() {
        Product featured = newProduct(true);
        newProduct(false);

        RecommendationsDto recommendations = recommendationService.getRecommendations();
        assertThat(recommendations.featured()).anyMatch(p -> p.id().equals(featured.getId()));
    }

    @Test
    void popularProductsAreOrderedByTotalLaunchesAcrossCustomers() {
        Product popular = newProduct(false);
        Product lessPopular = newProduct(false);
        Long customerA = newCustomer();
        Long customerB = newCustomer();

        for (int i = 0; i < 5; i++) dashboardService.recordLaunch(customerA, popular.getId());
        dashboardService.recordLaunch(customerB, popular.getId());
        dashboardService.recordLaunch(customerA, lessPopular.getId());

        RecommendationsDto recommendations = recommendationService.getRecommendations();
        int popularIndex = indexOf(recommendations, popular.getId());
        int lessPopularIndex = indexOf(recommendations, lessPopular.getId());
        assertThat(popularIndex).isGreaterThanOrEqualTo(0);
        assertThat(popularIndex).isLessThan(lessPopularIndex);
    }

    @Test
    void anInactiveProductNeverAppearsInThePopularList() {
        Product inactive = newProduct(false);
        Long customerId = newCustomer();
        dashboardService.recordLaunch(customerId, inactive.getId());
        inactive.setStatus(ProductStatus.INACTIVE);
        productRepository.save(inactive);

        RecommendationsDto recommendations = recommendationService.getRecommendations();
        assertThat(recommendations.popular()).noneMatch(p -> p.id().equals(inactive.getId()));
    }

    private int indexOf(RecommendationsDto recommendations, Long productId) {
        for (int i = 0; i < recommendations.popular().size(); i++) {
            if (recommendations.popular().get(i).id().equals(productId)) return i;
        }
        return -1;
    }
}
