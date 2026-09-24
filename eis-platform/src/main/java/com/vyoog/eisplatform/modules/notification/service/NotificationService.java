package com.vyoog.eisplatform.modules.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.notification.dto.NotificationDto;
import com.vyoog.eisplatform.modules.notification.dto.NotificationPreferenceDto;
import com.vyoog.eisplatform.modules.notification.dto.NotificationSummaryDto;
import com.vyoog.eisplatform.modules.notification.model.Notification;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationPreference;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.repository.NotificationPreferenceRepository;
import com.vyoog.eisplatform.modules.notification.repository.NotificationRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Phase 18: the one seam every other module calls through to raise a real
 * notification — deliberately depends on nothing from any other domain
 * module (only the shared {@link EmailService} interface, same as
 * PasswordResetService already does), so modules that trigger notifications
 * (registration, auth, authorization) can call this one-directionally
 * without creating a cycle back into their own internals. Callers resolve
 * and pass the customer's email themselves rather than this service looking
 * it up, for exactly that reason.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    /**
     * Always records the in-app notification (the notification center is the
     * durable record — see Notification's own javadoc); sends an email only
     * when this customer hasn't muted this category (see
     * NotificationPreference) and only a best-effort attempt — a failed email
     * must never roll back the in-app record, same reasoning as
     * SmtpEmailService's own try/catch around every send.
     */
    @Transactional
    public void notify(Long customerId, String customerEmail, NotificationCategory category,
                        NotificationSeverity severity, String title, String message) {
        recordNotification(customerId, category, severity, title, message);

        if (customerEmail == null || customerEmail.isBlank()) {
            return;
        }
        if (isEmailDisabled(customerId, category)) {
            return;
        }
        try {
            emailService.sendNotificationEmail(customerEmail, title, message);
        } catch (Exception e) {
            log.error("Failed to send notification email for category {}: {}", category, e.getMessage());
        }
    }

    /** For an event that already sends its own dedicated, more specific
     * email through a different call site (e.g. PasswordResetService's own
     * "password changed" notice) — records the in-app entry only, so the
     * customer never gets two emails about the same event (see the roadmap's
     * own "avoid unnecessary notification noise"). */
    @Transactional
    public void notifyInAppOnly(Long customerId, NotificationCategory category,
                                 NotificationSeverity severity, String title, String message) {
        recordNotification(customerId, category, severity, title, message);
    }

    private void recordNotification(Long customerId, NotificationCategory category,
                                     NotificationSeverity severity, String title, String message) {
        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setCategory(category);
        notification.setSeverity(severity);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    public NotificationSummaryDto listNotifications(Long customerId) {
        List<NotificationDto> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
            .map(this::toDto)
            .toList();
        long unreadCount = notificationRepository.countByCustomerIdAndReadFalse(customerId);
        return new NotificationSummaryDto(unreadCount, notifications);
    }

    @Transactional
    public void markRead(Long customerId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndCustomerId(notificationId, customerId)
            .orElseThrow(() -> new ForbiddenException("This notification does not belong to you."));
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllRead(Long customerId) {
        Instant now = Instant.now();
        List<Notification> unread = notificationRepository.findByCustomerIdAndReadFalse(customerId);
        for (Notification notification : unread) {
            notification.setRead(true);
            notification.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
    }

    public NotificationPreferenceDto getPreferences(Long customerId) {
        List<NotificationCategory> disabled = preferenceRepository.findById(customerId)
            .map(p -> readCategories(p.getEmailDisabledCategoriesJson()))
            .orElse(List.of());
        return new NotificationPreferenceDto(disabled);
    }

    @Transactional
    public NotificationPreferenceDto updatePreferences(Long customerId, List<NotificationCategory> emailDisabledCategories) {
        NotificationPreference preference = preferenceRepository.findById(customerId)
            .orElseGet(() -> {
                NotificationPreference created = new NotificationPreference();
                created.setCustomerId(customerId);
                return created;
            });
        List<NotificationCategory> disabled = emailDisabledCategories == null ? List.of() : emailDisabledCategories;
        preference.setEmailDisabledCategoriesJson(writeCategories(disabled));
        preference.setUpdatedAt(Instant.now());
        preferenceRepository.save(preference);
        return new NotificationPreferenceDto(disabled);
    }

    private boolean isEmailDisabled(Long customerId, NotificationCategory category) {
        return preferenceRepository.findById(customerId)
            .map(p -> readCategories(p.getEmailDisabledCategoriesJson()).contains(category))
            .orElse(false);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getCategory(),
            notification.getSeverity(),
            notification.getTitle(),
            notification.getMessage(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }

    private String writeCategories(List<NotificationCategory> categories) {
        try {
            return objectMapper.writeValueAsString(categories);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize notification preferences", e);
        }
    }

    private List<NotificationCategory> readCategories(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<NotificationCategory>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
