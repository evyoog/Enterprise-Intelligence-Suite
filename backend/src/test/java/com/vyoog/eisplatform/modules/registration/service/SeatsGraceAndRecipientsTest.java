package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.modules.access.service.AccessManagementService;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.notification.repository.NotificationRepository;
import com.vyoog.eisplatform.modules.preference.model.CustomerPreference;
import com.vyoog.eisplatform.modules.preference.repository.CustomerPreferenceRepository;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.EntitlementCheckRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.renewal.dto.SaveReminderRecipientsRequest;
import com.vyoog.eisplatform.modules.renewal.repository.RenewalReminderLogRepository;
import com.vyoog.eisplatform.modules.renewal.service.RenewalReminderRecipientService;
import com.vyoog.eisplatform.modules.renewal.service.RenewalReminderService;
import com.vyoog.eisplatform.modules.renewal.service.RenewalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * C100 (sprint 2026.4.3 answers): SE-1 B seats (charged now for the rest of the term, decrease at renewal, Manage billing), RR-1 B (auto-renew off, a
 * read-only grace period, then expiry), RR-2 B / RR-5 (reminder recipients and their channels), RR-4 A (reminders run to the end of the local grace day).
 * Lives in this package because it builds catalog plans (LayeredArchitectureTest).
 */
@SpringBootTest
@ActiveProfiles("test")
class SeatsGraceAndRecipientsTest {

    @Autowired private SubscriptionSeatService seatService;
    @Autowired private SubscriptionService subscriptionService;
    @Autowired private RenewalService renewalService;
    @Autowired private RenewalReminderService reminderService;
    @Autowired private RenewalReminderRecipientService recipientService;
    @Autowired private EntitlementService entitlements;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private AccessManagementService accessService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private CustomerPreferenceRepository customerPreferenceRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private RenewalReminderLogRepository logRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    private ProductPlan monthlyPlan(String price) {
        Product product = new Product();
        product.setName("C100 product " + System.nanoTime());
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

    private Organization newOrganization(int seats) {
        Organization organization = new Organization();
        organization.setName("C100 Org");
        organization.setCode("C100-" + System.nanoTime());
        organization.setBusinessEmail("c100@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(seats);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String timeZone) {
        Customer customer = new Customer();
        customer.setEmail("c100-" + System.nanoTime() + "@test.example");
        customer.setFirstName("C100");
        customer.setLastName("Tester");
        customer = customerRepository.save(customer);
        if (timeZone != null) {
            CustomerPreference preference = new CustomerPreference();
            preference.setCustomerId(customer.getId());
            preference.setTimeZone(timeZone);
            customerPreferenceRepository.save(preference);
        }
        return customer;
    }

    private Long newMember(Organization org, OrgRole role, String timeZone) {
        Customer customer = newCustomer(timeZone);
        memberService.addMember(org.getId(), customer.getId(), role);
        return customer.getId();
    }

    /** An organization subscription on a Monthly plan priced 1000 per seat, with the term ending {@code daysLeft} days from now. */
    private ProductSubscription orgSubscription(Organization org, int daysLeft) {
        ProductPlan plan = monthlyPlan("1000");
        Long id = subscriptionService.subscribeOrganization(org.getId(), plan.getProduct().getId(), plan.getId()).id();
        ProductSubscription s = subscriptionRepository.findById(id).orElseThrow();
        s.setExpiresAt(Instant.now().plus(daysLeft, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES));
        return subscriptionRepository.save(s);
    }

    private Invoice latestInvoice(Long subscriptionId) {
        return invoiceRepository.findBySubscriptionIdOrderByIssuedAtDescIdDesc(subscriptionId).stream()
            .max(java.util.Comparator.comparing(Invoice::getId)).orElseThrow();
    }

    private List<com.vyoog.eisplatform.modules.billing.model.InvoiceLine> linesOf(Invoice invoice) {
        return new TransactionTemplate(transactionManager).execute(status ->
            List.copyOf(invoiceRepository.findById(invoice.getId()).orElseThrow().getLines()));
    }

    private long invoiceCount(Long subscriptionId) {
        return invoiceRepository.findBySubscriptionIdOrderByIssuedAtDescIdDesc(subscriptionId).size();
    }

    // ------------------------------------------------------------------ SE-1 B, SE-2 A

    @Test
    void anIncreaseIsEffectiveAtOnceAndChargedForTheRestOfTheTerm() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN, null);
        newMember(org, OrgRole.MEMBER, null);
        ProductSubscription subscription = orgSubscription(org, 15);
        var before = seatService.summary(adminId, subscription.getId());
        assertThat(before.unitAmount()).as("the plan price is per seat: 1000 in minor units").isEqualTo(100_000);
        assertThat(before.termDays()).isEqualTo(30);

        var changed = seatService.changeSeats(adminId, subscription.getId(), 8);

        assertThat(changed.effect()).isEqualTo("NOW");
        assertThat(changed.quantity()).isEqualTo(8);
        assertThat(changed.pendingQuantity()).isNull();
        assertThat(changed.invoiceId()).isNotNull();
        Invoice invoice = invoiceRepository.findById(changed.invoiceId()).orElseThrow();
        int basisPoints = Math.min(before.remainingDays(), 30) * 10_000 / 30;
        long expected = BigDecimal.valueOf(3 * 100_000L).multiply(BigDecimal.valueOf(basisPoints)).divide(BigDecimal.valueOf(10_000), 0, java.math.RoundingMode.HALF_UP).longValueExact();
        assertThat(invoice.getSubtotal()).as("3 added seats x the price x the days left / 30").isEqualTo(expected);
        var lines = linesOf(invoice);
        assertThat(lines).singleElement().satisfies(line -> {
            assertThat(line.getDescription()).startsWith("Added seats");
            assertThat(line.getQuantity()).isEqualTo(3);
        });
    }

    @Test
    void aDecreaseWaitsForTheNextRenewalWithNoCreditAndCanBeWithdrawn() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN, null);
        ProductSubscription subscription = orgSubscription(org, 15);
        long invoices = invoiceCount(subscription.getId());

        var scheduled = seatService.changeSeats(adminId, subscription.getId(), 3);

        assertThat(scheduled.effect()).isEqualTo("AT_RENEWAL");
        assertThat(scheduled.quantity()).as("seats stay until the renewal").isEqualTo(5);
        assertThat(scheduled.pendingQuantity()).isEqualTo(3);
        assertThat(invoiceCount(subscription.getId())).as("no invoice and no credit").isEqualTo(invoices);
        assertThat(seatService.summary(adminId, subscription.getId()).pendingQuantity()).isEqualTo(3);

        var withdrawn = seatService.changeSeats(adminId, subscription.getId(), 5);
        assertThat(withdrawn.effect()).isEqualTo("WITHDRAWN");
        assertThat(withdrawn.pendingQuantity()).isNull();
        assertThat(seatService.changeSeats(adminId, subscription.getId(), 5).effect()).isEqualTo("UNCHANGED");
    }

    @Test
    void theScheduledDecreaseTakesEffectWithTheRenewalAndIsBilledForFewerSeats() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN, null);
        ProductSubscription subscription = orgSubscription(org, 15);
        seatService.changeSeats(adminId, subscription.getId(), 3);

        ProductSubscription due = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        due.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        subscriptionRepository.save(due);
        assertThat(renewalService.renewDue()).isGreaterThanOrEqualTo(1);

        ProductSubscription renewed = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(renewed.getQuantity()).isEqualTo(3);
        assertThat(renewed.getPendingQuantity()).isNull();
        Invoice renewal = latestInvoice(subscription.getId());
        assertThat(renewal.getSubtotal()).as("3 seats x 1000").isEqualTo(300_000);
        assertThat(linesOf(renewal).get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void aScheduledDecreaseNeverGoesBelowTheSeatsInUseWhenItIsApplied() {
        Organization org = newOrganization(6);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN, null);
        ProductSubscription subscription = orgSubscription(org, 15);
        seatService.changeSeats(adminId, subscription.getId(), 2);   // 1 seat in use now
        newMember(org, OrgRole.MEMBER, null);
        newMember(org, OrgRole.MEMBER, null);
        newMember(org, OrgRole.MEMBER, null);                         // 4 members use a seat of this product by the renewal

        ProductSubscription due = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        due.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        subscriptionRepository.save(due);
        renewalService.renewDue();

        assertThat(subscriptionRepository.findById(subscription.getId()).orElseThrow().getQuantity())
            .as("the renewal keeps the seats in use").isGreaterThanOrEqualTo(4);
    }

    @Test
    void seatsAreChangedWithManageBillingAndManageSubscriptionsCanOnlyLook() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN, null);
        Long plainId = newMember(org, OrgRole.MEMBER, null);
        Long subsOnlyId = newMember(org, OrgRole.MEMBER, null);
        Long billingId = newMember(org, OrgRole.MEMBER, null);
        ProductSubscription subscription = orgSubscription(org, 15);
        grant(org, adminId, subsOnlyId, "MANAGE_SUBSCRIPTIONS");
        grant(org, adminId, billingId, "MANAGE_ORGANIZATION_BILLING");

        assertThatThrownBy(() -> seatService.summary(plainId, subscription.getId())).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> seatService.changeSeats(plainId, subscription.getId(), 6)).isInstanceOf(ForbiddenException.class);
        assertThat(seatService.summary(subsOnlyId, subscription.getId()).quantity()).as("Manage subscriptions may look").isEqualTo(5);
        assertThatThrownBy(() -> seatService.changeSeats(subsOnlyId, subscription.getId(), 6)).isInstanceOf(ForbiddenException.class);
        assertThat(seatService.changeSeats(billingId, subscription.getId(), 6).quantity()).as("Manage billing changes seats").isEqualTo(6);
    }

    private void grant(Organization org, Long adminCustomerId, Long customerId, String permission) {
        OrganizationMember member = memberRepository.findByOrganizationIdAndCustomerId(org.getId(), customerId).orElseThrow();
        accessService.setOverrides(adminCustomerId, member.getId(), new com.vyoog.eisplatform.modules.access.dto.AccessDtos.SetOverridesRequest(Map.of(permission, true), null, null));
    }

    @Test
    void seatsCannotChangeOnceTheTermHasEnded() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN, null);
        ProductSubscription subscription = orgSubscription(org, 15);
        ProductSubscription ended = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        ended.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        subscriptionRepository.save(ended);

        assertThatThrownBy(() -> seatService.changeSeats(adminId, subscription.getId(), 6))
            .isInstanceOf(InvalidStateException.class).hasMessageContaining("term has ended");
    }

    // ------------------------------------------------------------------ RR-1 B

    private ProductSubscription individualSubscription(Customer owner) {
        ProductPlan plan = monthlyPlan("0");
        Long id = subscriptionService.subscribeFromCart(owner.getId(), plan.getProduct().getId(), plan.getId());
        return subscriptionRepository.findById(id).orElseThrow();
    }

    @Test
    void anEndedSubscriptionWithAutoRenewOffIsReadOnlyForSevenDaysThenExpires() {
        Customer owner = newCustomer("Asia/Kolkata");
        ProductSubscription subscription = individualSubscription(owner);
        Long productId = subscription.getProductId();
        subscriptionService.setAutoRenew(owner.getId(), subscription.getId(), false);
        ProductSubscription ended = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(ended.isAutoRenew()).isFalse();
        ended.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        subscriptionRepository.save(ended);

        subscriptionService.expireOverdueSubscriptions();

        ProductSubscription inGrace = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(inGrace.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(inGrace.getGraceEndsAt()).isCloseTo(ended.getExpiresAt().plus(7, ChronoUnit.DAYS), within(1, ChronoUnit.MILLIS));
        var check = entitlements.check(new EntitlementCheckRequest(owner.getId(), null, productId, null));
        assertThat(check.allowed()).isTrue();
        assertThat(check.accessMode()).isEqualTo("READ_ONLY");
        assertThat(check.graceEndsAt()).isCloseTo(inGrace.getGraceEndsAt(), within(1, ChronoUnit.MILLIS));
        assertThat(entitlements.forCustomer(owner.getId())).anySatisfy(e -> {
            assertThat(e.productId()).isEqualTo(productId);
            assertThat(e.accessMode()).isEqualTo("READ_ONLY");
        });

        // After the grace period the subscription expires and grants nothing.
        inGrace.setGraceEndsAt(Instant.now().minus(1, ChronoUnit.HOURS));
        subscriptionRepository.save(inGrace);
        assertThat(subscriptionService.expireOverdueSubscriptions()).isGreaterThanOrEqualTo(1);
        assertThat(subscriptionRepository.findById(subscription.getId()).orElseThrow().getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(entitlements.check(new EntitlementCheckRequest(owner.getId(), null, productId, null)).allowed()).isFalse();
    }

    @Test
    void turningAutoRenewBackOnDuringTheGracePeriodRenewsAtOnceAndRestoresFullAccess() {
        Customer owner = newCustomer("Asia/Kolkata");
        ProductSubscription subscription = individualSubscription(owner);
        subscriptionService.setAutoRenew(owner.getId(), subscription.getId(), false);
        ProductSubscription ended = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        ended.setExpiresAt(Instant.now().minus(2, ChronoUnit.DAYS));
        subscriptionRepository.save(ended);
        subscriptionService.expireOverdueSubscriptions();
        assertThat(subscriptionRepository.findById(subscription.getId()).orElseThrow().getGraceEndsAt()).isNotNull();

        subscriptionService.setAutoRenew(owner.getId(), subscription.getId(), true);

        ProductSubscription renewed = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(renewed.getGraceEndsAt()).isNull();
        assertThat(renewed.getExpiresAt()).isAfter(Instant.now());
        assertThat(entitlements.check(new EntitlementCheckRequest(owner.getId(), null, renewed.getProductId(), null)).accessMode()).isEqualTo("FULL");
    }

    // ------------------------------------------------------------------ RR-4 A

    /** The term ended at 10:00 IST on 10 Jan 2030 with auto-renew off; the grace period ends at 10:00 IST on 17 Jan. */
    private static final Instant ENDED = Instant.parse("2030-01-10T04:30:00Z");

    @Test
    void graceRemindersGoOutDailyAndStopAtTheEndOfTheLocalGraceDayNotAtTheInstantItEnds() {
        Customer owner = newCustomer("Asia/Kolkata");
        ProductSubscription subscription = individualSubscription(owner);
        subscription.setAutoRenew(false);
        subscription.setExpiresAt(ENDED);
        subscription.setGraceEndsAt(ENDED.plus(7, ChronoUnit.DAYS));
        subscriptionRepository.save(subscription);
        long start = logRepository.findBySubscriptionIdOrderBySentAtAsc(subscription.getId()).size();

        Instant jan12 = Instant.parse("2030-01-12T04:00:00Z");   // 09:30 IST
        reminderService.sendDue(jan12);
        assertThat(logRepository.findBySubscriptionIdOrderBySentAtAsc(subscription.getId())).hasSize((int) start + 1);
        reminderService.sendDue(jan12.plus(1, ChronoUnit.HOURS));
        assertThat(logRepository.findBySubscriptionIdOrderBySentAtAsc(subscription.getId())).as("one per day").hasSize((int) start + 1);
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(owner.getId())).anyMatch(n -> n.getTitle().contains("has ended"));

        // 15:30 IST on the last grace day, after the grace instant (10:00 IST) and after the expiry job: still reminded.
        ProductSubscription expired = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        expired.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(expired);
        reminderService.sendDue(Instant.parse("2030-01-17T10:00:00Z"));
        assertThat(logRepository.findBySubscriptionIdOrderBySentAtAsc(subscription.getId())).as("the last local day still gets its reminder").hasSize((int) start + 2);

        // The next local day: nothing more.
        reminderService.sendDue(Instant.parse("2030-01-18T04:00:00Z"));
        assertThat(logRepository.findBySubscriptionIdOrderBySentAtAsc(subscription.getId())).hasSize((int) start + 2);
    }

    // ------------------------------------------------------------------ RR-2 B, RR-5

    @Test
    void anAdminChoosesWhoElseGetsTheRemindersAndOnWhichChannels() {
        Organization org = newOrganization(10);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN, "Asia/Kolkata");
        Long inAppOnly = newMember(org, OrgRole.MEMBER, "Asia/Kolkata");
        Long emailOnly = newMember(org, OrgRole.MEMBER, "Asia/Kolkata");
        Long notChosen = newMember(org, OrgRole.MEMBER, "Asia/Kolkata");
        ProductSubscription subscription = orgSubscription(org, 15);
        subscription.setExpiresAt(ENDED);
        subscriptionRepository.save(subscription);

        assertThatThrownBy(() -> recipientService.save(notChosen, new SaveReminderRecipientsRequest(List.of()))).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> recipientService.save(adminId, new SaveReminderRecipientsRequest(List.of(new SaveReminderRecipientsRequest.Entry(inAppOnly, false, false)))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("email, in-app or both");
        assertThatThrownBy(() -> recipientService.save(adminId, new SaveReminderRecipientsRequest(List.of(new SaveReminderRecipientsRequest.Entry(newCustomer(null).getId(), true, true)))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("active members");

        var saved = recipientService.save(adminId, new SaveReminderRecipientsRequest(List.of(
            new SaveReminderRecipientsRequest.Entry(inAppOnly, false, true), new SaveReminderRecipientsRequest.Entry(emailOnly, true, false))));
        assertThat(saved.defaults()).extracting(p -> p.customerId()).contains(adminId).doesNotContain(inAppOnly);
        assertThat(saved.added()).extracting(p -> p.customerId()).containsExactlyInAnyOrder(inAppOnly, emailOnly);

        reminderService.sendDue(Instant.parse("2030-01-08T04:00:00Z"));   // two days before the renewal, 09:30 IST

        var recipients = logRepository.findBySubscriptionIdOrderBySentAtAsc(subscription.getId()).stream().map(l -> l.getRecipientCustomerId()).toList();
        assertThat(recipients).contains(adminId, inAppOnly, emailOnly).doesNotContain(notChosen);
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(inAppOnly)).as("in-app chosen").anyMatch(n -> n.getTitle().contains("renews"));
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(emailOnly)).as("in-app not chosen: email only").noneMatch(n -> n.getTitle().contains("renews"));
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(adminId)).anyMatch(n -> n.getTitle().contains("renews"));
    }
}
