package com.vyoog.eisplatform.modules.dashboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestDto;
import com.vyoog.eisplatform.modules.authorization.service.PrivilegedAccessService;
import com.vyoog.eisplatform.modules.dashboard.dto.*;
import com.vyoog.eisplatform.modules.dashboard.model.DashboardPreference;
import com.vyoog.eisplatform.modules.dashboard.model.FavoriteProduct;
import com.vyoog.eisplatform.modules.dashboard.model.ProductUsage;
import com.vyoog.eisplatform.modules.dashboard.repository.DashboardPreferenceRepository;
import com.vyoog.eisplatform.modules.dashboard.repository.FavoriteProductRepository;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.MyProductDto;
import com.vyoog.eisplatform.modules.registration.dto.OrgProductAccessDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Phase 16: aggregates the personalized dashboard purely by REUSING Phases
 * 2-8's existing services (never duplicating their logic) and adding the two
 * genuinely new pieces this phase needs — favorites and real launch-usage
 * tracking. Every alert is derived from a fact one of those existing
 * services already exposes; nothing here fabricates data.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RENEWAL_REMINDER_DAYS = 30;
    // "recentlyUsed"/"favorites" are just derived views over the same product
    // list (see DashboardProductDto) — listed here so they're visible and
    // reorderable like any other widget by default.
    private static final List<String> DEFAULT_WIDGET_ORDER =
        List.of("organization", "alerts", "recentlyUsed", "favorites", "products");

    private final OrganizationSelfService organizationSelfService;
    private final SubscriptionService subscriptionService;
    private final PrivilegedAccessService privilegedAccessService;
    private final ProductRepository productRepository;
    private final FavoriteProductRepository favoriteRepository;
    private final ProductUsageRepository usageRepository;
    private final DashboardPreferenceRepository preferenceRepository;
    private final ObjectMapper objectMapper;

    public DashboardDto getDashboard(Long customerId, String keycloakSub, boolean mfaVerifiedThisSession) {
        boolean isOrgMember = organizationSelfService.hasOrganization(customerId);

        OrganizationDto organization = isOrgMember ? organizationSelfService.getMyOrganization(customerId) : null;
        List<DashboardProductDto> products = buildProducts(customerId, isOrgMember);
        List<DashboardAlertDto> alerts = buildAlerts(customerId, keycloakSub, organization, isOrgMember, mfaVerifiedThisSession);
        DashboardPreferenceDto preferences = getPreferences(customerId);

        return new DashboardDto(organization, products, alerts, preferences);
    }

    // ------------------------------------------------------------------
    // Products (merges catalog/entitlement data with this customer's own
    // favorite/usage rows — the two genuinely new pieces this phase adds).
    // ------------------------------------------------------------------

    private List<DashboardProductDto> buildProducts(Long customerId, boolean isOrgMember) {
        Map<Long, FavoriteProduct> favoritesByProduct = favoriteRepository.findByCustomerId(customerId).stream()
            .collect(Collectors.toMap(FavoriteProduct::getProductId, f -> f, (a, b) -> a));
        Map<Long, ProductUsage> usageByProduct = usageRepository.findByCustomerId(customerId).stream()
            .collect(Collectors.toMap(ProductUsage::getProductId, u -> u, (a, b) -> a));

        if (isOrgMember) {
            List<OrgProductAccessDto> orgProducts = organizationSelfService.listMyOrgProducts(customerId);
            Map<Long, String> launchUrlByProduct = launchUrlsFor(orgProducts.stream().map(OrgProductAccessDto::productId).toList());
            return orgProducts.stream()
                .map(p -> toDashboardProduct(p, favoritesByProduct, usageByProduct, launchUrlByProduct.get(p.productId())))
                .toList();
        }
        List<MyProductDto> myProducts = subscriptionService.listMyProducts(customerId);
        Map<Long, String> launchUrlByProduct = launchUrlsFor(myProducts.stream().map(MyProductDto::productId).toList());
        return myProducts.stream()
            .map(p -> toDashboardProduct(p, favoritesByProduct, usageByProduct, launchUrlByProduct.get(p.productId())))
            .toList();
    }

    private Map<Long, String> launchUrlsFor(List<Long> productIds) {
        return productRepository.findAllById(productIds).stream()
            .filter(p -> p.getLaunchUrl() != null)
            .collect(Collectors.toMap(com.vyoog.eisplatform.modules.product.model.Product::getId,
                com.vyoog.eisplatform.modules.product.model.Product::getLaunchUrl));
    }

    private DashboardProductDto toDashboardProduct(OrgProductAccessDto p, Map<Long, FavoriteProduct> favorites, Map<Long, ProductUsage> usage, String launchUrl) {
        ProductUsage u = usage.get(p.productId());
        return new DashboardProductDto(
            p.productId(), p.productName(), p.category(), launchUrl, p.orgSubscriptionStatus(),
            p.myAccessAssigned(), p.myProductRole(),
            favorites.containsKey(p.productId()),
            u == null ? null : u.getLastLaunchedAt(),
            u == null ? 0 : u.getLaunchCount()
        );
    }

    private DashboardProductDto toDashboardProduct(MyProductDto p, Map<Long, FavoriteProduct> favorites, Map<Long, ProductUsage> usage, String launchUrl) {
        ProductUsage u = usage.get(p.productId());
        return new DashboardProductDto(
            p.productId(), p.productName(), p.category(), launchUrl, p.subscriptionStatus(),
            null, null,
            favorites.containsKey(p.productId()),
            u == null ? null : u.getLastLaunchedAt(),
            u == null ? 0 : u.getLaunchCount()
        );
    }

    // ------------------------------------------------------------------
    // Alerts — every one derived from an existing, real fact.
    // ------------------------------------------------------------------

    private List<DashboardAlertDto> buildAlerts(Long customerId, String keycloakSub, OrganizationDto organization,
                                                 boolean isOrgMember, boolean mfaVerifiedThisSession) {
        List<DashboardAlertDto> alerts = new java.util.ArrayList<>();

        if (organization != null) {
            if (organization.licensedSeats() > 0 && organization.activeMemberCount() >= organization.licensedSeats()) {
                alerts.add(new DashboardAlertDto("SEAT_LIMIT_REACHED", "warning",
                    "Your organization is using all " + organization.licensedSeats() + " licensed seats."));
            }
            if (organization.mfaRequired() && !mfaVerifiedThisSession) {
                alerts.add(new DashboardAlertDto("ORGANIZATION_MFA_REQUIRED", "warning",
                    "Your organization requires multi-factor authentication for this account."));
            }
        }

        List<SubscriptionDto> subscriptions = isOrgMember
            ? organizationSelfService.listMyOrgSubscriptions(customerId)
            : subscriptionService.listMySubscriptions(customerId);
        Instant soon = Instant.now().plus(RENEWAL_REMINDER_DAYS, ChronoUnit.DAYS);
        for (SubscriptionDto subscription : subscriptions) {
            if (subscription.status() == SubscriptionStatus.ACTIVE
                    && subscription.expiresAt() != null
                    && subscription.expiresAt().isBefore(soon)) {
                alerts.add(new DashboardAlertDto("SUBSCRIPTION_EXPIRING_SOON", "info",
                    subscription.productName() + "'s subscription expires on " + subscription.expiresAt() + "."));
            }
        }

        for (PrivilegedAccessRequestDto request : privilegedAccessService.listMyRequests(keycloakSub)) {
            if ("PENDING".equals(request.effectiveStatus())) {
                alerts.add(new DashboardAlertDto("PRIVILEGED_ACCESS_PENDING", "info",
                    "Your request for " + request.permissionName() + " is awaiting approval."));
            } else if ("APPROVED".equals(request.effectiveStatus())) {
                alerts.add(new DashboardAlertDto("PRIVILEGED_ACCESS_ACTIVE", "info",
                    "You have temporary " + request.permissionName() + " access until " + request.expiresAt() + "."));
            }
        }

        return alerts;
    }

    // ------------------------------------------------------------------
    // Favorites
    // ------------------------------------------------------------------

    @Transactional
    public void addFavorite(Long customerId, Long productId) {
        requireRealProduct(productId);
        if (favoriteRepository.findByCustomerIdAndProductId(customerId, productId).isEmpty()) {
            FavoriteProduct favorite = new FavoriteProduct();
            favorite.setCustomerId(customerId);
            favorite.setProductId(productId);
            favoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void removeFavorite(Long customerId, Long productId) {
        favoriteRepository.findByCustomerIdAndProductId(customerId, productId).ifPresent(favoriteRepository::delete);
    }

    // ------------------------------------------------------------------
    // Usage / "recently used" tracking
    // ------------------------------------------------------------------

    @Transactional
    public void recordLaunch(Long customerId, Long productId) {
        requireRealProduct(productId);
        ProductUsage usage = usageRepository.findByCustomerIdAndProductId(customerId, productId)
            .orElseGet(() -> {
                ProductUsage created = new ProductUsage();
                created.setCustomerId(customerId);
                created.setProductId(productId);
                created.setLaunchCount(0);
                return created;
            });
        usage.setLaunchCount(usage.getLaunchCount() + 1);
        usage.setLastLaunchedAt(Instant.now());
        usageRepository.save(usage);
    }

    private void requireRealProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
    }

    // ------------------------------------------------------------------
    // Preferences
    // ------------------------------------------------------------------

    public DashboardPreferenceDto getPreferences(Long customerId) {
        return preferenceRepository.findById(customerId)
            .map(this::toDto)
            .orElseGet(() -> new DashboardPreferenceDto(DEFAULT_WIDGET_ORDER, List.of()));
    }

    @Transactional
    public DashboardPreferenceDto updatePreferences(Long customerId, List<String> widgetOrder, List<String> hiddenWidgets) {
        DashboardPreference preference = preferenceRepository.findById(customerId)
            .orElseGet(() -> {
                DashboardPreference created = new DashboardPreference();
                created.setCustomerId(customerId);
                return created;
            });
        preference.setWidgetOrderJson(writeJson(widgetOrder == null ? DEFAULT_WIDGET_ORDER : widgetOrder));
        preference.setHiddenWidgetsJson(writeJson(hiddenWidgets == null ? List.of() : hiddenWidgets));
        preference.setUpdatedAt(Instant.now());
        preferenceRepository.save(preference);
        return toDto(preference);
    }

    private DashboardPreferenceDto toDto(DashboardPreference preference) {
        return new DashboardPreferenceDto(
            readJson(preference.getWidgetOrderJson(), DEFAULT_WIDGET_ORDER),
            readJson(preference.getHiddenWidgetsJson(), List.of())
        );
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize dashboard preferences", e);
        }
    }

    private List<String> readJson(String json, List<String> fallback) {
        if (json == null || json.isBlank()) {
            return fallback;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return fallback;
        }
    }
}
