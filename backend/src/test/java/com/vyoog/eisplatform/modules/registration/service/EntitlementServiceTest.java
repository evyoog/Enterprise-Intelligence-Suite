package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.EntitlementCheckRequest;
import com.vyoog.eisplatform.modules.registration.dto.EntitlementDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-SUB-002 (C97 EN-1..EN-5): derived, read-only entitlements and the check hosted products call. */
@SpringBootTest
@ActiveProfiles("test")
class EntitlementServiceTest {

    @Autowired private EntitlementService entitlements;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private OrganizationProductAccessRepository accessRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;

    private Customer customer() {
        Customer c = new Customer();
        c.setEmail("entitle-" + System.nanoTime() + "@example.com");
        c.setFirstName("En");
        c.setLastName("Title");
        return customerRepository.save(c);
    }

    private ProductPlan plan(String features, Integer limit) {
        Product product = new Product();
        product.setName("Entitled Product " + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName("Pro");
        plan.setPrice(BigDecimal.TEN);
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setCurrency(Currency.INR);
        plan.setIncludedFeatures(features);
        plan.setUsageLimit(limit);
        return planRepository.save(plan);
    }

    private ProductSubscription subscribe(ProductPlan plan, Customer owner, Organization org, SubscriptionStatus status) {
        ProductSubscription s = new ProductSubscription();
        s.setProductId(plan.getProduct().getId());
        s.setPlanId(plan.getId());
        s.setOwnerType(org == null ? RegistrationOwnerType.INDIVIDUAL : RegistrationOwnerType.ORGANIZATION);
        s.setOwnerCustomerId(owner == null ? null : owner.getId());
        s.setOwnerOrganizationId(org == null ? null : org.getId());
        s.setStatus(status);
        s.setExpiresAt(Instant.now().plus(20, ChronoUnit.DAYS));
        return subscriptionRepository.save(s);
    }

    @Test
    void anActiveSubscriptionGivesTheFeaturesAndOnlyTheLimit() {
        Customer c = customer();
        ProductPlan plan = plan("Reports, API access ,Exports", 1000);
        subscribe(plan, c, null, SubscriptionStatus.ACTIVE);

        EntitlementDto e = entitlements.forCustomer(c.getId()).get(0);

        assertThat(e.includedFeatures()).containsExactly("Reports", "API access", "Exports");
        assertThat(e.usageLimit()).isEqualTo(1000);
        assertThat(e.status()).isEqualTo("ACTIVE");
        assertThat(e.ownerType()).isEqualTo("INDIVIDUAL");

        var ok = entitlements.check(new EntitlementCheckRequest(c.getId(), null, plan.getProduct().getId(), "api access"));
        assertThat(ok.allowed()).isTrue();
        assertThat(ok.plan()).isEqualTo("Pro");
        assertThat(ok.usageLimit()).isEqualTo(1000);
        var missing = entitlements.check(new EntitlementCheckRequest(c.getId(), null, plan.getProduct().getId(), "SSO"));
        assertThat(missing.allowed()).isFalse();
        assertThat(missing.reason()).isEqualTo("FEATURE_NOT_INCLUDED");
    }

    @Test
    void suspendedCancelledExpiredAndUnknownGrantNothing() {
        Customer c = customer();
        for (SubscriptionStatus status : new SubscriptionStatus[] {SubscriptionStatus.SUSPENDED, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED,
                SubscriptionStatus.PENDING_SUBSCRIPTION}) {
            ProductPlan plan = plan("A", null);
            subscribe(plan, c, null, status);
            var response = entitlements.check(new EntitlementCheckRequest(c.getId(), null, plan.getProduct().getId(), null));
            assertThat(response.allowed()).as(status.name()).isFalse();
            assertThat(response.reason()).isEqualTo("NO_SUBSCRIPTION");
        }
        assertThat(entitlements.forCustomer(c.getId())).isEmpty();
        assertThat(entitlements.check(new EntitlementCheckRequest(c.getId(), null, 999999L, null)).reason()).isEqualTo("UNKNOWN_PRODUCT");
        assertThat(entitlements.check(new EntitlementCheckRequest(null, null, 1L, null)).reason()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void aSubscriptionPastItsTermEndGrantsNothingEvenBeforeTheExpiryJobRuns() {
        Customer c = customer();
        ProductPlan plan = plan("A", null);
        ProductSubscription s = subscribe(plan, c, null, SubscriptionStatus.ACTIVE);
        s.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        subscriptionRepository.save(s);

        assertThat(entitlements.check(new EntitlementCheckRequest(c.getId(), null, plan.getProduct().getId(), null)).allowed()).isFalse();
    }

    @Test
    void aPlanChangeShowsAtOnce() {
        Customer c = customer();
        ProductPlan basic = plan("A", 10);
        ProductPlan better = new ProductPlan();
        better.setProduct(basic.getProduct());
        better.setName("Better");
        better.setPrice(BigDecimal.valueOf(50));
        better.setBillingPeriod(BillingPeriod.MONTHLY);
        better.setCurrency(Currency.INR);
        better.setIncludedFeatures("A, B");
        better.setUsageLimit(500);
        better = planRepository.save(better);
        ProductSubscription s = subscribe(basic, c, null, SubscriptionStatus.ACTIVE);
        s.setPlanId(better.getId());
        subscriptionRepository.save(s);

        var response = entitlements.check(new EntitlementCheckRequest(c.getId(), null, basic.getProduct().getId(), "B"));
        assertThat(response.allowed()).isTrue();
        assertThat(response.usageLimit()).isEqualTo(500);
    }

    @Test
    void anOrganizationMemberNeedsTheOrganizationsSubscriptionAndOwnProductAccess() {
        Organization org = new Organization();
        org.setName("Entitled Org");
        org.setCode("ENT-" + System.nanoTime());
        org.setBusinessEmail("ent@example.com");
        org.setCountry("India");
        org = organizationRepository.save(org);
        Customer withAccess = customer();
        Customer withoutAccess = customer();
        Customer outsider = customer();
        ProductPlan plan = plan("Dashboards", null);
        subscribe(plan, null, org, SubscriptionStatus.ACTIVE);
        for (Customer c : new Customer[] {withAccess, withoutAccess}) {
            OrganizationMember m = new OrganizationMember();
            m.setOrganizationId(org.getId());
            m.setCustomerId(c.getId());
            m.setOrgRole(OrgRole.MEMBER);
            m.setStatus(MembershipStatus.ACTIVE);
            m = memberRepository.save(m);
            if (c == withAccess) {
                OrganizationProductAccess a = new OrganizationProductAccess();
                a.setOrganizationMemberId(m.getId());
                a.setProductId(plan.getProduct().getId());
                a.setProductRole("USER");
                a.setStatus(MembershipStatus.ACTIVE);
                accessRepository.save(a);
            }
        }
        Long productId = plan.getProduct().getId();

        assertThat(entitlements.check(new EntitlementCheckRequest(null, org.getId(), productId, null)).allowed()).as("the organization as a whole").isTrue();
        assertThat(entitlements.check(new EntitlementCheckRequest(withAccess.getId(), null, productId, null)).allowed()).isTrue();
        assertThat(entitlements.check(new EntitlementCheckRequest(withAccess.getId(), org.getId(), productId, null)).allowed()).isTrue();
        var denied = entitlements.check(new EntitlementCheckRequest(withoutAccess.getId(), org.getId(), productId, null));
        assertThat(denied.allowed()).isFalse();
        assertThat(denied.reason()).isEqualTo("NO_MEMBER_ACCESS");
        assertThat(entitlements.check(new EntitlementCheckRequest(outsider.getId(), org.getId(), productId, null)).allowed()).isFalse();
        assertThat(entitlements.forCustomer(withAccess.getId())).extracting(EntitlementDto::ownerType).containsExactly("ORGANIZATION");
        assertThat(entitlements.forCustomer(withoutAccess.getId())).isEmpty();
        assertThat(entitlements.forOrganization(org.getId())).hasSize(1);
    }

    @Test
    void anAdminLookupOfAnUnknownCustomerOrOrganizationIsNotFound() {
        assertThatThrownBy(() -> entitlements.adminLookupCustomer(1L, 987654321L)).isInstanceOf(com.vyoog.eisplatform.common.exception.ResourceNotFoundException.class);
        assertThatThrownBy(() -> entitlements.adminLookupOrganization(1L, 987654321L)).isInstanceOf(com.vyoog.eisplatform.common.exception.ResourceNotFoundException.class);
    }
}
