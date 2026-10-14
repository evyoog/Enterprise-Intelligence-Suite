package com.vyoog.eisplatform.modules.renewal.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;
import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.preference.model.CustomerPreference;
import com.vyoog.eisplatform.modules.preference.repository.CustomerPreferenceRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import com.vyoog.eisplatform.modules.renewal.dto.RenewalDto;
import com.vyoog.eisplatform.modules.renewal.dto.RenewalReminderPreferenceDto;
import com.vyoog.eisplatform.modules.renewal.dto.SaveRenewalReminderPreferenceRequest;
import com.vyoog.eisplatform.modules.renewal.model.RenewalReminderLog;
import com.vyoog.eisplatform.modules.renewal.model.RenewalReminderPreference;
import com.vyoog.eisplatform.modules.renewal.repository.RenewalReminderLogRepository;
import com.vyoog.eisplatform.modules.renewal.repository.RenewalReminderPreferenceRepository;
import com.vyoog.eisplatform.modules.renewal.repository.RenewalReminderRecipientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REQ-SUB-004.4–.11 (C64): renewal reminders. A reminder is due for a
 * subscription and recipient when, in the recipient's time zone, the
 * renewal date is 1…N days away and the send time has passed, and none was
 * logged for that local date (BR-5, BR-6). Reminders stop once the renewal
 * date moves on (renewed) and are never sent after it (OQ 4 default).
 */
@Service
@Slf4j
public class RenewalReminderService {

    public static final int MIN_DAYS = 1;
    public static final int MAX_DAYS = 30;

    private final ProductSubscriptionRepository subscriptionRepository;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final CustomerPreferenceRepository customerPreferenceRepository;
    private final RenewalReminderPreferenceRepository preferenceRepository;
    private final RenewalReminderLogRepository logRepository;
    private final BillingSettingsService billingSettingsService;
    private final SubscriptionService subscriptionService;
    private final RenewalService renewalService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate newTransaction;
    private final String frontendUrl;
    private final RenewalReminderRecipientRepository recipientRepository;
    private final com.vyoog.eisplatform.modules.authorization.service.MemberAccessService memberAccessService;

    public RenewalReminderService(ProductSubscriptionRepository subscriptionRepository, OrganizationMemberRepository memberRepository,
                                  CustomerRepository customerRepository, CustomerPreferenceRepository customerPreferenceRepository,
                                  RenewalReminderPreferenceRepository preferenceRepository, RenewalReminderLogRepository logRepository,
                                  BillingSettingsService billingSettingsService, SubscriptionService subscriptionService,
                                  RenewalService renewalService, NotificationService notificationService, AuditService auditService,
                                  OutboxService outboxService, PlatformTransactionManager transactionManager,
                                  @Value("${app.frontend-url}") String frontendUrl, RenewalReminderRecipientRepository recipientRepository,
                                  com.vyoog.eisplatform.modules.authorization.service.MemberAccessService memberAccessService) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.customerRepository = customerRepository;
        this.customerPreferenceRepository = customerPreferenceRepository;
        this.preferenceRepository = preferenceRepository;
        this.logRepository = logRepository;
        this.billingSettingsService = billingSettingsService;
        this.subscriptionService = subscriptionService;
        this.renewalService = renewalService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.recipientRepository = recipientRepository;
        this.memberAccessService = memberAccessService;
    }

    /** BR-4: what applies to one recipient. */
    record Effective(boolean enabled, int daysBefore, LocalTime sendTime, ZoneId zone) {
    }

    Effective effective(Long customerId, BillingSettings settings) {
        Optional<RenewalReminderPreference> own = preferenceRepository.findByCustomerId(customerId);
        int days = own.map(RenewalReminderPreference::getDaysBefore).orElse(null) != null
            ? own.get().getDaysBefore() : settings.getReminderLeadDays();
        String time = own.map(RenewalReminderPreference::getSendTime).orElse(null) != null
            ? own.get().getSendTime() : settings.getReminderSendTime();
        return new Effective(own.map(RenewalReminderPreference::isEnabled).orElse(true), days, LocalTime.parse(time),
            zoneFor(customerId, settings));
    }

    private ZoneId zoneFor(Long customerId, BillingSettings settings) {
        String preferred = customerPreferenceRepository.findById(customerId).map(CustomerPreference::getTimeZone).orElse(null);
        for (String candidate : new String[] {preferred, settings.getReminderTimeZone(), "UTC"}) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            try {
                return ZoneId.of(candidate);
            } catch (DateTimeException ignored) {
                // fall through to the next candidate
            }
        }
        return ZoneId.of("UTC");
    }

    /** A reminder recipient and the channels they get it on (C100 RR-5). */
    record Recipient(Customer customer, boolean inApp, boolean email) {
    }

    /** BR-8, C100 RR-2 B and RR-5: an individual subscription's owner; for an organization subscription the ACTIVE organization admins, the ACTIVE
     * members with Manage billing, and the people an organization administrator added (each on the channels chosen), each person once. */
    List<Recipient> recipients(ProductSubscription subscription) {
        if (subscription.getOwnerType() == RegistrationOwnerType.INDIVIDUAL) {
            return customerRepository.findById(subscription.getOwnerCustomerId()).map(c -> List.of(new Recipient(c, true, true))).orElse(List.of());
        }
        Long organizationId = subscription.getOwnerOrganizationId();
        Map<Long, Recipient> byCustomer = new LinkedHashMap<>();
        memberRepository.findByOrganizationId(organizationId).stream().filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .filter(m -> m.getOrgRole() == OrgRole.ORG_ADMIN || memberAccessService.hasPermission(m, "MANAGE_ORGANIZATION_BILLING"))
            .forEach(m -> customerRepository.findById(m.getCustomerId()).ifPresent(c -> byCustomer.put(c.getId(), new Recipient(c, true, true))));
        for (var extra : recipientRepository.findByOrganizationId(organizationId)) {
            boolean active = memberRepository.findByOrganizationIdAndCustomerId(organizationId, extra.getCustomerId())
                .map(m -> m.getStatus() == MembershipStatus.ACTIVE).orElse(false);
            if (!active) {
                continue;
            }
            Recipient existing = byCustomer.get(extra.getCustomerId());
            if (existing != null) {
                continue;   // already gets every channel
            }
            customerRepository.findById(extra.getCustomerId()).ifPresent(c ->
                byCustomer.put(c.getId(), new Recipient(c, extra.isInAppEnabled(), extra.isEmailEnabled())));
        }
        return new ArrayList<>(byCustomer.values());
    }

    /** One scheduler run (every 15 minutes, RenewalJob). Returns reminders sent. */
    public int sendDue() {
        return sendDue(Instant.now());
    }

    public int sendDue(Instant now) {
        BillingSettings settings = billingSettingsService.current();
        List<ProductSubscription> candidates = subscriptionRepository.findByStatusAndExpiresAtBetween(
            SubscriptionStatus.ACTIVE, now, now.plus(Duration.ofDays(MAX_DAYS + 2)));
        int sent = 0;
        for (ProductSubscription subscription : candidates) {
            for (Recipient recipient : recipients(subscription)) {
                Effective eff = effective(recipient.customer().getId(), settings);
                if (!eff.enabled()) {
                    continue;
                }
                ZonedDateTime localNow = now.atZone(eff.zone());
                LocalDate today = localNow.toLocalDate();
                long days = ChronoUnit.DAYS.between(today, subscription.getExpiresAt().atZone(eff.zone()).toLocalDate());
                if (days < 1 || days > eff.daysBefore() || localNow.toLocalTime().isBefore(eff.sendTime())) {
                    continue;
                }
                sent += sendOnce(subscription, recipient, today, (int) days, now, false);
            }
        }
        sent += sendGraceDue(now, settings);
        return sent;
    }

    /**
     * C100 RR-4 A: once the term has ended with auto-renew off, a reminder goes out every day of the grace period at the recipient's send time and in
     * the recipient's time zone, and the last one is on the grace period's last day <b>in that time zone</b>: the reminders do not stop at the
     * instant the subscription expires, they stop at the end of that local day. A renewed subscription has no grace end and gets no more.
     */
    private int sendGraceDue(Instant now, BillingSettings settings) {
        int sent = 0;
        for (ProductSubscription subscription : subscriptionRepository.findByGraceEndsAtBetween(now.minus(Duration.ofDays(2)), now.plus(Duration.ofDays(GRACE_DAYS_MAX)))) {
            if ((subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.EXPIRED)
                    || subscription.getExpiresAt() == null || subscription.getExpiresAt().isAfter(now)) {
                continue;
            }
            for (Recipient recipient : recipients(subscription)) {
                Effective eff = effective(recipient.customer().getId(), settings);
                if (!eff.enabled()) {
                    continue;
                }
                ZonedDateTime localNow = now.atZone(eff.zone());
                LocalDate today = localNow.toLocalDate();
                LocalDate endedOn = subscription.getExpiresAt().atZone(eff.zone()).toLocalDate();
                LocalDate lastDay = subscription.getGraceEndsAt().atZone(eff.zone()).toLocalDate();
                if (today.isBefore(endedOn) || today.isAfter(lastDay) || localNow.toLocalTime().isBefore(eff.sendTime())) {
                    continue;
                }
                sent += sendOnce(subscription, recipient, today, (int) ChronoUnit.DAYS.between(today, endedOn), now, true);
            }
        }
        return sent;
    }

    private static final int GRACE_DAYS_MAX = 9;

    private int sendOnce(ProductSubscription subscription, Recipient recipient, LocalDate today, int daysBefore, Instant now, boolean grace) {
        if (logRepository.existsBySubscriptionIdAndRecipientCustomerIdAndLocalDate(subscription.getId(), recipient.customer().getId(), today)) {
            return 0;
        }
        try {
            Boolean done = newTransaction.execute(status -> send(subscription, recipient, today, daysBefore, now, grace));
            return Boolean.TRUE.equals(done) ? 1 : 0;
        } catch (DataIntegrityViolationException e) {
            return 0;   // BR-6: another run logged this reminder first — nothing to send.
        }
    }

    private boolean send(ProductSubscription subscription, Recipient target, LocalDate localDate, int daysBefore, Instant now, boolean grace) {
        Customer recipient = target.customer();
        RenewalReminderLog entry = new RenewalReminderLog();
        entry.setSubscriptionId(subscription.getId());
        entry.setRecipientCustomerId(recipient.getId());
        entry.setLocalDate(localDate);
        entry.setRenewalDate(subscription.getExpiresAt());
        entry.setDaysBefore(daysBefore);
        entry.setSentAt(now);
        logRepository.saveAndFlush(entry);

        SubscriptionService.RenewalInfo info = subscriptionService.renewalInfo(subscription);
        ZoneId zone = effectiveZone(recipient);
        String date = DateTimeFormatter.ISO_LOCAL_DATE.format(subscription.getExpiresAt().atZone(zone));
        String plan = info.planName() == null ? "" : " (" + info.planName() + ")";
        String amount = info.amount() == null || info.amount().compareTo(BigDecimal.ZERO) == 0
            ? "" : " for " + info.amount().stripTrailingZeros().toPlainString() + " " + info.currency();
        java.util.Map<String, String> values = new java.util.HashMap<>();
        values.put("product", info.productName());
        values.put("plan", plan);
        values.put("date", date);
        values.put("manageUrl", frontendUrl + "/my/subscriptions");
        values.put("billingUrl", frontendUrl + "/billing");
        values.put("preferencesUrl", frontendUrl + "/account/preferences");
        String templateCode;
        if (grace) {
            // C100 RR-4 A: the grace reminder names the last day, in the recipient's own time zone.
            String graceEnd = DateTimeFormatter.ISO_LOCAL_DATE.format(subscription.getGraceEndsAt().atZone(zone));
            values.put("graceEnd", graceEnd);
            values.put("how", subscription.getStatus() == SubscriptionStatus.ACTIVE
                ? "It is read-only until " + graceEnd + "; renew before then to keep full access."
                : "It has expired; renew to get access back.");
            templateCode = "NOTIF_RENEWAL_GRACE_REMINDER";
        } else {
            values.put("amount", amount);
            values.put("how", renewalService.canChargeAutomatically(subscription)
                ? "Your saved payment method will be charged."
                : "A renewal invoice will be issued for you to pay online or by invoice.");
            templateCode = "NOTIF_RENEWAL_REMINDER";
        }
        // C92 (TM-1 A): the wording is an administrator-editable template, in the recipient's language. C100 RR-5: on the channels chosen for this person.
        notificationService.notifyTemplated(recipient.getId(), recipient.getEmail(), NotificationCategory.SUBSCRIPTION,
            NotificationSeverity.INFO, templateCode, values, target.inApp(), target.email());
        auditService.recordSuccess("RENEWAL_REMINDER_SENT", null, recipient.getId(), recipient.getEmail(), "ProductSubscription",
            subscription.getId().toString(), subscription.getOwnerOrganizationId(),
            grace ? "Renewal reminder " + (-daysBefore) + " day(s) after the term ended (grace period)" : "Renewal reminder " + daysBefore + " day(s) before " + date);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getId());
        payload.put("recipientCustomerId", recipient.getId());
        payload.put("renewalDate", subscription.getExpiresAt().toString());
        payload.put("daysBefore", daysBefore);
        payload.put("grace", grace);
        payload.put("localDate", localDate.toString());
        outboxService.publish(PlatformEventTypes.RENEWAL_REMINDER_SENT, PlatformEventTypes.AGGREGATE_SUBSCRIPTION, subscription.getId(), payload);
        return true;
    }

    private ZoneId effectiveZone(Customer recipient) {
        return zoneFor(recipient.getId(), billingSettingsService.current());
    }

    // ------------------------------------------------------------------
    // REQ-SUB-004.8: the user's own settings
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public RenewalReminderPreferenceDto preferences(Long customerId) {
        BillingSettings settings = billingSettingsService.current();
        Optional<RenewalReminderPreference> own = preferenceRepository.findByCustomerId(customerId);
        Effective eff = effective(customerId, settings);
        return new RenewalReminderPreferenceDto(eff.enabled(), own.map(RenewalReminderPreference::getDaysBefore).orElse(null),
            own.map(RenewalReminderPreference::getSendTime).orElse(null), eff.daysBefore(), eff.sendTime().toString(),
            eff.zone().getId(), settings.getReminderLeadDays(), settings.getReminderSendTime(), MIN_DAYS, MAX_DAYS);
    }

    @Transactional
    public RenewalReminderPreferenceDto savePreferences(Long customerId, SaveRenewalReminderPreferenceRequest request) {
        if (request.daysBefore() != null && (request.daysBefore() < MIN_DAYS || request.daysBefore() > MAX_DAYS)) {
            throw new IllegalArgumentException("Days before renewal must be between " + MIN_DAYS + " and " + MAX_DAYS + ".");
        }
        RenewalReminderPreference preference = preferenceRepository.findByCustomerId(customerId).orElseGet(() -> {
            RenewalReminderPreference created = new RenewalReminderPreference();
            created.setCustomerId(customerId);
            return created;
        });
        preference.setEnabled(request.enabled());
        preference.setDaysBefore(request.daysBefore());
        preference.setSendTime(request.sendTime() == null || request.sendTime().isBlank() ? null : request.sendTime());
        preference.setUpdatedAt(Instant.now());
        preferenceRepository.save(preference);
        auditService.recordSuccess("RENEWAL_REMINDER_PREFERENCES_CHANGED", null, customerId, null, "RenewalReminderPreference",
            customerId.toString(), null, "Reminders " + (request.enabled() ? "on" : "off") + ", days "
                + (request.daysBefore() == null ? "default" : request.daysBefore()) + ", time "
                + (preference.getSendTime() == null ? "default" : preference.getSendTime()));
        return preferences(customerId);
    }

    // ------------------------------------------------------------------
    // REQ-SUB-004: the caller's renewals (My subscriptions)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<RenewalDto> myRenewals(Long customerId) {
        return myRenewals(customerId, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<RenewalDto> myRenewals(Long customerId, Instant now) {
        BillingSettings settings = billingSettingsService.current();
        Effective eff = effective(customerId, settings);
        List<ProductSubscription> subscriptions = new ArrayList<>(subscriptionRepository.findByOwnerCustomerId(customerId));
        memberRepository.findByCustomerIdAndOrgRoleAndStatus(customerId, OrgRole.ORG_ADMIN, MembershipStatus.ACTIVE)
            .forEach(m -> subscriptions.addAll(subscriptionRepository.findByOwnerOrganizationId(m.getOrganizationId())));
        return subscriptions.stream()
            .filter(s -> s.getExpiresAt() != null && (s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.SUSPENDED))
            .map(s -> {
                SubscriptionService.RenewalInfo info = subscriptionService.renewalInfo(s);
                ZonedDateTime next = s.getStatus() == SubscriptionStatus.ACTIVE ? nextReminder(s, customerId, eff, now) : null;
                return new RenewalDto(s.getId(), info.productName(), info.planName(), s.isAutoRenew(), s.getExpiresAt(),
                    eff.enabled(), next == null ? null : next.toOffsetDateTime().toString(), s.getGraceEndsAt());
            })
            .toList();
    }

    ZonedDateTime nextReminder(ProductSubscription subscription, Long customerId, Effective eff, Instant now) {
        if (!eff.enabled()) {
            return null;
        }
        ZonedDateTime localNow = now.atZone(eff.zone());
        LocalDate today = localNow.toLocalDate();
        LocalDate renewal = subscription.getExpiresAt().atZone(eff.zone()).toLocalDate();
        if (subscription.getGraceEndsAt() != null && !subscription.getExpiresAt().isAfter(now)) {
            // C100 RR-4 A: in the grace period the next reminder is today (if not sent yet and still to come) or tomorrow, up to the last grace day.
            LocalDate lastDay = subscription.getGraceEndsAt().atZone(eff.zone()).toLocalDate();
            LocalDate day = today;
            if (!localNow.toLocalTime().isBefore(eff.sendTime()) || logRepository.existsBySubscriptionIdAndRecipientCustomerIdAndLocalDate(subscription.getId(), customerId, today)) {
                day = today.plusDays(1);
            }
            return day.isAfter(lastDay) ? null : ZonedDateTime.of(day, eff.sendTime(), eff.zone());
        }
        for (int d = eff.daysBefore(); d >= 1; d--) {
            LocalDate date = renewal.minusDays(d);
            if (date.isBefore(today)) {
                continue;
            }
            if (date.equals(today) && logRepository.existsBySubscriptionIdAndRecipientCustomerIdAndLocalDate(subscription.getId(), customerId, today)) {
                continue;
            }
            return ZonedDateTime.of(date, eff.sendTime(), eff.zone());
        }
        return null;
    }
}
