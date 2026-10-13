package com.vyoog.eisplatform.modules.administration.service;

import com.vyoog.eisplatform.modules.administration.dto.PlatformDefaultsDto;
import com.vyoog.eisplatform.modules.administration.dto.SavePlatformDefaultsRequest;
import com.vyoog.eisplatform.modules.administration.model.PlatformCurrency;
import com.vyoog.eisplatform.modules.administration.model.PlatformDefault;
import com.vyoog.eisplatform.modules.administration.repository.PlatformCurrencyRepository;
import com.vyoog.eisplatform.modules.administration.repository.PlatformDefaultRepository;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.RenewalReminderDefaultsDto;
import com.vyoog.eisplatform.modules.billing.dto.SaveRenewalReminderDefaultsRequest;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 15.01.02 Configure defaults (REQ-GOV-002, C92, TM-2 A): language, region, currency, time zone, date format and reminder
 * days for new users. The first five live here (empty = no default); reminder days are the renewal-reminder default of the
 * billing settings, so the reminder job and this screen share one value.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformDefaultsService {

    public static final Set<String> LANGUAGES = Set.of("en", "es");
    public static final Set<String> DATE_FORMATS = Set.of("auto", "DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD");

    private final PlatformDefaultRepository repository;
    private final PlatformCurrencyRepository currencyRepository;
    private final BillingSettingsService billingSettingsService;
    private final AuditService auditService;

    public PlatformDefaultsDto get() {
        PlatformDefault row = current();
        return new PlatformDefaultsDto(row.getLanguage(), row.getRegion(), row.getCurrency(), row.getTimeZone(), row.getDateFormat(),
            billingSettingsService.renewalReminderDefaults().daysBefore(), row.getUpdatedAt());
    }

    /** The default language, or null when none is set. */
    public String defaultLanguage() {
        return current().getLanguage();
    }

    private PlatformDefault current() {
        return repository.findById(PlatformDefault.SINGLETON_ID).orElseGet(PlatformDefault::new);
    }

    @Transactional
    public PlatformDefaultsDto save(SavePlatformDefaultsRequest request, Long adminCustomerId, String actorKeycloakSub) {
        PlatformDefault row = current();
        List<String> changes = new ArrayList<>();

        String language = blankToNull(request.language());
        if (language != null && !LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Language must be one of " + new java.util.TreeSet<>(LANGUAGES) + ".");
        }
        String region = blankToNull(request.region());
        if (region != null && !isRealLocaleTag(region)) {
            throw new IllegalArgumentException("Not a real region/locale: " + region);
        }
        String currency = blankToNull(request.currency());
        if (currency != null) {
            currency = currency.toUpperCase(Locale.ROOT);
            String code = currency;
            PlatformCurrency known = currencyRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + code));
            if (!known.isEnabled()) {
                throw new IllegalArgumentException("The currency " + code + " is disabled. Enable it first.");
            }
        }
        String timeZone = blankToNull(request.timeZone());
        if (timeZone != null) {
            try {
                ZoneId.of(timeZone);
            } catch (DateTimeException e) {
                throw new IllegalArgumentException("Not a real time zone: " + timeZone);
            }
        }
        String dateFormat = blankToNull(request.dateFormat());
        if (dateFormat != null && !DATE_FORMATS.contains(dateFormat)) {
            throw new IllegalArgumentException("Date format must be one of auto, DD/MM/YYYY, MM/DD/YYYY, YYYY-MM-DD.");
        }
        if (request.reminderDays() != null && (request.reminderDays() < 1 || request.reminderDays() > 30)) {
            throw new IllegalArgumentException("Reminder days must be 1 to 30.");
        }

        track(changes, "language", row.getLanguage(), language);
        track(changes, "region", row.getRegion(), region);
        track(changes, "currency", row.getCurrency(), currency);
        track(changes, "time zone", row.getTimeZone(), timeZone);
        track(changes, "date format", row.getDateFormat(), dateFormat);
        row.setLanguage(language);
        row.setRegion(region);
        row.setCurrency(currency);
        row.setTimeZone(timeZone);
        row.setDateFormat(dateFormat);
        row.setUpdatedBy(actorKeycloakSub);
        row.setUpdatedAt(Instant.now());
        repository.save(row);

        if (request.reminderDays() != null) {
            RenewalReminderDefaultsDto reminders = billingSettingsService.renewalReminderDefaults();
            if (!request.reminderDays().equals(reminders.daysBefore())) {
                changes.add("reminder days " + reminders.daysBefore() + " -> " + request.reminderDays());
                billingSettingsService.saveRenewalReminderDefaults(adminCustomerId,
                    new SaveRenewalReminderDefaultsRequest(request.reminderDays(), reminders.sendTime(), reminders.timeZone()));
            }
        }
        if (!changes.isEmpty()) {
            auditService.recordSuccess("PLATFORM_DEFAULTS_CHANGED", actorKeycloakSub, adminCustomerId, null, "PlatformDefault", "1", null,
                "Platform defaults changed: " + String.join("; ", changes));
        }
        return get();
    }

    /** Language subtag must be a real ISO 639-1 code ({@link Locale#forLanguageTag} alone accepts nonsense). */
    public static boolean isRealLocaleTag(String region) {
        String languageSubtag = region.split("-")[0].toLowerCase(Locale.ROOT);
        return languageSubtag.length() == 2 && Arrays.asList(Locale.getISOLanguages()).contains(languageSubtag);
    }

    private static void track(List<String> changes, String name, String before, String after) {
        if (!java.util.Objects.equals(before, after)) {
            changes.add(name + " " + (before == null ? "(none)" : before) + " -> " + (after == null ? "(none)" : after));
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
