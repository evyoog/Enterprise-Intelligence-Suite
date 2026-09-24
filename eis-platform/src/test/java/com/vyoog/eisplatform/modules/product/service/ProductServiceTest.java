package com.vyoog.eisplatform.modules.product.service;

import com.vyoog.eisplatform.common.exception.ProductInUseException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.platform.dto.PlatformCreateRequest;
import com.vyoog.eisplatform.modules.platform.dto.PlatformDto;
import com.vyoog.eisplatform.modules.platform.service.PlatformService;
import com.vyoog.eisplatform.modules.product.dto.ProductCreateRequest;
import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.dto.ProductPlanCreateRequest;
import com.vyoog.eisplatform.modules.product.dto.ProductSearchResponse;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductSubscriptionRepository productSubscriptionRepository;

    @Autowired
    private PlatformService platformService;

    // Regression test for a real NPE: MapStruct maps a null request.plans() to
    // product.plans = null (not the entity's default empty list), and
    // createProduct() used to call product.getPlans().forEach(...) unconditionally
    // — a product created with no pricing tiers (the common case) threw a 500 on
    // every single request until this was fixed.
    @Test
    void createProductWithNoPlansDoesNotThrow() {
        ProductCreateRequest request = new ProductCreateRequest(
            "No-tier product", null, new BigDecimal("9.99"), null, null, null, null, null, null, null);

        ProductDto created = productService.createProduct(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPlans()).isEmpty();
    }

    @Test
    void createProductWithPlansSavesEachOne() {
        ProductCreateRequest request = new ProductCreateRequest(
            "Tiered product", null, new BigDecimal("0"), null, null, null, null, null, null,
            List.of(new ProductPlanCreateRequest("Basic", new BigDecimal("10.00"), BillingPeriod.MONTHLY, 1)));

        ProductDto created = productService.createProduct(request);

        assertThat(created.getPlans()).hasSize(1);
        assertThat(created.getPlans().get(0).getName()).isEqualTo("Basic");
    }

    @Test
    void updateProductChangesScalarFields() {
        ProductDto created = productService.createProduct(new ProductCreateRequest(
            "Original name", "Original description", new BigDecimal("9.99"), null, null, null, null, null, null, null));

        ProductDto updated = productService.updateProduct(created.getId(), new ProductCreateRequest(
            "Updated name", "Updated description", new BigDecimal("19.99"), null,
            "https://example.com/launch", "Updated category", ProductStatus.INACTIVE, null, null, null));

        assertThat(updated.getName()).isEqualTo("Updated name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getPrice()).isEqualByComparingTo("19.99");
        assertThat(updated.getLaunchUrl()).isEqualTo("https://example.com/launch");
        assertThat(updated.getCategory()).isEqualTo("Updated category");
        assertThat(updated.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    // Regression-shaped test for the same class of bug as createProductWithPlansSavesEachOne:
    // updateProduct() replaces the whole plans collection, so an update that drops the
    // pricing tiers entirely (plans = null) must not throw and must actually clear them —
    // not leave the old tiers dangling because null wasn't handled.
    @Test
    void updateProductReplacesPlansEntirely() {
        ProductDto created = productService.createProduct(new ProductCreateRequest(
            "Tiered product", null, new BigDecimal("0"), null, null, null, null, null, null,
            List.of(new ProductPlanCreateRequest("Basic", new BigDecimal("10.00"), BillingPeriod.MONTHLY, 1))));

        ProductDto updated = productService.updateProduct(created.getId(), new ProductCreateRequest(
            "Tiered product", null, new BigDecimal("0"), null, null, null, null, null, null,
            List.of(new ProductPlanCreateRequest("Pro", new BigDecimal("25.00"), BillingPeriod.YEARLY, 1))));

        assertThat(updated.getPlans()).hasSize(1);
        assertThat(updated.getPlans().get(0).getName()).isEqualTo("Pro");

        ProductDto clearedOfPlans = productService.updateProduct(created.getId(), new ProductCreateRequest(
            "Tiered product", null, new BigDecimal("5.00"), null, null, null, null, null, null, null));

        assertThat(clearedOfPlans.getPlans()).isEmpty();
    }

    @Test
    void updateProductThrowsWhenNotFound() {
        assertThatThrownBy(() -> productService.updateProduct(-1L, new ProductCreateRequest(
            "Doesn't matter", null, new BigDecimal("1.00"), null, null, null, null, null, null, null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProductRemovesItFromTheListing() {
        ProductDto created = productService.createProduct(new ProductCreateRequest(
            "To delete", null, new BigDecimal("1.00"), null, null, null, null, null, null, null));

        productService.deleteProduct(created.getId());

        assertThatThrownBy(() -> productService.getProduct(created.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProductThrowsWhenNotFound() {
        assertThatThrownBy(() -> productService.deleteProduct(-1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // Regression-shaped: a product with real subscription history must never
    // be silently deletable — that would orphan a paying customer's own
    // subscription row. The fix translates the resulting FK violation into a
    // clean, actionable error rather than either a raw 500 or a silent no-op.
    @Test
    void deleteProductWithAnExistingSubscriptionIsBlocked() {
        ProductDto created = productService.createProduct(new ProductCreateRequest(
            "Subscribed product", null, new BigDecimal("1.00"), null, null, null, null, null, null, null));

        Customer customer = new Customer();
        customer.setEmail("subscriber-" + created.getId() + "@example.com");
        customer.setFirstName("Sub");
        customer.setLastName("Scriber");
        customer.setStatus(RegistrationStatus.COMPLETED);
        customer = customerRepository.save(customer);

        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(created.getId());
        subscription.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        subscription.setOwnerCustomerId(customer.getId());
        productSubscriptionRepository.save(subscription);

        assertThatThrownBy(() -> productService.deleteProduct(created.getId()))
            .isInstanceOf(ProductInUseException.class);

        // The product must still be there — the failed delete must not have
        // partially applied.
        assertThat(productService.getProduct(created.getId())).isNotNull();
    }

    // Search tests use a per-test unique tag (nanoTime-based) in the name/
    // category, since this test class shares one H2 instance across all its
    // methods with no rollback between them — assertions check for THIS
    // test's own tagged rows among the results, never an exact total count.

    @Test
    void searchProductsMatchesNameDescriptionOrCategory() {
        String tag = "searchtag" + System.nanoTime();
        productService.createProduct(new ProductCreateRequest(
            "Product " + tag, "A totally unrelated description", new BigDecimal("1.00"),
            null, null, null, null, null, null, null));

        ProductSearchResponse response = productService.searchProducts(tag, null, null, null, null, false);

        assertThat(response.items()).extracting(ProductDto::getName).contains("Product " + tag);
    }

    @Test
    void searchProductsFiltersByCategory() {
        String tag = "cattag" + System.nanoTime();
        productService.createProduct(new ProductCreateRequest(
            "In category " + tag, null, new BigDecimal("1.00"), null, null, tag, null, null, null, null));
        productService.createProduct(new ProductCreateRequest(
            "Not in category " + tag, null, new BigDecimal("1.00"), null, null, "other-" + tag, null, null, null, null));

        ProductSearchResponse response = productService.searchProducts(null, tag, null, null, null, false);

        assertThat(response.items()).extracting(ProductDto::getName)
            .contains("In category " + tag)
            .doesNotContain("Not in category " + tag);
    }

    @Test
    void searchProductsFiltersByPlatform() {
        String tag = "platformtag" + System.nanoTime();
        PlatformDto platform = platformService.createPlatform(new PlatformCreateRequest("Platform " + tag, null, null));
        ProductDto onPlatform = productService.createProduct(new ProductCreateRequest(
            "On platform " + tag, null, new BigDecimal("1.00"), null, null, null, null, null, List.of(platform.getId()), null));
        productService.createProduct(new ProductCreateRequest(
            "Off platform " + tag, null, new BigDecimal("1.00"), null, null, null, null, null, null, null));

        ProductSearchResponse response = productService.searchProducts(tag, null, platform.getId(), null, null, false);

        assertThat(response.items()).extracting(ProductDto::getId).containsExactly(onPlatform.getId());
    }

    @Test
    void searchProductsExcludesInactiveUnlessIncludeInactive() {
        String tag = "inactivetag" + System.nanoTime();
        productService.createProduct(new ProductCreateRequest(
            "Inactive " + tag, null, new BigDecimal("1.00"), null, null, null, ProductStatus.INACTIVE, null, null, null));

        ProductSearchResponse publicResponse = productService.searchProducts(tag, null, null, null, null, false);
        ProductSearchResponse adminResponse = productService.searchProducts(tag, null, null, null, null, true);

        assertThat(publicResponse.items()).isEmpty();
        assertThat(adminResponse.items()).extracting(ProductDto::getName).contains("Inactive " + tag);
    }

    @Test
    void searchProductsSortsByPriceWithinTaggedResults() {
        String tag = "sorttag" + System.nanoTime();
        productService.createProduct(new ProductCreateRequest(
            "Cheap " + tag, null, new BigDecimal("5.00"), null, null, tag, null, null, null, null));
        productService.createProduct(new ProductCreateRequest(
            "Pricey " + tag, null, new BigDecimal("50.00"), null, null, tag, null, null, null, null));

        ProductSearchResponse ascending = productService.searchProducts(null, tag, null, "price", "asc", false);
        ProductSearchResponse descending = productService.searchProducts(null, tag, null, "price", "desc", false);

        assertThat(ascending.items()).extracting(ProductDto::getName)
            .containsExactly("Cheap " + tag, "Pricey " + tag);
        assertThat(descending.items()).extracting(ProductDto::getName)
            .containsExactly("Pricey " + tag, "Cheap " + tag);
    }

    @Test
    void searchProductsFacetsIncludeThisTestsOwnCategory() {
        String tag = "facettag" + System.nanoTime();
        productService.createProduct(new ProductCreateRequest(
            "Faceted " + tag, null, new BigDecimal("1.00"), null, null, tag, null, null, null, null));

        ProductSearchResponse response = productService.searchProducts(null, null, null, null, null, false);

        assertThat(response.facets().categories())
            .anySatisfy(facet -> {
                assertThat(facet.category()).isEqualTo(tag);
                assertThat(facet.count()).isEqualTo(1);
            });
    }
}
