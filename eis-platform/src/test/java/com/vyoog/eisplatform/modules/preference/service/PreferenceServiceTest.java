package com.vyoog.eisplatform.modules.preference.service;

import com.vyoog.eisplatform.modules.preference.dto.CustomerPreferenceDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 6 (2026.3.3): a customer's own portal-wide preferences — language,
 * region (Intl formatting locale), time zone, theme, reduced motion —
 * previously localStorage-only on the frontend (see CustomerPreference's own
 * javadoc for the language/region/timeZone distinction this replaces).
 */
@SpringBootTest
@ActiveProfiles("test")
class PreferenceServiceTest {

    @Autowired
    private PreferenceService preferenceService;
    @Autowired
    private CustomerRepository customerRepository;

    private Long newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("pref-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Pref");
        customer.setLastName("Tester");
        customer.setStatus(RegistrationStatus.COMPLETED);
        return customerRepository.save(customer).getId();
    }

    @Test
    void aCustomerWithNoStoredPreferencesGetsAllNullsExceptReducedMotionFalse() {
        Long customerId = newCustomer();
        CustomerPreferenceDto dto = preferenceService.getPreferences(customerId);

        assertThat(dto.language()).isNull();
        assertThat(dto.region()).isNull();
        assertThat(dto.timeZone()).isNull();
        assertThat(dto.themeMode()).isNull();
        assertThat(dto.reducedMotion()).isFalse();
    }

    @Test
    void updatingPreferencesPersistsAndRoundTripsExactly() {
        Long customerId = newCustomer();

        CustomerPreferenceDto updated = preferenceService.updatePreferences(
            customerId, "es", "en-IN", "Asia/Kolkata", "dark", true);

        assertThat(updated.language()).isEqualTo("es");
        assertThat(updated.region()).isEqualTo("en-IN");
        assertThat(updated.timeZone()).isEqualTo("Asia/Kolkata");
        assertThat(updated.themeMode()).isEqualTo("dark");
        assertThat(updated.reducedMotion()).isTrue();

        CustomerPreferenceDto reread = preferenceService.getPreferences(customerId);
        assertThat(reread).isEqualTo(updated);
    }

    @Test
    void updatingASecondTimeOverwritesTheFirstRowRatherThanCreatingASecond() {
        Long customerId = newCustomer();
        preferenceService.updatePreferences(customerId, "en", null, null, "light", false);
        CustomerPreferenceDto second = preferenceService.updatePreferences(customerId, "es", null, null, "dark", true);

        assertThat(preferenceService.getPreferences(customerId)).isEqualTo(second);
    }

    @Test
    void rejectsALanguageThisAppHasNoTranslationsFor() {
        Long customerId = newCustomer();
        assertThatThrownBy(() -> preferenceService.updatePreferences(customerId, "fr", null, null, null, false))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAThemeModeThatIsntOneOfTheThreeRealOnes() {
        Long customerId = newCustomer();
        assertThatThrownBy(() -> preferenceService.updatePreferences(customerId, null, null, null, "solarized", false))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsATimeZoneThatIsntARealIanaZone() {
        Long customerId = newCustomer();
        assertThatThrownBy(() -> preferenceService.updatePreferences(customerId, null, null, "Mars/OlympusMons", null, false))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsARealIanaTimeZone() {
        Long customerId = newCustomer();
        CustomerPreferenceDto dto = preferenceService.updatePreferences(customerId, null, null, "America/New_York", null, false);
        assertThat(dto.timeZone()).isEqualTo("America/New_York");
    }

    @Test
    void rejectsARegionThatIsntARealLocale() {
        Long customerId = newCustomer();
        assertThatThrownBy(() -> preferenceService.updatePreferences(customerId, null, "not-a-locale-!!!", null, null, false))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingPreferencesToNullClearsThemBackToUnset() {
        Long customerId = newCustomer();
        preferenceService.updatePreferences(customerId, "en", "en-US", "UTC", "light", true);
        CustomerPreferenceDto cleared = preferenceService.updatePreferences(customerId, null, null, null, null, false);

        assertThat(cleared.language()).isNull();
        assertThat(cleared.region()).isNull();
        assertThat(cleared.timeZone()).isNull();
        assertThat(cleared.themeMode()).isNull();
        assertThat(cleared.reducedMotion()).isFalse();
    }
}
