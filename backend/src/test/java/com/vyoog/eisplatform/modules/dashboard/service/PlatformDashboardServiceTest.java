package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.dashboard.dto.PlatformDashboardDto;
import com.vyoog.eisplatform.modules.dashboard.model.ProductUsage;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.platform.model.Platform;
import com.vyoog.eisplatform.modules.platform.repository.PlatformRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.reviews.model.ProductReview;
import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;
import com.vyoog.eisplatform.modules.reviews.repository.ProductReviewRepository;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** "/admin/platform-dashboard" (C53): real platform-wide counts, never a
 * fabricated trend — see PlatformDashboardService's own javadoc. Every
 * assertion here is a DELTA (before/after), never an absolute count, since
 * the shared test database already carries rows from other tests in the
 * same run. */
@SpringBootTest
@ActiveProfiles("test")
class PlatformDashboardServiceTest {

    @Autowired
    private PlatformDashboardService platformDashboardService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private ProductSubscriptionRepository subscriptionRepository;
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;
    @Autowired
    private ProductUsageRepository productUsageRepository;

    private Organization newOrganization(OrganizationLifecycleStatus lifecycleStatus) {
        Organization organization = new Organization();
        organization.setName("Platform Dashboard Test Org");
        organization.setCode("PDASH-" + System.nanoTime());
        organization.setBusinessEmail("pdash@example.com");
        organization.setCountry("India");
        organization.setLifecycleStatus(lifecycleStatus);
        return organizationRepository.save(organization);
    }

    private Product newProduct(ProductStatus status) {
        Product product = new Product();
        product.setName("PDash-" + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(status);
        return productRepository.save(product);
    }

    private void newSubscription(Long productId, SubscriptionStatus status) {
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(productId);
        subscription.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        subscription.setOwnerCustomerId(1L);
        subscription.setStatus(status);
        subscription.setStartedAt(Instant.now());
        subscriptionRepository.save(subscription);
    }

    @Test
    void organizationsCountReflectsLifecycleStatus() {
        PlatformDashboardDto before = platformDashboardService.getPlatformDashboard();

        newOrganization(OrganizationLifecycleStatus.ACTIVE);
        newOrganization(OrganizationLifecycleStatus.SUSPENDED);

        PlatformDashboardDto after = platformDashboardService.getPlatformDashboard();

        assertThat(after.organizations().total() - before.organizations().total()).isEqualTo(2);
        assertThat(after.organizations().active() - before.organizations().active()).isEqualTo(1);
    }

    @Test
    void catalogCountsReflectProductStatusAndPlatformCount() {
        PlatformDashboardDto before = platformDashboardService.getPlatformDashboard();

        newProduct(ProductStatus.ACTIVE);
        newProduct(ProductStatus.INACTIVE);
        Platform platform = new Platform();
        platform.setName("PDash Platform " + System.nanoTime());
        platformRepository.save(platform);

        PlatformDashboardDto after = platformDashboardService.getPlatformDashboard();

        assertThat(after.catalog().totalProducts() - before.catalog().totalProducts()).isEqualTo(2);
        assertThat(after.catalog().activeProducts() - before.catalog().activeProducts()).isEqualTo(1);
        assertThat(after.catalog().totalPlatforms() - before.catalog().totalPlatforms()).isEqualTo(1);
    }

    @Test
    void subscriptionsByStatusCountsEveryOwnerPlatformWide() {
        Product product = newProduct(ProductStatus.ACTIVE);
        PlatformDashboardDto before = platformDashboardService.getPlatformDashboard();

        newSubscription(product.getId(), SubscriptionStatus.ACTIVE);
        newSubscription(product.getId(), SubscriptionStatus.CANCELLED);

        PlatformDashboardDto after = platformDashboardService.getPlatformDashboard();

        long activeBefore = before.subscriptions().byStatus().get("ACTIVE");
        long activeAfter = after.subscriptions().byStatus().get("ACTIVE");
        long cancelledBefore = before.subscriptions().byStatus().get("CANCELLED");
        long cancelledAfter = after.subscriptions().byStatus().get("CANCELLED");

        assertThat(activeAfter - activeBefore).isEqualTo(1);
        assertThat(cancelledAfter - cancelledBefore).isEqualTo(1);
        assertThat(after.subscriptions().active()).isEqualTo(activeAfter);
    }

    @Test
    void openSupportTicketCountExcludesTerminalStatuses() {
        PlatformDashboardDto before = platformDashboardService.getPlatformDashboard();

        SupportTicket open = new SupportTicket();
        open.setRequestedByCustomerId(1L);
        open.setSubject("Open ticket");
        open.setDescription("Needs attention");
        open.setStatus(TicketStatus.OPEN);
        supportTicketRepository.save(open);

        SupportTicket resolved = new SupportTicket();
        resolved.setRequestedByCustomerId(1L);
        resolved.setSubject("Resolved ticket");
        resolved.setDescription("Already handled");
        resolved.setStatus(TicketStatus.RESOLVED);
        supportTicketRepository.save(resolved);

        PlatformDashboardDto after = platformDashboardService.getPlatformDashboard();

        assertThat(after.openSupportTicketCount() - before.openSupportTicketCount()).isEqualTo(1);
    }

    @Test
    void pendingReviewCountReflectsModerationStatus() {
        Product product = newProduct(ProductStatus.ACTIVE);
        PlatformDashboardDto before = platformDashboardService.getPlatformDashboard();

        ProductReview pending = new ProductReview();
        pending.setProductId(product.getId());
        pending.setCustomerId(1L);
        pending.setRating(5);
        pending.setStatus(ReviewStatus.PENDING);
        productReviewRepository.save(pending);

        ProductReview approved = new ProductReview();
        approved.setProductId(product.getId());
        approved.setCustomerId(2L);
        approved.setRating(4);
        approved.setStatus(ReviewStatus.APPROVED);
        productReviewRepository.save(approved);

        PlatformDashboardDto after = platformDashboardService.getPlatformDashboard();

        assertThat(after.pendingReviewCount() - before.pendingReviewCount()).isEqualTo(1);
    }

    @Test
    void topProductsByLaunchesIsRealUsageNeverFabricated() {
        Product product = newProduct(ProductStatus.ACTIVE);
        ProductUsage usage = new ProductUsage();
        usage.setCustomerId(1L);
        usage.setProductId(product.getId());
        usage.setLaunchCount(7);
        productUsageRepository.save(usage);

        PlatformDashboardDto dashboard = platformDashboardService.getPlatformDashboard();

        assertThat(dashboard.topProductsByLaunches())
            .anyMatch(p -> p.productId().equals(product.getId()) && p.totalLaunches() == 7 && p.productName().equals(product.getName()));
    }

    @Test
    void serviceHealthAndBillingNoteAreAlwaysPresent() {
        PlatformDashboardDto dashboard = platformDashboardService.getPlatformDashboard();

        assertThat(dashboard.serviceHealth().platformStatus()).isNotBlank();
        assertThat(dashboard.billing().note()).isNotBlank();
    }
}
