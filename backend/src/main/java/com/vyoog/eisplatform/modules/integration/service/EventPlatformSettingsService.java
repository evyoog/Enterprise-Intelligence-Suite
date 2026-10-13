package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.integration.dto.EventPlatformSettingsDto;
import com.vyoog.eisplatform.modules.integration.dto.UpdateEventPlatformSettingsRequest;
import com.vyoog.eisplatform.modules.integration.model.EventPlatformSetting;
import com.vyoog.eisplatform.modules.integration.repository.EventPlatformSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * REQ-INT-002 (C91): retention (EV-3, default 30 days, configurable) and failure alerts (EV-4, in-app and email).
 * The first read creates the row from the configured default ({@code app.events.retention-days}).
 */
@Service
@Transactional(readOnly = true)
public class EventPlatformSettingsService {

    public static final int MIN_RETENTION_DAYS = 1;
    public static final int MAX_RETENTION_DAYS = 3650;
    public static final int MAX_RECIPIENTS = 20;
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@,;]+@[^\\s@,;]+\\.[^\\s@,;]+$");

    private final EventPlatformSettingRepository repository;
    private final AuditService auditService;
    private final int defaultRetentionDays;

    public EventPlatformSettingsService(EventPlatformSettingRepository repository, AuditService auditService,
                                        @Value("${app.events.retention-days:30}") int defaultRetentionDays) {
        this.repository = repository;
        this.auditService = auditService;
        this.defaultRetentionDays = defaultRetentionDays;
    }

    public EventPlatformSettingsDto get() {
        return toDto(current());
    }

    /** The row, or an unsaved default when none exists yet (reads never write). */
    EventPlatformSetting current() {
        return repository.findById(EventPlatformSetting.SINGLETON_ID).orElseGet(() -> {
            EventPlatformSetting fresh = new EventPlatformSetting();
            fresh.setRetentionDays(Math.min(MAX_RETENTION_DAYS, Math.max(MIN_RETENTION_DAYS, defaultRetentionDays)));
            return fresh;
        });
    }

    public int retentionDays() {
        return current().getRetentionDays();
    }

    @Transactional
    public EventPlatformSettingsDto update(UpdateEventPlatformSettingsRequest request, String actorKeycloakSub) {
        EventPlatformSetting row = current();
        List<String> changes = new ArrayList<>();
        if (request.retentionDays() != null) {
            int days = request.retentionDays();
            if (days < MIN_RETENTION_DAYS || days > MAX_RETENTION_DAYS) {
                throw new IllegalArgumentException("Keep delivered events for " + MIN_RETENTION_DAYS + " to " + MAX_RETENTION_DAYS + " days.");
            }
            if (days != row.getRetentionDays()) {
                changes.add("retention " + row.getRetentionDays() + " -> " + days + " days");
            }
            row.setRetentionDays(days);
        }
        if (request.alertRecipients() != null) {
            String joined = String.join(",", normalize(request.alertRecipients()));
            if (!joined.equals(row.getAlertRecipients() == null ? "" : row.getAlertRecipients())) {
                changes.add("alert recipients changed");
            }
            row.setAlertRecipients(joined.isEmpty() ? null : joined);
        }
        if (request.alertInApp() != null) {
            if (request.alertInApp() != row.isAlertInApp()) {
                changes.add("in-app alert " + (request.alertInApp() ? "on" : "off"));
            }
            row.setAlertInApp(request.alertInApp());
        }
        if (request.alertEmail() != null) {
            if (request.alertEmail() != row.isAlertEmail()) {
                changes.add("email alert " + (request.alertEmail() ? "on" : "off"));
            }
            row.setAlertEmail(request.alertEmail());
        }
        row.setUpdatedBy(actorKeycloakSub);
        row.setUpdatedAt(Instant.now());
        EventPlatformSetting saved = repository.save(row);
        if (!changes.isEmpty()) {
            auditService.recordSuccess("EVENT_SETTINGS_CHANGED", actorKeycloakSub, null, null, "EventPlatformSetting", "1", null,
                String.join("; ", changes));
        }
        return toDto(saved);
    }

    /** Lower-cased, de-duplicated, validated addresses (at most {@value #MAX_RECIPIENTS}). */
    static List<String> normalize(List<String> raw) {
        Set<String> result = new LinkedHashSet<>();
        for (String entry : raw) {
            if (entry == null) {
                continue;
            }
            for (String part : entry.split("[,;\\s]+")) {
                String email = part.trim().toLowerCase(Locale.ROOT);
                if (email.isEmpty()) {
                    continue;
                }
                if (!EMAIL.matcher(email).matches()) {
                    throw new IllegalArgumentException("\"" + part.trim() + "\" is not a valid email address.");
                }
                result.add(email);
            }
        }
        if (result.size() > MAX_RECIPIENTS) {
            throw new IllegalArgumentException("At most " + MAX_RECIPIENTS + " alert recipients.");
        }
        return new ArrayList<>(result);
    }

    static List<String> recipients(EventPlatformSetting row) {
        if (row.getAlertRecipients() == null || row.getAlertRecipients().isBlank()) {
            return List.of();
        }
        return List.of(row.getAlertRecipients().split(","));
    }

    private static EventPlatformSettingsDto toDto(EventPlatformSetting row) {
        return new EventPlatformSettingsDto(row.getRetentionDays(), recipients(row), row.isAlertInApp(), row.isAlertEmail(), row.getUpdatedAt());
    }
}
