package com.vyoog.eisplatform.modules.product.service;

import com.vyoog.eisplatform.modules.platform.repository.PlatformRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** C44 (2026-09-29): the catalog must contain every named eVyoog product
 * suite. {@code CatalogSeeder} itself already ran once via
 * {@code ApplicationRunner} by the time this test executes (same
 * @SpringBootTest context as the app), so this just asserts the outcome. */
@SpringBootTest
@ActiveProfiles("test")
class CatalogSeederTest {

    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private ProductRepository productRepository;

    private static final String[] SUITE_NAMES = {
        "Valam.ai", "Varthan.ai", "Thittam.ai", "Thiran.ai", "Yukth.ai", "Tharav.ai"
    };

    @Test
    void everyNamedSuiteExistsAsAPlatformAndAFlagshipProduct() {
        for (String name : SUITE_NAMES) {
            assertThat(platformRepository.findByName(name)).as("platform " + name).isPresent();
            assertThat(productRepository.findByName(name)).as("product " + name).isPresent();
        }
    }

    @Test
    @Transactional
    void everySeededFlagshipProductIsActiveFeaturedAndHasAStandardPlan() {
        for (String name : SUITE_NAMES) {
            Product product = productRepository.findByName(name).orElseThrow();
            assertThat(product.getStatus().name()).isEqualTo("ACTIVE");
            assertThat(product.isFeatured()).isTrue();
            assertThat(product.getPlans()).hasSize(1);
            assertThat(product.getPlans().get(0).getName()).isEqualTo("Standard");
        }
    }

    @Test
    void runningTheSeederAgainDoesNotCreateDuplicates() {
        long platformsBefore = platformRepository.count();
        long productsBefore = productRepository.count();

        new CatalogSeeder(platformRepository, productRepository).run(null);

        assertThat(platformRepository.count()).isEqualTo(platformsBefore);
        assertThat(productRepository.count()).isEqualTo(productsBefore);
    }
}
