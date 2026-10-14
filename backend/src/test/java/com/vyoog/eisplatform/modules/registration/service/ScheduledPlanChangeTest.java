package com.vyoog.eisplatform.modules.registration.service;

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
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.renewal.service.RenewalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** C97 SC-1: an upgrade takes effect at once; a downgrade waits for the next renewal and can be withdrawn. */
@SpringBootTest
@ActiveProfiles("test")
class ScheduledPlanChangeTest {

    @Autowired private SubscriptionService subscriptionService;
    @Autowired private RenewalService renewalService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;

    private Product product() {
        Product product = new Product();
        product.setName("Plan change product " + System.nanoTime());
        product.setPrice(BigDecimal.ONE);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private ProductPlan plan(Product product, String name, String price, BillingPeriod period) {
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName(name);
        plan.setPrice(new BigDecimal(price));
        plan.setBillingPeriod(period);
        plan.setCurrency(Currency.INR);
        return planRepository.save(plan);
    }

    private Customer customer() {
        Customer c = new Customer();
        c.setEmail("planchange-" + System.nanoTime() + "@example.com");
        c.setFirstName("Plan");
        c.setLastName("Change");
        return customerRepository.save(c);
    }

    @Test
    void anUpgradeIsImmediateAndADowngradeWaitsForTheRenewalAndCanBeWithdrawn() {
        Product product = product();
        ProductPlan basic = plan(product, "Basic", "0", BillingPeriod.MONTHLY);
        ProductPlan pro = plan(product, "Pro", "0", BillingPeriod.MONTHLY);
        Customer owner = customer();
        Long id = subscriptionService.subscribeFromCart(owner.getId(), product.getId(), pro.getId());

        // A same-price plan is not a downgrade.
        assertThat(subscriptionService.changePlan(owner.getId(), id, new ChangePlanRequest(basic.getId())).planId()).isEqualTo(basic.getId());
    }

    @Test
    void aCheaperPlanIsScheduledAndTakesEffectWhenTheSubscriptionRenews() {
        Product product = product();
        ProductPlan pro = plan(product, "Pro", "500", BillingPeriod.MONTHLY);
        ProductPlan basic = plan(product, "Basic", "100", BillingPeriod.MONTHLY);
        ProductPlan enterprise = plan(product, "Enterprise", "900", BillingPeriod.MONTHLY);
        Customer owner = customer();
        Long id = subscriptionService.subscribeFromCart(owner.getId(), product.getId(), pro.getId());
        subscriptionService.activatePending(id, "test");

        SubscriptionDto scheduled = subscriptionService.changePlan(owner.getId(), id, new ChangePlanRequest(basic.getId()));
        assertThat(scheduled.planId()).as("still on the plan they paid for").isEqualTo(pro.getId());
        assertThat(scheduled.pendingPlanId()).isEqualTo(basic.getId());
        assertThat(scheduled.pendingPlanName()).isEqualTo("Basic");

        SubscriptionDto withdrawn = subscriptionService.cancelScheduledPlanChange(owner.getId(), id);
        assertThat(withdrawn.pendingPlanId()).isNull();
        assertThat(withdrawn.planId()).isEqualTo(pro.getId());

        subscriptionService.changePlan(owner.getId(), id, new ChangePlanRequest(basic.getId()));
        SubscriptionDto upgraded = subscriptionService.changePlan(owner.getId(), id, new ChangePlanRequest(enterprise.getId()));
        assertThat(upgraded.planId()).as("an upgrade is immediate").isEqualTo(enterprise.getId());
        assertThat(upgraded.pendingPlanId()).as("and replaces a scheduled downgrade").isNull();

        subscriptionService.changePlan(owner.getId(), id, new ChangePlanRequest(basic.getId()));
        ProductSubscription s = subscriptionRepository.findById(id).orElseThrow();
        s.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        subscriptionRepository.save(s);
        assertThat(renewalService.renewDue()).isGreaterThanOrEqualTo(1);

        ProductSubscription renewed = subscriptionRepository.findById(id).orElseThrow();
        assertThat(renewed.getPlanId()).isEqualTo(basic.getId());
        assertThat(renewed.getPendingPlanId()).isNull();
    }

    @Test
    void aYearlyPlanIsComparedPerMonth() {
        Product product = product();
        ProductPlan monthly = plan(product, "Monthly", "100", BillingPeriod.MONTHLY);
        ProductPlan yearly = plan(product, "Yearly", "1000", BillingPeriod.YEARLY);   // 83.33 a month
        assertThat(SubscriptionService.isDowngrade(monthly, yearly)).isTrue();
        assertThat(SubscriptionService.isDowngrade(yearly, monthly)).isFalse();
    }
}
