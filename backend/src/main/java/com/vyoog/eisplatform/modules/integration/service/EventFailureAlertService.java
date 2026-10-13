package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.modules.integration.model.EventPlatformSetting;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * REQ-INT-002 (C91, EV-4 "both"): when an event fails for good, the addresses on the settings screen are told in the
 * app (those who have an account) and by email. Platform administrators without an account in the app get the email
 * only; everyone with the permission also sees the failed count on the events screen. A failed alert never fails the
 * dispatcher.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventFailureAlertService {

    private static final Logger log = LoggerFactory.getLogger(EventFailureAlertService.class);

    private final EventPlatformSettingsService settingsService;
    private final OutboxEventRepository outboxEventRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final com.vyoog.eisplatform.modules.template.service.MessageTemplateService templateService;
    private final com.vyoog.eisplatform.modules.template.service.MessageLanguageResolver languageResolver;

    /** Number of addresses the alert was sent to. */
    @Transactional
    public int notifyFailed(Long outboxEventId) {
        try {
            EventPlatformSetting settings = settingsService.current();
            if (!settings.isAlertInApp() && !settings.isAlertEmail()) {
                return 0;
            }
            Optional<OutboxEvent> found = outboxEventRepository.findById(outboxEventId);
            if (found.isEmpty()) {
                return 0;
            }
            OutboxEvent event = found.get();
            Map<String, String> values = Map.of("eventType", event.getEventType(),
                "aggregate", event.getAggregateType() + " " + event.getAggregateId(), "attempts", String.valueOf(event.getAttempts()),
                "error", event.getLastError() == null ? "" : event.getLastError());
            int sent = 0;
            for (String address : EventPlatformSettingsService.recipients(settings)) {
                Optional<Customer> customer = customerRepository.findByEmailIgnoreCase(address);
                try {
                    // C92 (TM-1 A): the wording is an administrator-editable template, in the recipient's language.
                    var text = templateService.render("NOTIF_EVENT_FAILED", languageResolver.forEmail(address), values);
                    if (customer.isPresent() && settings.isAlertInApp()) {
                        if (settings.isAlertEmail()) {
                            notificationService.notify(customer.get().getId(), address, NotificationCategory.SYSTEM, NotificationSeverity.WARNING, text.subject(), text.body());
                        } else {
                            notificationService.notifyInAppOnly(customer.get().getId(), NotificationCategory.SYSTEM, NotificationSeverity.WARNING, text.subject(), text.body());
                        }
                    } else if (settings.isAlertEmail()) {
                        emailService.sendNotificationEmail(address, text.subject(), text.body());
                    } else {
                        continue;
                    }
                    sent++;
                } catch (RuntimeException e) {
                    log.warn("Could not alert {} about failed event {}: {}", address, event.getEventId(), e.getMessage());
                }
            }
            return sent;
        } catch (RuntimeException e) {
            log.warn("Could not send the failed-event alert for event {}: {}", outboxEventId, e.getMessage());
            return 0;
        }
    }
}
