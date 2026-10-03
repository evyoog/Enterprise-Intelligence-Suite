package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.ChangePlanRequest;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
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

/**
 * 07.01 Subscription Lifecycle & Changes, 07.04 automatic expiry (sprint
 * 2026.4.3).
 */
@SpringBootTest
@ActiveProfiles("test")
class SubscriptionServiceTest {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductPlanRepository productPlanRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ProductSubscriptionRepository subscriptionRepository;
    @Autowired
    private com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository outboxEventRepository;

    private Product newProduct() {
        Product product = new Product();
        product.setName("Test Product " + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private ProductPlan newPlan(Product product, BillingPeriod period) {
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName("Plan " + period);
        plan.setPrice(BigDecimal.ONE);
        plan.setBillingPeriod(period);
        plan.setCurrency(Currency.USD);
        return productPlanRepository.save(plan);
    }

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("sub-test-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    private ProductSubscription activeSubscription(Long customerId, Long productId) {
        return subscriptionRepository.findByOwnerCustomerIdAndProductId(customerId, productId)
            .orElseThrow();
    }

    @Test
    void suspendThenReactivateRoundTrips() {
        Product product = newProduct();
        Long customerId = newCustomer().getId();
        subscriptionService.subscribe(customerId, product.getId());
        Long subscriptionId = activeSubscription(customerId, product.getId()).getId();

        SubscriptionDto suspended = subscriptionService.suspendSubscription(customerId, subscriptionId);
        assertThat(suspended.status()).isEqualTo(SubscriptionStatus.SUSPENDED);

        assertThatThrownBy(() -> subscriptionService.suspendSubscription(customerId, subscriptionId))
            .isInstanceOf(IllegalArgumentException.class);

        SubscriptionDto reactivated = subscriptionService.reactivateSubscription(customerId, subscriptionId);
        assertThat(reactivated.status()).isEqualTo(SubscriptionStatus.ACTIVE);

        assertThatThrownBy(() -> subscriptionService.reactivateSubscription(customerId, subscriptionId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelIsOneWay() {
        Product product = newProduct();
        Long customerId = newCustomer().getId();
        subscriptionService.subscribe(customerId, product.getId());
        Long subscriptionId = activeSubscription(customerId, product.getId()).getId();

        SubscriptionDto cancelled = subscriptionService.cancelSubscription(customerId, subscriptionId);
        assertThat(cancelled.status()).isEqualTo(SubscriptionStatus.CANCELLED);

        assertThatThrownBy(() -> subscriptionService.cancelSubscription(customerId, subscriptionId))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> subscriptionService.reactivateSubscription(customerId, subscriptionId))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> subscriptionService.renewSubscription(customerId, subscriptionId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anotherCustomersSubscriptionIsInvisible() {
        Product product = newProduct();
        Long ownerId = newCustomer().getId();
        Long strangerId = newCustomer().getId();
        subscriptionService.subscribe(ownerId, product.getId());
        Long subscriptionId = activeSubscription(ownerId, product.getId()).getId();

        assertThatThrownBy(() -> subscriptionService.suspendSubscription(strangerId, subscriptionId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePlanValidatesPlanBelongsToSameProduct() {
        Product product = newProduct();
        Product otherProduct = newProduct();
        ProductPlan plan = newPlan(product, BillingPeriod.MONTHLY);
        ProductPlan foreignPlan = newPlan(otherProduct, BillingPeriod.MONTHLY);
        Long customerId = newCustomer().getId();
        subscriptionService.subscribe(customerId, product.getId());
        Long subscriptionId = activeSubscription(customerId, product.getId()).getId();

        SubscriptionDto changed = subscriptionService.changePlan(customerId, subscriptionId,
            new ChangePlanRequest(plan.getId()));
        assertThat(changed.planId()).isEqualTo(plan.getId());
        assertThat(changed.planName()).isEqualTo(plan.getName());

        assertThatThrownBy(() -> subscriptionService.changePlan(customerId, subscriptionId,
            new ChangePlanRequest(foreignPlan.getId())))
            .isInstanceOf(IllegalArgumentException.class);

        SubscriptionDto cleared = subscriptionService.changePlan(customerId, subscriptionId, new ChangePlanRequest(null));
        assertThat(cleared.planId()).isNull();
    }

    @Test
    void renewExtendsFromMonthlyPlanAndReactivatesExpired() {
        Product product = newProduct();
        ProductPlan plan = newPlan(product, BillingPeriod.MONTHLY);
        Long customerId = newCustomer().getId();
        subscriptionService.subscribe(customerId, product.getId());
        ProductSubscription subscription = activeSubscription(customerId, product.getId());
        subscriptionService.changePlan(customerId, subscription.getId(), new ChangePlanRequest(plan.getId()));

        // Simulate the expiry job having already caught up with this subscription.
        subscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        subscriptionRepository.save(subscription);

        SubscriptionDto renewed = subscriptionService.renewSubscription(customerId, subscription.getId());
        assertThat(renewed.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(renewed.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void renewRefusedWithNoBillingPeriodToRenew() {
        Product product = newProduct();
        Long customerId = newCustomer().getId();
        subscriptionService.subscribe(customerId, product.getId());
        Long subscriptionId = activeSubscription(customerId, product.getId()).getId();

        assertThatThrownBy(() -> subscriptionService.renewSubscription(customerId, subscriptionId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expiryJobFlipsOverdueActiveSubscriptionsOnly() {
        Product product = newProduct();
        Long customerId = newCustomer().getId();
        subscriptionService.subscribe(customerId, product.getId());
        ProductSubscription overdue = activeSubscription(customerId, product.getId());
        overdue.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        subscriptionRepository.save(overdue);

        Product futureProduct = newProduct();
        subscriptionService.subscribe(customerId, futureProduct.getId());
        ProductSubscription notYetDue = activeSubscription(customerId, futureProduct.getId());
        notYetDue.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
        subscriptionRepository.save(notYetDue);

        int expiredCount = subscriptionService.expireOverdueSubscriptions();
        assertThat(expiredCount).isGreaterThanOrEqualTo(1);

        assertThat(subscriptionRepository.findById(overdue.getId()).orElseThrow().getStatus())
            .isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(subscriptionRepository.findById(notYetDue.getId()).orElseThrow().getStatus())
            .isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void organizationOwnedSubscriptionCannotBeActedOnViaMeEndpoints() {
        Product product = newProduct();
        Organization organization = new Organization();
        organization.setName("Test Org " + System.nanoTime());
        organization.setCode("SUBTEST-" + System.nanoTime());
        organization.setBusinessEmail("biz@test.example");
        organization.setCountry("India");
        organization.setLicensedSeats(5);
        organization = organizationRepository.save(organization);

        ProductSubscription toSave = new ProductSubscription();
        toSave.setProductId(product.getId());
        toSave.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        toSave.setOwnerOrganizationId(organization.getId());
        toSave.setStatus(SubscriptionStatus.ACTIVE);
        Long orgSubscriptionId = subscriptionRepository.save(toSave).getId();

        Long someCustomerId = newCustomer().getId();
        assertThatThrownBy(() -> subscriptionService.suspendSubscription(someCustomerId, orgSubscriptionId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    /** REQ-INT-002.8 (C62): lifecycle changes publish their events in the same transaction. */
    @Test
    void lifecycleChangesPublishPlatformEvents() {
        Product product = newProduct();
        Long customerId = newCustomer().getId();
        Long subscriptionId = subscriptionService.subscribe(customerId, product.getId()).id();
        subscriptionService.suspendSubscription(customerId, subscriptionId);
        subscriptionService.reactivateSubscription(customerId, subscriptionId);
        subscriptionService.cancelSubscription(customerId, subscriptionId);

        var events = outboxEventRepository.findByAggregateTypeAndAggregateIdOrderByIdAsc("Subscription", subscriptionId.toString());
        assertThat(events).extracting(e -> e.getEventType())
            .containsExactly("SubscriptionCreated", "SubscriptionSuspended", "SubscriptionResumed", "SubscriptionCancelled");
        assertThat(events.get(0).getPayload()).contains("\"subscriptionId\":" + subscriptionId, "\"status\":\"ACTIVE\"");
    }
}
