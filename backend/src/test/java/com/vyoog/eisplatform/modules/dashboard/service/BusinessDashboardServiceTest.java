package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.dashboard.dto.BusinessApplicationDto;
import com.vyoog.eisplatform.modules.dashboard.dto.BusinessDashboardDto;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Phase 19: the org-admin-only business dashboard — real org-wide
 * aggregation of usage/access/subscriptions, never fabricated billing or
 * per-product health data (see BusinessDashboardService's own javadoc). */
@SpringBootTest
@ActiveProfiles("test")
class BusinessDashboardServiceTest {

    @Autowired
    private BusinessDashboardService businessDashboardService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private OrganizationSelfService organizationSelfService;
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
        organization.setName("Business Dashboard Test Org");
        organization.setCode("BIZ-" + System.nanoTime());
        organization.setBusinessEmail("biz@bizdash.example");
        organization.setCountry("India");
        organization.setLicensedSeats(5);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    private Product newProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(BigDecimal.TEN);
        return productRepository.save(product);
    }

    private void subscribeOrgTo(Organization org, Product product) {
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        subscription.setOwnerOrganizationId(org.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscriptionRepository.save(subscription);
    }

    @Test
    void nonAdminCannotSeeTheBusinessDashboard() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("member@bizdash.example").getId(), OrgRole.MEMBER);

        assertThatThrownBy(() -> businessDashboardService.getBusinessDashboard(member.getCustomerId()))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void orgAdminSeesRealSeatUsageAndSubscriptions() {
        Organization org = newOrganization();
        Product product = newProduct("PMS-" + System.nanoTime());
        subscribeOrgTo(org, product);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("admin@bizdash.example").getId(), OrgRole.ORG_ADMIN);

        BusinessDashboardDto dashboard = businessDashboardService.getBusinessDashboard(admin.getCustomerId());

        assertThat(dashboard.seatUsage().licensedSeats()).isEqualTo(5);
        assertThat(dashboard.seatUsage().activeMemberCount()).isEqualTo(1);
        assertThat(dashboard.billing().subscriptions()).extracting(s -> s.productId()).contains(product.getId());
        assertThat(dashboard.billing().note()).isNotBlank();
        assertThat(dashboard.serviceHealth().platformStatus()).isNotBlank();
        assertThat(dashboard.support().available()).isFalse();
        assertThat(dashboard.support().note()).isNotBlank();
    }

    /** Phase 8 (2026.3.3): the phase's own scope names "Incident
     * Integration" explicitly — no incident data source exists anywhere in
     * this system, so this asserts that gap is disclosed with the same
     * explicitness as the Billing/Support gaps, never silently omitted or
     * papered over with an invented incident feed. */
    @Test
    void serviceHealthExplicitlyDisclosesTheLackOfIncidentTracking() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("admin-health@bizdash.example").getId(), OrgRole.ORG_ADMIN);

        BusinessDashboardDto dashboard = businessDashboardService.getBusinessDashboard(admin.getCustomerId());

        assertThat(dashboard.serviceHealth().note()).containsIgnoringCase("incident");
    }

    @Test
    void applicationUsageAggregatesAcrossAllMembersNotJustOne() {
        Organization org = newOrganization();
        Product product = newProduct("Ticketing-" + System.nanoTime());
        subscribeOrgTo(org, product);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("admin2@bizdash.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("teammate2@bizdash.example").getId(), OrgRole.MEMBER);

        organizationSelfService.assignProductAccess(admin.getCustomerId(), admin.getId(), product.getId(), "ADMIN");
        organizationSelfService.assignProductAccess(admin.getCustomerId(), teammate.getId(), product.getId(), "USER");

        dashboardService.recordLaunch(admin.getCustomerId(), product.getId());
        dashboardService.recordLaunch(admin.getCustomerId(), product.getId());
        dashboardService.recordLaunch(teammate.getCustomerId(), product.getId());

        BusinessDashboardDto dashboard = businessDashboardService.getBusinessDashboard(admin.getCustomerId());

        BusinessApplicationDto app = dashboard.applications().stream()
            .filter(a -> a.productId().equals(product.getId())).findFirst().orElseThrow();
        assertThat(app.assignedMembers()).isEqualTo(2);
        assertThat(app.totalLaunches()).isEqualTo(3);
        assertThat(app.lastUsedAt()).isNotNull();
    }

    @Test
    void unusedProductAccessProducesARealAlert() {
        Organization org = newOrganization();
        Product product = newProduct("Requirements-" + System.nanoTime());
        subscribeOrgTo(org, product);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("admin3@bizdash.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("teammate3@bizdash.example").getId(), OrgRole.MEMBER);

        // Assigned but never launched.
        organizationSelfService.assignProductAccess(admin.getCustomerId(), teammate.getId(), product.getId(), "USER");

        BusinessDashboardDto dashboard = businessDashboardService.getBusinessDashboard(admin.getCustomerId());

        assertThat(dashboard.alerts()).anyMatch(a -> a.type().equals("UNUSED_PRODUCT_ACCESS") && a.message().contains(product.getName()));
    }
}
