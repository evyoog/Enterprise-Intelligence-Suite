package com.vyoog.eisplatform.modules.evaluation.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.evaluation.dto.EvaluationDtos;
import com.vyoog.eisplatform.modules.notification.model.Notification;
import com.vyoog.eisplatform.modules.notification.repository.NotificationRepository;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import com.vyoog.eisplatform.modules.serviceinstance.repository.ServiceInstanceRepository;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C102 / REQ-MKT-005: free trials (TR-1), demo requests, and the compare page (TR-2). */
@SpringBootTest
@ActiveProfiles("test")
class TrialDemoCompareTest {

    @Autowired private TrialService trialService;
    @Autowired private DemoRequestService demoRequestService;
    @Autowired private CompareService compareService;
    @Autowired private SubscriptionService subscriptionService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private ServiceInstanceRepository instanceRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private NotificationRepository notificationRepository;

    private Product product(Integer trialDays) {
        Product p = new Product();
        p.setName("Trial-" + System.nanoTime());
        p.setPrice(BigDecimal.TEN);
        p.setStatus(ProductStatus.ACTIVE);
        p.setTrialDays(trialDays);
        return productRepository.save(p);
    }

    private ProductPlan plan(Product product, String name, String price) {
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName(name);
        plan.setPrice(new BigDecimal(price));
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setCurrency(Currency.INR);
        return planRepository.save(plan);
    }

    private Customer customer() {
        Customer c = new Customer();
        c.setEmail("trial-" + System.nanoTime() + "@trial.example");
        c.setFirstName("Tri");
        c.setLastName("Al");
        return customerRepository.save(c);
    }

    private List<String> titles(Customer c) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(c.getId()).stream().map(Notification::getTitle).toList();
    }

    // ------------------------------------------------------------------ trials

    @Test
    void aTrialIsAnActiveSubscriptionOnTheHighestPlanThatEndsAfterTheTrialDaysWithoutRenewing() {
        Product p = product(7);
        plan(p, "Basic", "100.00");
        ProductPlan top = plan(p, "Enterprise", "900.00");
        Customer c = customer();

        SubscriptionDto trial = trialService.startForCustomer(c.getId(), p.getId());

        assertThat(trial.trial()).isTrue();
        assertThat(trial.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(trial.planId()).isEqualTo(top.getId());   // all features
        assertThat(trial.autoRenew()).isFalse();
        assertThat(Duration.between(Instant.now(), trial.expiresAt()).toDays()).isBetween(6L, 7L);
        // The product is told to provision (service instance + event), like any new subscription.
        assertThat(instanceRepository.findBySubscriptionId(trial.id())).isPresent();
        assertThat(titles(c)).anyMatch(t -> t.startsWith("Free trial started"));
    }

    @Test
    void oneTrialPerCustomerPerProductEverAndOnlyWhereTheProductOffersIt() {
        Product p = product(14);
        Product noTrial = product(null);
        Customer c = customer();
        trialService.startForCustomer(c.getId(), p.getId());

        assertThatThrownBy(() -> trialService.startForCustomer(c.getId(), p.getId())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already");
        assertThatThrownBy(() -> trialService.startForCustomer(c.getId(), noTrial.getId())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("does not offer");
        // Someone who once subscribed (even paid and cancelled) cannot start a trial either.
        Customer buyer = customer();
        subscriptionService.subscribe(buyer.getId(), p.getId());
        assertThatThrownBy(() -> trialService.startForCustomer(buyer.getId(), p.getId())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aTrialCannotBeRenewedAutoRenewedOrChangedToAnotherPlan() {
        Product p = product(7);
        ProductPlan top = plan(p, "Pro", "500.00");
        Customer c = customer();
        SubscriptionDto trial = trialService.startForCustomer(c.getId(), p.getId());

        assertThatThrownBy(() -> subscriptionService.renewSubscription(c.getId(), trial.id())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("trial");
        assertThatThrownBy(() -> subscriptionService.setAutoRenew(c.getId(), trial.id(), true)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("trial");
        assertThatThrownBy(() -> subscriptionService.changePlan(c.getId(), trial.id(), new com.vyoog.eisplatform.modules.registration.dto.ChangePlanRequest(top.getId())))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("trial");
    }

    @Test
    void aTrialThatEndsExpiresAtOnceWithNoGraceAndTheInstanceIsDeprovisioned() {
        Product p = product(7);
        Customer c = customer();
        SubscriptionDto trial = trialService.startForCustomer(c.getId(), p.getId());
        ProductSubscription stored = subscriptionRepository.findById(trial.id()).orElseThrow();
        stored.setExpiresAt(Instant.now().minus(Duration.ofMinutes(5)));
        subscriptionRepository.save(stored);

        subscriptionService.expireOverdueSubscriptions();

        ProductSubscription after = subscriptionRepository.findById(trial.id()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(after.getGraceEndsAt()).isNull();
        assertThat(instanceRepository.findBySubscriptionId(trial.id()).orElseThrow().getStatus().name()).isEqualTo("DEPROVISIONED");
        assertThat(titles(c)).anyMatch(t -> t.startsWith("Free trial ended"));
    }

    @Test
    void theOwnerIsRemindedOnceThreeDaysBeforeTheTrialEnds() {
        Product p = product(7);
        Customer c = customer();
        SubscriptionDto trial = trialService.startForCustomer(c.getId(), p.getId());
        ProductSubscription stored = subscriptionRepository.findById(trial.id()).orElseThrow();
        stored.setExpiresAt(Instant.now().plus(Duration.ofDays(2)));
        subscriptionRepository.save(stored);

        subscriptionService.remindEndingTrials();
        subscriptionService.remindEndingTrials();

        assertThat(titles(c).stream().filter(t -> t.contains("ends soon")).count()).isEqualTo(1);
    }

    @Test
    void buyingDuringATrialTurnsItIntoANormalSubscriptionThatStartsAfterPayment() {
        Product p = product(7);
        ProductPlan plan = plan(p, "Pro", "500.00");
        Customer c = customer();
        SubscriptionDto trial = trialService.startForCustomer(c.getId(), p.getId());

        subscriptionService.subscribeFromCart(c.getId(), p.getId(), plan.getId());

        ProductSubscription after = subscriptionRepository.findById(trial.id()).orElseThrow();
        assertThat(after.isTrial()).isFalse();
        assertThat(after.getStatus()).isEqualTo(SubscriptionStatus.PENDING_SUBSCRIPTION);   // paid plan: after payment
        assertThat(after.isAutoRenew()).isTrue();
    }

    @Test
    void anOrganizationTrialNeedsAnAdminOrManageBillingAndUsesTheOrganizationsSeats() {
        Product p = product(10);
        Organization org = new Organization();
        org.setName("Trial Org");
        org.setCode("TRL-" + System.nanoTime());
        org.setBusinessEmail("biz@trial.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        org = organizationRepository.save(org);
        Customer admin = customer();
        Customer member = customer();
        memberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        memberService.addMember(org.getId(), member.getId(), OrgRole.MEMBER);

        assertThatThrownBy(() -> trialService.startForOrganization(member.getId(), p.getId())).isInstanceOf(ForbiddenException.class);
        SubscriptionDto trial = trialService.startForOrganization(admin.getId(), p.getId());
        assertThat(trial.trial()).isTrue();
        assertThat(trial.quantity()).isEqualTo(5);
        assertThatThrownBy(() -> trialService.startForOrganization(admin.getId(), p.getId())).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------ demo request

    @Test
    void aDemoRequestIsATicketInTheDemoRequestCategoryWithSlaDeadlines() {
        Product p = product(null);
        Customer c = customer();

        SupportTicketDto ticket = demoRequestService.request(c.getId(), new EvaluationDtos.DemoRequest(p.getId(), "Next Tuesday afternoon", "We are 40 people."));

        assertThat(ticket.category()).isEqualTo("Demo request");
        assertThat(ticket.subject()).startsWith("Demo request: ").contains(p.getName());
        assertThat(ticket.description()).contains("Next Tuesday afternoon").contains("We are 40 people.");
        assertThat(ticket.firstResponseDueAt()).isNotNull();
        assertThatThrownBy(() -> demoRequestService.request(c.getId(), new EvaluationDtos.DemoRequest(null, null, null))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> demoRequestService.request(c.getId(), new EvaluationDtos.DemoRequest(-1L, null, null))).isInstanceOf(RuntimeException.class);
    }

    // ------------------------------------------------------------------ compare

    @Test
    void comparingPlansMultipliesThePriceBySeatsInEachItemsOwnCurrency() {
        Product a = product(null);
        Product b = product(14);
        ProductPlan aPro = plan(a, "Pro", "100.00");
        plan(a, "Lite", "40.00");
        ProductPlan bTeam = plan(b, "Team", "250.50");

        var result = compareService.compare(a.getId() + ":" + aPro.getId() + "," + b.getId() + ":" + bTeam.getId(), 3, null);

        assertThat(result.seats()).isEqualTo(3);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).plan().getName()).isEqualTo("Pro");
        assertThat(result.items().get(0).subtotal()).isEqualByComparingTo("300.00");
        assertThat(result.items().get(1).subtotal()).isEqualByComparingTo("751.50");
        assertThat(result.items().get(1).currency()).isEqualTo("INR");
        assertThat(result.items().get(1).trialDays()).isEqualTo(14);
        assertThat(result.items().get(0).tax().status()).isEqualTo("NOT_SIGNED_IN");
    }

    @Test
    void aProductOnItsOwnIsComparedAtItsEntryPlanAndAFlatProductAtItsPrice() {
        Product withPlans = product(null);
        plan(withPlans, "Pro", "100.00");
        plan(withPlans, "Lite", "40.00");
        Product flat = product(null);

        var result = compareService.compare(withPlans.getId() + "," + flat.getId(), null, null);

        assertThat(result.seats()).isEqualTo(1);
        assertThat(result.items().get(0).plan().getName()).isEqualTo("Lite");
        assertThat(result.items().get(0).unitPrice()).isEqualByComparingTo("40.00");
        assertThat(result.items().get(1).plan()).isNull();
        assertThat(result.items().get(1).unitPrice()).isEqualByComparingTo("10.00");
    }

    @Test
    void aSignedInCustomerWithoutBillingDetailsIsToldToAddThem() {
        Product a = product(null);
        Product b = product(null);
        Customer c = customer();

        var result = compareService.compare(a.getId() + "," + b.getId(), 1, c.getId());

        assertThat(result.items().get(0).tax().status()).isEqualTo("NEEDS_BILLING_DETAILS");
    }

    @Test
    void theItemsAreValidated() {
        Product a = product(null);
        Product b = product(null);
        Product other = product(null);
        ProductPlan foreign = plan(other, "Pro", "10.00");
        assertThatThrownBy(() -> compareService.compare(String.valueOf(a.getId()), 1, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> compareService.compare(a.getId() + "," + a.getId(), 1, null)).hasMessageContaining("twice");
        assertThatThrownBy(() -> compareService.compare(a.getId() + ",x", 1, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> compareService.compare(a.getId() + "," + b.getId() + "," + other.getId() + ",9,10", 1, null)).hasMessageContaining("2 to 4");
        assertThatThrownBy(() -> compareService.compare(a.getId() + "," + b.getId(), 0, null)).hasMessageContaining("Seats");
        assertThatThrownBy(() -> compareService.compare(a.getId() + ":" + foreign.getId() + "," + b.getId(), 1, null)).hasMessageContaining("does not belong");
        assertThatThrownBy(() -> compareService.compare(a.getId() + ",999999999", 1, null)).isInstanceOf(RuntimeException.class);
    }
}
