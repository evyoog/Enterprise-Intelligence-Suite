package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import com.vyoog.eisplatform.modules.preference.model.CustomerPreference;
import com.vyoog.eisplatform.modules.preference.repository.CustomerPreferenceRepository;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.renewal.dto.SaveRenewalReminderPreferenceRequest;
import com.vyoog.eisplatform.modules.renewal.repository.RenewalReminderLogRepository;
import com.vyoog.eisplatform.modules.renewal.service.RenewalReminderService;
import com.vyoog.eisplatform.modules.renewal.service.RenewalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-SUB-004 (C64): renewal date, auto-renewal and daily renewal
 * reminders in the recipient's time zone. TC-SUB-018..023. Lives in this
 * package because it builds catalog plans (LayeredArchitectureTest). */
@SpringBootTest
@ActiveProfiles("test")
class RenewalAndRemindersTest {

    /** A renewal at 10:00 IST (04:30 UTC). */
    private static final Instant RENEWAL = Instant.parse("2030-01-10T04:30:00Z");

    @Autowired private SubscriptionService subscriptionService;
    @Autowired private RenewalService renewalService;
    @Autowired private RenewalReminderService reminderService;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private CustomerPreferenceRepository customerPreferenceRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private RenewalReminderLogRepository logRepository;

    private ProductPlan monthlyPlan(String price) {
        Product product = new Product();
        product.setName("Renewing product " + System.nanoTime());
        product.setPrice(new BigDecimal(price));
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName("Monthly");
        plan.setPrice(new BigDecimal(price));
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setCurrency(Currency.INR);
        return planRepository.save(plan);
    }

    private Customer newCustomer(String timeZone) {
        Customer customer = new Customer();
        customer.setEmail("renewal-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Renewal");
        customer.setLastName("Tester");
        customer = customerRepository.save(customer);
        CustomerPreference preference = new CustomerPreference();
        preference.setCustomerId(customer.getId());
        preference.setTimeZone(timeZone);
        customerPreferenceRepository.save(preference);
        return customer;
    }

    /** An individual subscription renewing at {@link #RENEWAL}. */
    private ProductSubscription renewingAt(Customer owner, Instant renewal) {
        ProductPlan plan = monthlyPlan("0");
        Long id = subscriptionService.subscribeFromCart(owner.getId(), plan.getProduct().getId(), plan.getId());
        ProductSubscription subscription = subscriptionRepository.findById(id).orElseThrow();
        subscription.setExpiresAt(renewal);
        return subscriptionRepository.save(subscription);
    }

    private long logsFor(ProductSubscription s) {
        return logRepository.findBySubscriptionIdOrderBySentAtAsc(s.getId()).size();
    }

    @Test
    void aNewMonthlySubscriptionAutoRenewsAndHasARenewalDate() {
        ProductPlan plan = monthlyPlan("499");
        Customer owner = newCustomer("Asia/Kolkata");
        Long id = subscriptionService.subscribeFromCart(owner.getId(), plan.getProduct().getId(), plan.getId());
        ProductSubscription s = subscriptionRepository.findById(id).orElseThrow();
        assertThat(s.isAutoRenew()).isTrue();
        // BR-SUB-010: the end is 23:59:00.000 (+05:30) on the last day of the term, never the arbitrary moment 30 days after the start
        assertThat(s.getExpiresAt()).isEqualTo(SubscriptionClock.endOfDay(s.getStartedAt().plus(Duration.ofDays(30))));
        assertThat(SubscriptionClock.format(s.getExpiresAt())).endsWith("T23:59:00.000+05:30");
    }

    @Test
    void aDueSubscriptionIsRenewedWithARenewalInvoiceAndNotExpired() {
        ProductPlan plan = monthlyPlan("499");
        Customer owner = newCustomer("Asia/Kolkata");
        Long id = subscriptionService.subscribeFromCart(owner.getId(), plan.getProduct().getId(), plan.getId());
        ProductSubscription s = subscriptionRepository.findById(id).orElseThrow();
        Instant oldRenewal = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        s.setExpiresAt(oldRenewal);
        subscriptionRepository.save(s);

        subscriptionService.expireOverdueSubscriptions();
        assertThat(subscriptionRepository.findById(id).orElseThrow().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        assertThat(renewalService.renewDue()).isGreaterThanOrEqualTo(1);
        ProductSubscription renewed = subscriptionRepository.findById(id).orElseThrow();
        assertThat(renewed.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(renewed.getExpiresAt()).isEqualTo(SubscriptionClock.endOfDay(oldRenewal.plus(30, ChronoUnit.DAYS)));
        assertThat(SubscriptionClock.format(renewed.getExpiresAt())).endsWith("T23:59:00.000+05:30");
        assertThat(invoiceRepository.findBySubscriptionIdOrderByIssuedAtDescIdDesc(id)).hasSize(1);
        assertThat(outboxEventRepository.findByAggregateTypeAndAggregateIdOrderByIdAsc("Subscription", id.toString()))
            .extracting(e -> e.getEventType()).contains("SubscriptionRenewed");
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "SUBSCRIPTION_AUTO_RENEWED".equals(a.getAction()) && id.toString().equals(a.getTargetId()));
        assertThat(renewalService.canChargeAutomatically(renewed)).isFalse();

        // renewed once per term: running again does nothing for it
        renewalService.renewDue();
        assertThat(subscriptionRepository.findById(id).orElseThrow().getExpiresAt())
            .isEqualTo(SubscriptionClock.endOfDay(oldRenewal.plus(30, ChronoUnit.DAYS)));
    }

    @Test
    void aReminderIsSentOncePerDayAtTheSendTimeInTheRecipientsTimeZone() {
        Customer owner = newCustomer("Asia/Kolkata");
        reminderService.savePreferences(owner.getId(), new SaveRenewalReminderPreferenceRequest(true, 7, "09:00"));
        ProductSubscription s = renewingAt(owner, RENEWAL);

        // 08:30 IST, 7 days before: before the send time
        reminderService.sendDue(RENEWAL.minus(7, ChronoUnit.DAYS).minus(90, ChronoUnit.MINUTES));
        assertThat(logsFor(s)).isZero();
        // 09:30 IST, 7 days before: sent
        reminderService.sendDue(RENEWAL.minus(7, ChronoUnit.DAYS).minus(30, ChronoUnit.MINUTES));
        assertThat(logsFor(s)).isEqualTo(1);
        // later the same day: not again
        reminderService.sendDue(RENEWAL.minus(7, ChronoUnit.DAYS).plus(5, ChronoUnit.HOURS));
        assertThat(logsFor(s)).isEqualTo(1);
        // the next day: again (daily)
        reminderService.sendDue(RENEWAL.minus(6, ChronoUnit.DAYS));
        assertThat(logsFor(s)).isEqualTo(2);
        // 8 days before is outside the window; the renewal day itself too
        reminderService.sendDue(RENEWAL.minus(8, ChronoUnit.DAYS));
        reminderService.sendDue(RENEWAL.minus(1, ChronoUnit.HOURS));
        assertThat(logsFor(s)).isEqualTo(2);

        assertThat(outboxEventRepository.findByAggregateTypeAndAggregateIdOrderByIdAsc("Subscription", s.getId().toString()))
            .extracting(e -> e.getEventType()).filteredOn("RenewalReminderSent"::equals).hasSize(2);
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "RENEWAL_REMINDER_SENT".equals(a.getAction()) && s.getId().toString().equals(a.getTargetId()));
    }

    @Test
    void theSameInstantIsADifferentLocalDayInAnotherTimeZone() {
        // 23:30 UTC on 2 Jan = 05:00 on 3 Jan in Kolkata; in New York it is still 2 Jan.
        Customer india = newCustomer("Asia/Kolkata");
        Customer newYork = newCustomer("America/New_York");
        reminderService.savePreferences(india.getId(), new SaveRenewalReminderPreferenceRequest(true, 7, "04:00"));
        reminderService.savePreferences(newYork.getId(), new SaveRenewalReminderPreferenceRequest(true, 7, "04:00"));
        ProductSubscription indian = renewingAt(india, RENEWAL);
        ProductSubscription american = renewingAt(newYork, RENEWAL);

        reminderService.sendDue(Instant.parse("2030-01-02T23:30:00Z"));
        assertThat(logsFor(indian)).isEqualTo(1);
        // New York: 18:30 on 2 Jan; the renewal is 9 Jan locally (23:30 on 9 Jan), 7 days away, time passed — sent
        assertThat(logsFor(american)).isEqualTo(1);
        assertThat(logRepository.findBySubscriptionIdOrderBySentAtAsc(indian.getId()).get(0).getLocalDate()).hasToString("2030-01-03");
        assertThat(logRepository.findBySubscriptionIdOrderBySentAtAsc(american.getId()).get(0).getLocalDate()).hasToString("2030-01-02");
    }

    @Test
    void usersCanTurnRemindersOffOrChooseTheirOwnDays() {
        Customer off = newCustomer("Asia/Kolkata");
        Customer threeDays = newCustomer("Asia/Kolkata");
        reminderService.savePreferences(off.getId(), new SaveRenewalReminderPreferenceRequest(false, null, null));
        reminderService.savePreferences(threeDays.getId(), new SaveRenewalReminderPreferenceRequest(true, 3, "09:00"));
        ProductSubscription s1 = renewingAt(off, RENEWAL);
        ProductSubscription s2 = renewingAt(threeDays, RENEWAL);

        reminderService.sendDue(RENEWAL.minus(5, ChronoUnit.DAYS));
        assertThat(logsFor(s1)).isZero();
        assertThat(logsFor(s2)).isZero();
        reminderService.sendDue(RENEWAL.minus(3, ChronoUnit.DAYS));
        assertThat(logsFor(s2)).isEqualTo(1);
        // turning reminders off never turns auto-renew off
        assertThat(subscriptionRepository.findById(s1.getId()).orElseThrow().isAutoRenew()).isTrue();

        var prefs = reminderService.preferences(threeDays.getId());
        assertThat(prefs.effectiveDaysBefore()).isEqualTo(3);
        assertThat(prefs.effectiveTimeZone()).isEqualTo("Asia/Kolkata");
        assertThatThrownBy(() -> reminderService.savePreferences(threeDays.getId(), new SaveRenewalReminderPreferenceRequest(true, 31, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void organizationAdminsReceiveRemindersForOrganizationSubscriptions() {
        Organization org = new Organization();
        org.setName("Renewal Org");
        org.setCode("REN-" + System.nanoTime());
        org.setBusinessEmail("renewal@test-org.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        org = organizationRepository.save(org);
        Customer admin = newCustomer("Asia/Kolkata");
        Customer member = newCustomer("Asia/Kolkata");
        memberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        memberService.addMember(org.getId(), member.getId(), OrgRole.MEMBER);
        reminderService.savePreferences(admin.getId(), new SaveRenewalReminderPreferenceRequest(true, 7, "09:00"));

        ProductPlan plan = monthlyPlan("0");
        Long id = subscriptionService.subscribeOrganization(org.getId(), plan.getProduct().getId(), plan.getId()).id();
        ProductSubscription s = subscriptionRepository.findById(id).orElseThrow();
        s.setExpiresAt(RENEWAL);
        subscriptionRepository.save(s);

        reminderService.sendDue(RENEWAL.minus(2, ChronoUnit.DAYS));
        assertThat(logRepository.findBySubscriptionIdOrderBySentAtAsc(id))
            .extracting(l -> l.getRecipientCustomerId()).containsExactly(admin.getId());
    }

    @Test
    void myRenewalsShowTheNextReminder() {
        Customer owner = newCustomer("Asia/Kolkata");
        reminderService.savePreferences(owner.getId(), new SaveRenewalReminderPreferenceRequest(true, 7, "08:30"));
        ProductSubscription s = renewingAt(owner, RENEWAL);

        var renewals = reminderService.myRenewals(owner.getId(), RENEWAL.minus(10, ChronoUnit.DAYS));
        var mine = renewals.stream().filter(r -> r.subscriptionId().equals(s.getId())).findFirst().orElseThrow();
        assertThat(mine.autoRenew()).isTrue();
        assertThat(mine.remindersEnabled()).isTrue();
        assertThat(mine.nextReminderAt()).isEqualTo("2030-01-03T08:30+05:30");
    }
}
