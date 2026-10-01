package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.dashboard.dto.CatalogOverviewDto;
import com.vyoog.eisplatform.modules.dashboard.dto.OrganizationsOverviewDto;
import com.vyoog.eisplatform.modules.dashboard.dto.PlatformBillingOverviewDto;
import com.vyoog.eisplatform.modules.dashboard.dto.PlatformDashboardDto;
import com.vyoog.eisplatform.modules.dashboard.dto.ServiceHealthDto;
import com.vyoog.eisplatform.modules.dashboard.dto.SubscriptionsOverviewDto;
import com.vyoog.eisplatform.modules.dashboard.dto.TopProductDto;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.platform.repository.PlatformRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;
import com.vyoog.eisplatform.modules.reviews.repository.ProductReviewRepository;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * "/admin/platform-dashboard" (C53): a platform-wide overview for a
 * platform administrator, built on the same "only real numbers, an
 * honest note instead of a fabricated one where data doesn't exist yet"
 * rule {@link BusinessDashboardService}'s own javadoc states for the
 * organization-scoped dashboard. Every figure is computed at request time
 * from an existing table — no new stored aggregate, no invented trend
 * line.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformDashboardService {

    private static final int TOP_PRODUCTS_LIMIT = 5;

    private final OrganizationRepository organizationRepository;
    private final ProductRepository productRepository;
    private final PlatformRepository platformRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductUsageRepository productUsageRepository;
    private final InvoiceService invoiceService;
    private final HealthEndpoint healthEndpoint;

    public PlatformDashboardDto getPlatformDashboard() {
        OrganizationsOverviewDto organizations = new OrganizationsOverviewDto(
            organizationRepository.count(),
            organizationRepository.countByLifecycleStatus(OrganizationLifecycleStatus.ACTIVE)
        );

        CatalogOverviewDto catalog = new CatalogOverviewDto(
            productRepository.count(),
            productRepository.countByStatus(ProductStatus.ACTIVE),
            platformRepository.count()
        );

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            byStatus.put(status.name(), subscriptionRepository.countByStatus(status));
        }
        SubscriptionsOverviewDto subscriptions = new SubscriptionsOverviewDto(
            byStatus.get(SubscriptionStatus.ACTIVE.name()), byStatus
        );

        Instant now = Instant.now();
        Instant thisPeriodStart = now.minus(30, ChronoUnit.DAYS);
        Instant lastPeriodStart = now.minus(60, ChronoUnit.DAYS);
        Map<String, Long> revenueThisPeriod = invoiceService.platformSpentInPeriod(thisPeriodStart, now);
        Map<String, Long> revenueLastPeriod = invoiceService.platformSpentInPeriod(lastPeriodStart, thisPeriodStart);
        PlatformBillingOverviewDto billing = new PlatformBillingOverviewDto(
            revenueThisPeriod,
            revenueLastPeriod,
            revenueThisPeriod.isEmpty() && revenueLastPeriod.isEmpty()
                ? "No paid invoices yet (REQ-BIL-001) — nothing has been charged through the payment gateway yet."
                : "Reflects paid invoices from Billing & Payments (REQ-BIL-001), platform-wide, trailing/preceding 30-day windows."
        );

        long openSupportTicketCount = supportTicketRepository.countByStatusIn(
            List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.ESCALATED));
        long pendingReviewCount = productReviewRepository.countByStatus(ReviewStatus.PENDING);

        ServiceHealthDto serviceHealth = new ServiceHealthDto(
            healthEndpoint.health().getStatus().getCode(),
            "This reflects the Vyoog platform's own service status, checked live right now. Per-product status "
                + "and incidents are posted on the service status page."
        );

        List<TopProductDto> topProducts = topProductsByLaunches();

        return new PlatformDashboardDto(
            organizations, catalog, subscriptions, billing,
            openSupportTicketCount, pendingReviewCount, serviceHealth, topProducts
        );
    }

    private List<TopProductDto> topProductsByLaunches() {
        List<Object[]> rows = productUsageRepository.topProductsByTotalLaunches(PageRequest.of(0, TOP_PRODUCTS_LIMIT));
        List<TopProductDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            long totalLaunches = ((Number) row[1]).longValue();
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                continue;
            }
            result.add(new TopProductDto(productId, product.getName(), totalLaunches));
        }
        return result;
    }
}
