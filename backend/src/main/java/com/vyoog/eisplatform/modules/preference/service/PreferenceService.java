package com.vyoog.eisplatform.modules.preference.service;

import com.vyoog.eisplatform.modules.preference.dto.CustomerPreferenceDto;
import com.vyoog.eisplatform.modules.preference.model.CustomerPreference;
import com.vyoog.eisplatform.modules.preference.repository.CustomerPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * Phase 6 (2026.3.3): account-level portal personalization — see
 * {@link CustomerPreference}'s own javadoc for the language/region/timeZone
 * distinction this replaces (previously three concepts silently conflated
 * into one localStorage-only "region" list on the frontend). Same
 * find-or-default / find-or-create shape as
 * {@code DashboardService#getPreferences}/{@code #updatePreferences}, so a
 * customer who has never touched their preferences gets sensible defaults
 * without a row existing yet.
 */
@Service
@RequiredArgsConstructor
public class PreferenceService {

    /** Exactly the languages this app's own i18n bundle ships (see
     * i18n/index.ts's own SUPPORTED_LANGUAGES) — storing anything else would
     * be a preference with no real translations behind it. */
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es");
    private static final Set<String> SUPPORTED_THEME_MODES = Set.of("light", "dark", "system");

    private final CustomerPreferenceRepository repository;

    @Transactional(readOnly = true)
    public CustomerPreferenceDto getPreferences(Long customerId) {
        return repository.findById(customerId)
            .map(this::toDto)
            .orElseGet(() -> new CustomerPreferenceDto(null, null, null, null, false));
    }

    @Transactional
    public CustomerPreferenceDto updatePreferences(Long customerId, String language, String region,
                                                    String timeZone, String themeMode, boolean reducedMotion) {
        if (language != null && !SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        if (themeMode != null && !SUPPORTED_THEME_MODES.contains(themeMode)) {
            throw new IllegalArgumentException("Unsupported theme mode: " + themeMode);
        }
        if (timeZone != null) {
            try {
                ZoneId.of(timeZone);
            } catch (DateTimeException e) {
                throw new IllegalArgumentException("Not a real time zone: " + timeZone);
            }
        }
        if (region != null && !isRealLocaleTag(region)) {
            throw new IllegalArgumentException("Not a real region/locale: " + region);
        }

        CustomerPreference preference = repository.findById(customerId)
            .orElseGet(() -> {
                CustomerPreference created = new CustomerPreference();
                created.setCustomerId(customerId);
                return created;
            });
        preference.setLanguage(language);
        preference.setRegion(region);
        preference.setTimeZone(timeZone);
        preference.setThemeMode(themeMode);
        preference.setReducedMotion(reducedMotion);
        preference.setUpdatedAt(Instant.now());
        repository.save(preference);
        return toDto(preference);
    }

    /**
     * {@link Locale#forLanguageTag} is deliberately lenient about BCP 47
     * syntax (it parses "not-a-locale" without complaint) — real validation
     * means checking the language subtag against ISO 639-1's own curated
     * list, not just that it's syntactically well-formed.
     */
    private static boolean isRealLocaleTag(String region) {
        String languageSubtag = region.split("-")[0].toLowerCase(Locale.ROOT);
        return languageSubtag.length() == 2 && Arrays.asList(Locale.getISOLanguages()).contains(languageSubtag);
    }

    private CustomerPreferenceDto toDto(CustomerPreference preference) {
        return new CustomerPreferenceDto(
            preference.getLanguage(),
            preference.getRegion(),
            preference.getTimeZone(),
            preference.getThemeMode(),
            preference.isReducedMotion()
        );
    }
}
