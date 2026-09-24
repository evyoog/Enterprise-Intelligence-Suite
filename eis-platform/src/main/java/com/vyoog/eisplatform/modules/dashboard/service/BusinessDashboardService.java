package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.dashboard.dto.BillingOverviewDto;
import com.vyoog.eisplatform.modules.dashboard.dto.BusinessApplicationDto;
import com.vyoog.eisplatform.modules.dashboard.dto.BusinessDashboardDto;
import com.vyoog.eisplatform.modules.dashboard.dto.DashboardAlertDto;
import com.vyoog.eisplatform.modules.dashboard.dto.SeatUsageDto;
import com.vyoog.eisplatform.modules.dashboard.dto.ServiceHealthDto;
import com.vyoog.eisplatform.modules.dashboard.dto.SupportOverviewDto;
import com.vyoog.eisplatform.modules.dashboard.model.ProductUsage;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Phase 19: the org-admin-only "business dashboard" — Business, Usage,
 * Billing, Service Health, Support, and Alerts sections, built ONLY from
 * data that genuinely exists. Two sections are deliberately partial:
 * Billing (no payment/invoice model exists anywhere in this backend — see
 * BillingOverviewDto) and Support (the separate Ticketing app has no
 * query-by-customer endpoint yet — see SupportOverviewDto). Reporting that
 * gap explicitly, rather than fabricating numbers, is the point of this
 * class's own javadoc as much as the aggregation logic is.
 *
 * <p>Phase 8 (2026.3.3) re-verified every claim in this class against the
 * real current state of the systems it describes, rather than trusting the
 * comments as written: re-read the real Ticketing app's own controllers/
 * services/domain model directly (still true — its ticket-read endpoints
 * are {@code VYG-ADMIN}-only and filter by board/status/tags, never by any
 * customer/organization identifier, and its {@code Ticket} entity has no
 * such field at all) and confirmed {@link #getBusinessDashboard} still
 * fabricates nothing. Only change this phase made: the Service Health note
 * now also explicitly names the "no incident-tracking/status-page history"
 * gap (see ServiceHealthDto's own updated javadoc) — the phase's own scope
 * named "Incident Integration" but no incident data source exists anywhere
 * in this system, so that gap is now disclosed with the same explicitness
 * as the Billing/Support gaps always have been, rather than left implicit.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessDashboardService {

    private final OrganizationSelfService organizationSelfService;
    private final SubscriptionService subscriptionService;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationProductAccessRepository accessRepository;
    private final ProductUsageRepository usageRepository;
    private final ProductRepository productRepository;
    private final HealthEndpoint healthEndpoint;

    private record UsageKey(Long customerId, Long productId) {
    }

    public BusinessDashboardDto getBusinessDashboard(Long customerId) {
        Long organizationId = organizationSelfService.requireBusinessDashboardAccess(customerId);
        OrganizationDto organization = organizationSelfService.getMyOrganization(customerId);

        List<OrganizationMember> activeMembers = memberRepository.findByOrganizationId(organizationId).stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .toList();
        Map<Long, Long> memberIdToCustomerId = activeMembers.stream()
            .collect(Collectors.toMap(OrganizationMember::getId, OrganizationMember::getCustomerId));

        List<OrganizationProductAccess> accessRows = accessRepository
            .findByOrganizationMemberIdIn(memberIdToCustomerId.keySet()).stream()
            .filter(a -> a.getStatus() == MembershipStatus.ACTIVE)
            .toList();

        Map<UsageKey, ProductUsage> usageIndex = usageRepository
            .findByCustomerIdIn(memberIdToCustomerId.values()).stream()
            .collect(Collectors.toMap(u -> new UsageKey(u.getCustomerId(), u.getProductId()), u -> u));

        Map<Long, SubscriptionStatus> subscriptionStatusByProduct = subscriptionService
            .listOrganizationSubscriptions(organizationId).stream()
            .collect(Collectors.toMap(SubscriptionDto::productId, SubscriptionDto::status, (a, b) -> a));

        Map<Long, List<OrganizationProductAccess>> accessByProduct = accessRows.stream()
            .collect(Collectors.groupingBy(OrganizationProductAccess::getProductId));

        List<BusinessApplicationDto> applications = new ArrayList<>();
        for (Map.Entry<Long, List<OrganizationProductAccess>> entry : accessByProduct.entrySet()) {
            Long productId = entry.getKey();
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                continue;
            }
            long totalLaunches = 0;
            Instant lastUsedAt = null;
            for (OrganizationProductAccess access : entry.getValue()) {
                Long memberCustomerId = memberIdToCustomerId.get(access.getOrganizationMemberId());
                ProductUsage usage = usageIndex.get(new UsageKey(memberCustomerId, productId));
                if (usage == null) {
                    continue;
                }
                totalLaunches += usage.getLaunchCount();
                if (usage.getLastLaunchedAt() != null && (lastUsedAt == null || usage.getLastLaunchedAt().isAfter(lastUsedAt))) {
                    lastUsedAt = usage.getLastLaunchedAt();
                }
            }
            applications.add(new BusinessApplicationDto(
                productId, product.getName(), product.getCategory(),
                subscriptionStatusByProduct.get(productId),
                entry.getValue().size(), totalLaunches, lastUsedAt));
        }
        applications.sort((a, b) -> a.productName().compareToIgnoreCase(b.productName()));

        SeatUsageDto seatUsage = new SeatUsageDto(
            organization.licensedSeats(),
            organization.activeMemberCount(),
            organization.licensedSeats() > 0
                ? (organization.activeMemberCount() * 100.0) / organization.licensedSeats()
                : 0.0
        );

        BillingOverviewDto billing = new BillingOverviewDto(
            subscriptionService.listOrganizationSubscriptions(organizationId),
            "No payment or invoice data is available — this reflects configured subscription status and dates only, "
                + "not actual charges. No payment gateway or invoice model exists in this system yet."
        );

        ServiceHealthDto serviceHealth = new ServiceHealthDto(
            healthEndpoint.health().getStatus().getCode(),
            "This reflects the Vyoog platform's own service status only, checked live right now. Individual "
                + "product health/uptime monitoring is not implemented yet, and there is no incident-tracking "
                + "or status-page history in this system yet — this is a current snapshot, not a record of "
                + "past incidents."
        );

        SupportOverviewDto support = new SupportOverviewDto(
            false,
            "Support ticket integration is not available yet — the Ticketing app does not currently expose a way "
                + "to look up tickets by customer or organization."
        );

        List<DashboardAlertDto> alerts = buildAlerts(organization, applications);

        return new BusinessDashboardDto(organization, applications, seatUsage, billing, serviceHealth, support, alerts);
    }

    private List<DashboardAlertDto> buildAlerts(OrganizationDto organization, List<BusinessApplicationDto> applications) {
        List<DashboardAlertDto> alerts = new ArrayList<>();

        if (organization.licensedSeats() > 0) {
            if (organization.activeMemberCount() >= organization.licensedSeats()) {
                alerts.add(new DashboardAlertDto("SEAT_LIMIT_REACHED", "warning",
                    "You've used all " + organization.licensedSeats() + " licensed seats. Contact Vyoog to add more."));
            } else {
                long unused = organization.licensedSeats() - organization.activeMemberCount();
                alerts.add(new DashboardAlertDto("UNDERUTILIZED_SEATS", "info",
                    "You have " + unused + " unused licensed seat" + (unused == 1 ? "" : "s") + "."));
            }
        }

        for (BusinessApplicationDto app : applications) {
            if (app.assignedMembers() > 0 && app.totalLaunches() == 0) {
                alerts.add(new DashboardAlertDto("UNUSED_PRODUCT_ACCESS", "info",
                    app.assignedMembers() + (app.assignedMembers() == 1 ? " member has" : " members have")
                        + " access to " + app.productName() + " but " + (app.assignedMembers() == 1 ? "has" : "have")
                        + " never launched it."));
            }
        }

        return alerts;
    }
}
