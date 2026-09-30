package com.vyoog.eisplatform.modules.product.service;

import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.platform.repository.PlatformRepository;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the six named eVyoog product suites (see {@code docs/01-business/vision.md}
 * and {@code docs/01-business/scope.md}) as {@link Platform}s, each with one
 * flagship {@link Product}, on every startup, idempotently (checks by name
 * first — same pattern as {@link com.vyoog.eisplatform.modules.authorization.service.RbacSeeder}
 * and {@code PlatformAdministrationSeeder}). Decision C44 (2026-09-29):
 * "the platform's own catalog must contain every real eVyoog product suite,
 * not just whatever an admin happened to type in manually" — see
 * open-decisions.md.
 *
 * <p>Descriptions and categories here are sourced only from what this
 * repository's own source documents already say (EIS-document-analysis.md,
 * the product-recommendations FRD's own test fixture) — evyoog.com itself
 * could not be reached from this build environment (network egress to the
 * vyoog.com domain family is blocked here), so no domain feature claims are
 * invented for the three suites (Thittam.ai, Yukth.ai, Tharav.ai) this
 * codebase has no real description for. Their {@code description} says so
 * plainly rather than fabricating marketing copy. Real copy, images and
 * launch URLs should replace these through the existing admin catalog UI
 * (Settings &rarr; Catalog) once available — see the seeder's own row data
 * below for exactly which fields are placeholders.
 *
 * <p>{@code launchUrl}/{@code imageUrl} are deliberately left null (no
 * "Launch" button and no product image render at all — see ProductTile) —
 * this environment could not confirm a single real URL or asset for any of
 * the six suites, and shipping an unverified guess would be worse than
 * shipping nothing.
 *
 * <p>{@code @Order} after {@code PlatformAdministrationSeeder} implicitly
 * (both are plain {@code ApplicationRunner}s with no declared ordering
 * between them today) — this seeder does not depend on currencies/feature
 * flags being seeded first, so no explicit ordering is required.
 */
@Component
@RequiredArgsConstructor
public class CatalogSeeder implements ApplicationRunner {

    private final PlatformRepository platformRepository;
    private final ProductRepository productRepository;

    private record SuiteSeed(String platformName, String productName, String description, String category) {
    }

    /** The six suites named in vision.md/scope.md. Thiran.ai's description is
     * sourced from EIS-document-analysis.md ("Thiran's Product Life Cycle
     * Management, Production Management and Service Management"); Valam.ai
     * and Varthan.ai's are sourced from this codebase's own existing test
     * fixtures (ProductDetailPageTest, the product-recommendations FRD).
     * Thittam.ai, Yukth.ai and Tharav.ai have no description anywhere in
     * this repository's source documents beyond their name ("named only" —
     * EIS-document-analysis.md section 1.1.6) — their placeholder says so. */
    private static final List<SuiteSeed> SUITES = List.of(
        new SuiteSeed("Valam.ai", "Valam.ai", "AI-powered analytics and business intelligence.", "Analytics"),
        new SuiteSeed("Varthan.ai", "Varthan.ai", "AI-powered sales and customer engagement.", "Sales"),
        new SuiteSeed("Thittam.ai", "Thittam.ai", "One of eVyoog's AI product suites, hosted on this platform. Full product description not yet available.", null),
        new SuiteSeed("Thiran.ai", "Thiran.ai", "Product lifecycle, production and service management.", "Operations"),
        new SuiteSeed("Yukth.ai", "Yukth.ai", "One of eVyoog's AI product suites, hosted on this platform. Full product description not yet available.", null),
        new SuiteSeed("Tharav.ai", "Tharav.ai", "One of eVyoog's AI product suites, hosted on this platform. Full product description not yet available.", null)
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (SuiteSeed seed : SUITES) {
            Platform platform = platformRepository.findByName(seed.platformName())
                .orElseGet(() -> {
                    Platform created = new Platform();
                    created.setName(seed.platformName());
                    created.setDescription(seed.description());
                    return platformRepository.save(created);
                });

            if (productRepository.findByName(seed.productName()).isPresent()) continue;

            Product product = new Product();
            product.setName(seed.productName());
            product.setDescription(seed.description());
            product.setCategory(seed.category());
            product.setPrice(BigDecimal.ZERO);
            product.setStatus(ProductStatus.ACTIVE);
            product.setFeatured(true);
            product.getPlatforms().add(platform);
            product = productRepository.save(product);

            ProductPlan plan = new ProductPlan();
            plan.setProduct(product);
            plan.setName("Standard");
            plan.setPrice(BigDecimal.ZERO);
            plan.setBillingPeriod(BillingPeriod.MONTHLY);
            plan.setCurrency(Currency.USD);
            plan.setSortOrder(0);
            product.getPlans().add(plan);
            productRepository.save(product);
        }
    }
}
