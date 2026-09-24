package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.authorization.service.PrivilegedAccessService;
import com.vyoog.eisplatform.modules.dashboard.dto.DashboardDto;
import com.vyoog.eisplatform.modules.dashboard.dto.DashboardPreferenceDto;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 16: proves every dashboard alert is derived from a real fact (seat
 * count, subscription expiry, org MFA policy, a real PAM request — see
 * DashboardService's own javadoc) and that favorites/launch-tracking are
 * genuinely persisted per customer, not fabricated.
 */
@SpringBootTest
@ActiveProfiles("test")
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private PrivilegedAccessService privilegedAccessService;
    @Autowired
    private OrganizationMemberService organizationMemberService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSubscriptionRepository subscriptionRepository;

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setCode("DASH-" + System.nanoTime());
        organization.setBusinessEmail("biz@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setKeycloakSub("sub-" + email);
        return customerRepository.save(customer);
    }

    private Product newProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(BigDecimal.TEN);
        return productRepository.save(product);
    }

    private void subscribeOrgTo(Organization org, Product product, Instant expiresAt) {
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        subscription.setOwnerOrganizationId(org.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(expiresAt);
        subscriptionRepository.save(subscription);
    }

    @Test
    void anIndividualCustomerWithNoOrganizationGetsANullOrganizationAndTheEntitlementShape() {
        Customer lone = newCustomer("dash-lone@test-org.example");
        DashboardDto dashboard = dashboardService.getDashboard(lone.getId(), lone.getKeycloakSub(), false);

        assertThat(dashboard.organization()).isNull();
        // SubscriptionService.listMyProducts (existing, unchanged behavior)
        // shows the whole active catalog for an individual, not just
        // subscribed products — so this asserts the INDIVIDUAL shape
        // (myAccessAssigned is null, an org-only concept) rather than an
        // empty list, which shared test-class DB state would make flaky.
        assertThat(dashboard.products()).allMatch(p -> p.myAccessAssigned() == null && !p.favorite() && p.launchCount() == 0);
        assertThat(dashboard.alerts()).isEmpty();
    }

    @Test
    void seatLimitReachedProducesARealAlert() {
        Organization org = newOrganization();
        org.setLicensedSeats(1);
        organizationRepository.save(org);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("dash-seat@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();

        DashboardDto dashboard = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), false);

        assertThat(dashboard.alerts()).anyMatch(a -> a.type().equals("SEAT_LIMIT_REACHED"));
    }

    @Test
    void organizationMfaPolicyProducesAnAlertOnlyWhenThisSessionDidNotUseOtp() {
        Organization org = newOrganization();
        org.setMfaRequired(true);
        organizationRepository.save(org);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("dash-mfa@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();

        DashboardDto withoutOtp = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), false);
        assertThat(withoutOtp.alerts()).anyMatch(a -> a.type().equals("ORGANIZATION_MFA_REQUIRED"));

        DashboardDto withOtp = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), true);
        assertThat(withOtp.alerts()).noneMatch(a -> a.type().equals("ORGANIZATION_MFA_REQUIRED"));
    }

    @Test
    void aSubscriptionExpiringSoonProducesAnAlertButOneFarInTheFutureDoesNot() {
        Organization org = newOrganization();
        Product expiringSoon = newProduct("Expiring Soon App");
        Product expiringLater = newProduct("Expiring Later App");
        subscribeOrgTo(org, expiringSoon, Instant.now().plus(5, ChronoUnit.DAYS));
        subscribeOrgTo(org, expiringLater, Instant.now().plus(365, ChronoUnit.DAYS));
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("dash-expiry@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();

        DashboardDto dashboard = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), false);

        assertThat(dashboard.alerts())
            .filteredOn(a -> a.type().equals("SUBSCRIPTION_EXPIRING_SOON"))
            .hasSize(1)
            .allMatch(a -> a.message().contains("Expiring Soon App"));
    }

    @Test
    void aPendingPrivilegedAccessRequestProducesAnAlert() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("dash-pam@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();
        privilegedAccessService.request(customer.getKeycloakSub(), customer.getId(), "MANAGE_USERS", "need it for dashboard test", 30);

        DashboardDto dashboard = dashboardService.getDashboard(customer.getId(), customer.getKeycloakSub(), false);

        assertThat(dashboard.alerts()).anyMatch(a -> a.type().equals("PRIVILEGED_ACCESS_PENDING"));
    }

    @Test
    void favoritingTogglesOnAndOffAndReflectsInTheProductList() {
        Organization org = newOrganization();
        Product product = newProduct("Favorite Me");
        subscribeOrgTo(org, product, null);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("dash-fav@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();

        DashboardDto before = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), false);
        assertThat(before.products().get(0).favorite()).isFalse();

        dashboardService.addFavorite(adminCustomer.getId(), product.getId());
        DashboardDto afterAdd = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), false);
        assertThat(afterAdd.products().get(0).favorite()).isTrue();

        dashboardService.removeFavorite(adminCustomer.getId(), product.getId());
        DashboardDto afterRemove = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), false);
        assertThat(afterRemove.products().get(0).favorite()).isFalse();
    }

    @Test
    void favoritingAProductThatDoesNotExistIsRejected() {
        Customer customer = newCustomer("dash-badfav@test-org.example");
        assertThatThrownBy(() -> dashboardService.addFavorite(customer.getId(), 999_999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void recordingLaunchesAccumulatesCountAndUpdatesRecency() {
        Organization org = newOrganization();
        Product product = newProduct("Launch Me");
        subscribeOrgTo(org, product, null);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("dash-launch@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();

        dashboardService.recordLaunch(adminCustomer.getId(), product.getId());
        dashboardService.recordLaunch(adminCustomer.getId(), product.getId());
        dashboardService.recordLaunch(adminCustomer.getId(), product.getId());

        DashboardDto dashboard = dashboardService.getDashboard(adminCustomer.getId(), adminCustomer.getKeycloakSub(), false);
        var entry = dashboard.products().get(0);
        assertThat(entry.launchCount()).isEqualTo(3);
        assertThat(entry.lastLaunchedAt()).isNotNull();
    }

    @Test
    void preferencesDefaultThenRoundTripAfterUpdate() {
        Customer customer = newCustomer("dash-prefs@test-org.example");

        DashboardPreferenceDto defaults = dashboardService.getPreferences(customer.getId());
        assertThat(defaults.widgetOrder()).containsExactly("organization", "alerts", "recentlyUsed", "favorites", "products");
        assertThat(defaults.hiddenWidgets()).isEmpty();

        dashboardService.updatePreferences(customer.getId(), java.util.List.of("products", "alerts"), java.util.List.of("organization"));

        DashboardPreferenceDto updated = dashboardService.getPreferences(customer.getId());
        assertThat(updated.widgetOrder()).containsExactly("products", "alerts");
        assertThat(updated.hiddenWidgets()).containsExactly("organization");
    }
}
