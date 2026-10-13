package com.vyoog.eisplatform.modules.template.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.administration.dto.PlatformDefaultsDto;
import com.vyoog.eisplatform.modules.administration.dto.SavePlatformDefaultsRequest;
import com.vyoog.eisplatform.modules.administration.model.PlatformCurrency;
import com.vyoog.eisplatform.modules.administration.repository.PlatformCurrencyRepository;
import com.vyoog.eisplatform.modules.administration.service.PlatformDefaultsService;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.billing.service.BillingSettingsService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.preference.service.PreferenceService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import com.vyoog.eisplatform.modules.template.dto.MessageTemplateDtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/** REQ-GOV-002 (C92): admin-editable message templates (TM-1 A) and platform defaults (TM-2 A). TC-GOV-008..015. */
@SpringBootTest(properties = "management.health.mail.enabled=false")
@ActiveProfiles("test")
class MessageTemplateAndDefaultsTest {

    @MockBean private JavaMailSender mailSender;

    @Autowired private MessageTemplateService templates;
    @Autowired private PlatformDefaultsService defaults;
    @Autowired private PreferenceService preferenceService;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailService emailService;
    @Autowired private BillingSettingsService billingSettingsService;
    @Autowired private PlatformCurrencyRepository currencyRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    @BeforeEach
    void reset() {
        for (MessageCatalog.Type type : MessageCatalog.TYPES) {
            for (String lang : MessageCatalog.LANGUAGES) {
                templates.reset(type.code(), lang, "test");
            }
        }
        defaults.save(new SavePlatformDefaultsRequest(null, null, null, null, null, null), null, "test");
    }

    private Customer customer() {
        Customer c = new Customer();
        c.setEmail("tpl-" + System.nanoTime() + "@test.example");
        c.setFirstName("Tpl");
        c.setLastName("User");
        return customerRepository.save(c);
    }

    // TC-GOV-008
    @Test
    void theBuiltInTextIsWhatTheCodeSentBeforeAndFillsPlaceholdersInTheChosenLanguage() {
        var en = templates.render("EMAIL_PASSWORD_RESET", "en", Map.of("link", "https://x/reset"));
        assertThat(en.subject()).isEqualTo("Reset your Vyoog password");
        assertThat(en.body()).contains("set a new password here:\n\nhttps://x/reset\n\nThis link expires soon");

        var es = templates.render("NOTIF_ORG_ROLE_CHANGED", "es", Map.of("role", "MEMBER"));
        assertThat(es.subject()).isEqualTo("Su rol en la organización cambió");
        assertThat(es.body()).isEqualTo("Su rol en la organización ahora es MEMBER.");

        // an unknown language falls back to English; a missing value becomes empty
        assertThat(templates.render("NOTIF_ORG_ROLE_CHANGED", "fr", Map.of("role", "MEMBER")).subject()).isEqualTo("Your organization role changed");
        assertThat(templates.render("NOTIF_ORG_ROLE_CHANGED", "en", Map.of()).body()).isEqualTo("Your role in your organization is now .");
        assertThatThrownBy(() -> templates.render("NOPE", "en", Map.of())).isInstanceOf(IllegalArgumentException.class);
    }

    // TC-GOV-009
    @Test
    void anAdministratorEditsOneLanguageAndResetsItAndEachChangeIsAudited() {
        TemplateTypeDto saved = templates.save("NOTIF_ORG_ROLE_CHANGED", "en",
            new SaveTemplateRequest("Role update", "Hello, you are now {role} in your organization."), "sub-admin");
        assertThat(saved.variants()).filteredOn(v -> v.language().equals("en")).singleElement().satisfies(v -> {
            assertThat(v.customized()).isTrue();
            assertThat(v.subject()).isEqualTo("Role update");
            assertThat(v.defaultSubject()).isEqualTo("Your organization role changed");
        });
        assertThat(saved.variants()).filteredOn(v -> v.language().equals("es")).singleElement().satisfies(v -> assertThat(v.customized()).isFalse());

        var en = templates.render("NOTIF_ORG_ROLE_CHANGED", "en", Map.of("role", "ORG_ADMIN"));
        assertThat(en.subject()).isEqualTo("Role update");
        assertThat(en.body()).isEqualTo("Hello, you are now ORG_ADMIN in your organization.");
        assertThat(templates.render("NOTIF_ORG_ROLE_CHANGED", "es", Map.of("role", "X")).subject()).isEqualTo("Su rol en la organización cambió");

        templates.reset("NOTIF_ORG_ROLE_CHANGED", "en", "sub-admin");
        assertThat(templates.render("NOTIF_ORG_ROLE_CHANGED", "en", Map.of("role", "X")).subject()).isEqualTo("Your organization role changed");
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "MESSAGE_TEMPLATE_CHANGED".equals(a.getAction()))
            .anyMatch(a -> "MESSAGE_TEMPLATE_RESET".equals(a.getAction()));
    }

    // TC-GOV-010
    @Test
    void aTemplateMustUseOnlyKnownPlaceholdersKeepTheRequiredOnesAndStayWithinTheLimits() {
        assertThatThrownBy(() -> templates.save("EMAIL_PASSWORD_RESET", "en", new SaveTemplateRequest("Reset", "No link here."), "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("{link}");
        assertThatThrownBy(() -> templates.save("EMAIL_PASSWORD_RESET", "en", new SaveTemplateRequest("Reset {nope}", "Go: {link}"), "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("{nope}").hasMessageContaining("{link}");
        assertThatThrownBy(() -> templates.save("EMAIL_PASSWORD_RESET", "en", new SaveTemplateRequest(" ", "Go: {link}"), "s"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> templates.save("EMAIL_PASSWORD_RESET", "en", new SaveTemplateRequest("x".repeat(201), "Go: {link}"), "s"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> templates.save("EMAIL_PASSWORD_RESET", "en", new SaveTemplateRequest("Reset", "{link} " + "y".repeat(5000)), "s"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> templates.save("EMAIL_PASSWORD_RESET", "fr", new SaveTemplateRequest("Reset", "{link}"), "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Language");
        assertThatThrownBy(() -> templates.save("NOPE", "en", new SaveTemplateRequest("a", "b"), "s")).isInstanceOf(ResourceNotFoundException.class);
        // a required placeholder may sit in the subject instead of the body
        templates.save("EMAIL_PASSWORD_RESET", "en", new SaveTemplateRequest("Reset: {link}", "Use the link in the subject."), "s");
    }

    // TC-GOV-011
    @Test
    void aPreviewShowsExampleValuesAndSavesNothing() {
        PreviewDto preview = templates.preview("NOTIF_PRODUCT_ACCESS_GRANTED", "en", new SaveTemplateRequest("New: {product}", "Role {role} on {product}."));
        assertThat(preview.subject()).isEqualTo("New: Valam.ai");
        assertThat(preview.body()).isEqualTo("Role USER on Valam.ai.");
        assertThat(templates.render("NOTIF_PRODUCT_ACCESS_GRANTED", "en", Map.of("product", "P", "role", "R")).subject()).isEqualTo("Access granted: P");
        assertThatThrownBy(() -> templates.preview("NOTIF_PRODUCT_ACCESS_GRANTED", "en", new SaveTemplateRequest("{bad}", "x")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(templates.listTypes()).hasSize(MessageCatalog.TYPES.size()).allSatisfy(t -> assertThat(t.variants()).hasSize(2));
    }

    // TC-GOV-012
    @Test
    void notificationsAndEmailsUseTheEditedTextInTheRecipientsLanguage() {
        Customer asha = customer();
        Customer luis = customer();
        preferenceService.updatePreferences(luis.getId(), "es", null, null, null, false);
        templates.save("NOTIF_MEMBERSHIP_CHANGED", "en", new SaveTemplateRequest("Membership update", "Status: {status}."), "s");

        notificationService.notifyTemplated(asha.getId(), null, NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
            "NOTIF_MEMBERSHIP_CHANGED", Map.of("status", "SUSPENDED"));
        notificationService.notifyTemplated(luis.getId(), null, NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
            "NOTIF_MEMBERSHIP_CHANGED", Map.of("status", "SUSPENDED"));

        assertThat(notificationService.listNotifications(asha.getId()).notifications())
            .anyMatch(n -> n.title().equals("Membership update") && n.message().equals("Status: SUSPENDED."));
        assertThat(notificationService.listNotifications(luis.getId()).notifications())
            .anyMatch(n -> n.title().equals("Su pertenencia a la organización cambió") && n.message().equals("El estado de su pertenencia ahora es SUSPENDED."));

        // an email: the edited password-reset wording goes out, with the link filled in
        templates.save("EMAIL_PASSWORD_RESET", "en", new SaveTemplateRequest("Choose a new password", "Click {link} to choose a new password."), "s");
        emailService.sendPasswordResetEmail(asha.getEmail(), "https://eis.example/reset?token=abc");
        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(sent.capture());
        assertThat(sent.getAllValues()).anyMatch(m -> "Choose a new password".equals(m.getSubject())
            && "Click https://eis.example/reset?token=abc to choose a new password.".equals(m.getText()));
    }

    // TC-GOV-013
    @Test
    void theLanguageIsTheRecipientsOwnThenThePlatformDefaultThenEnglish() {
        Customer plain = customer();
        Customer spanish = customer();
        preferenceService.updatePreferences(spanish.getId(), "es", null, null, null, false);
        assertThat(languageOf(plain)).isEqualTo("en");

        defaults.save(new SavePlatformDefaultsRequest("es", null, null, null, null, null), null, "test");
        assertThat(languageOf(plain)).isEqualTo("es");
        preferenceService.updatePreferences(plain.getId(), "en", null, null, null, false);
        assertThat(languageOf(plain)).isEqualTo("en"); // the person's own choice wins
        assertThat(languageOf(spanish)).isEqualTo("es");
    }

    @Autowired private MessageLanguageResolver languageResolver;

    private String languageOf(Customer c) {
        assertThat(languageResolver.forEmail(c.getEmail())).isEqualTo(languageResolver.forCustomer(c.getId()));
        return languageResolver.forCustomer(c.getId());
    }

    // TC-GOV-014
    @Test
    void platformDefaultsStartEmptyAreValidatedAndAudited() {
        PlatformDefaultsDto initial = defaults.get();
        assertThat(initial.language()).isNull();
        assertThat(initial.region()).isNull();
        assertThat(initial.currency()).isNull();
        assertThat(initial.timeZone()).isNull();
        assertThat(initial.dateFormat()).isNull();
        assertThat(initial.reminderDays()).isBetween(1, 30);

        PlatformDefaultsDto saved = defaults.save(new SavePlatformDefaultsRequest("es", "en-IN", "inr", "Asia/Kolkata", "DD/MM/YYYY", 5), null, "sub-admin");
        assertThat(saved.language()).isEqualTo("es");
        assertThat(saved.region()).isEqualTo("en-IN");
        assertThat(saved.currency()).isEqualTo("INR");
        assertThat(saved.timeZone()).isEqualTo("Asia/Kolkata");
        assertThat(saved.dateFormat()).isEqualTo("DD/MM/YYYY");
        assertThat(saved.reminderDays()).isEqualTo(5);
        assertThat(billingSettingsService.renewalReminderDefaults().daysBefore()).isEqualTo(5); // one value for the reminder job
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "PLATFORM_DEFAULTS_CHANGED".equals(a.getAction()) && a.getDetail().contains("language (none) -> es"));

        assertThatThrownBy(() -> defaults.save(new SavePlatformDefaultsRequest("fr", null, null, null, null, null), null, "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> defaults.save(new SavePlatformDefaultsRequest(null, "zz-ZZ", null, null, null, null), null, "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> defaults.save(new SavePlatformDefaultsRequest(null, null, "XXX", null, null, null), null, "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> defaults.save(new SavePlatformDefaultsRequest(null, null, null, "Mars/Olympus", null, null), null, "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> defaults.save(new SavePlatformDefaultsRequest(null, null, null, null, "YY-MM", null), null, "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> defaults.save(new SavePlatformDefaultsRequest(null, null, null, null, null, 31), null, "s")).isInstanceOf(IllegalArgumentException.class);

        PlatformCurrency disabled = new PlatformCurrency();
        disabled.setCode("ZZZ");
        disabled.setName("Test");
        disabled.setEnabled(false);
        currencyRepository.save(disabled);
        assertThatThrownBy(() -> defaults.save(new SavePlatformDefaultsRequest(null, null, "ZZZ", null, null, null), null, "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("disabled");
        currencyRepository.delete(disabled);

        // a blank value clears a default; reminder days are left alone when not sent
        PlatformDefaultsDto cleared = defaults.save(new SavePlatformDefaultsRequest("", null, null, null, null, null), null, "s");
        assertThat(cleared.language()).isNull();
        assertThat(cleared.region()).isNull();
        assertThat(cleared.reminderDays()).isEqualTo(5);
    }

    // TC-GOV-015
    @Test
    void theDefaultsReachEveryUsersPreferencesSoTheyStartWithThem() {
        defaults.save(new SavePlatformDefaultsRequest("es", "en-GB", "INR", "Europe/London", "YYYY-MM-DD", null), null, "s");
        Customer fresh = customer();

        var prefs = preferenceService.getPreferences(fresh.getId());
        assertThat(prefs.language()).isNull(); // the person has not chosen anything yet
        assertThat(prefs.defaults().language()).isEqualTo("es");
        assertThat(prefs.defaults().timeZone()).isEqualTo("Europe/London");
        assertThat(prefs.defaults().dateFormat()).isEqualTo("YYYY-MM-DD");
        assertThat(prefs.defaults().currency()).isEqualTo("INR");

        var saved = preferenceService.updatePreferences(fresh.getId(), "en", null, null, null, false);
        assertThat(saved.language()).isEqualTo("en");
        assertThat(saved.defaults().language()).isEqualTo("es");
    }
}
