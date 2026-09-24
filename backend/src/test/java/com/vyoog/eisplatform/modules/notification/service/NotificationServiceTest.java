package com.vyoog.eisplatform.modules.notification.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.auth.service.FakeEmailService;
import com.vyoog.eisplatform.modules.notification.dto.NotificationDto;
import com.vyoog.eisplatform.modules.notification.dto.NotificationPreferenceDto;
import com.vyoog.eisplatform.modules.notification.dto.NotificationSummaryDto;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Phase 18: uses a fake EmailService (never real SMTP), same convention as
 * PasswordResetServiceTest — see that class's own javadoc. */
@SpringBootTest
@ActiveProfiles("test")
@Import(NotificationServiceTest.TestConfig.class)
class NotificationServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        EmailService fakeEmailService() {
            return new FakeEmailService();
        }
    }

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    private FakeEmailService fakeEmail() {
        return (FakeEmailService) emailService;
    }

    @BeforeEach
    void resetFake() {
        fakeEmail().reset();
    }

    private static long nextCustomerId() {
        return System.nanoTime();
    }

    @Test
    void notifyRecordsAnInAppNotificationAndSendsEmailByDefault() {
        long customerId = nextCustomerId();

        notificationService.notify(customerId, "customer@example.com", NotificationCategory.SUBSCRIPTION,
            NotificationSeverity.INFO, "Subscribed", "You're now subscribed.");

        NotificationSummaryDto summary = notificationService.listNotifications(customerId);
        assertThat(summary.notifications()).hasSize(1);
        assertThat(summary.unreadCount()).isEqualTo(1);
        NotificationDto notification = summary.notifications().get(0);
        assertThat(notification.title()).isEqualTo("Subscribed");
        assertThat(notification.category()).isEqualTo(NotificationCategory.SUBSCRIPTION);
        assertThat(notification.read()).isFalse();

        assertThat(fakeEmail().lastNotificationEmail).isEqualTo("customer@example.com");
        assertThat(fakeEmail().lastNotificationSubject).isEqualTo("Subscribed");
    }

    @Test
    void notifyInAppOnlyNeverSendsEmail() {
        long customerId = nextCustomerId();

        notificationService.notifyInAppOnly(customerId, NotificationCategory.SECURITY,
            NotificationSeverity.WARNING, "Password changed", "Your password changed.");

        assertThat(notificationService.listNotifications(customerId).notifications()).hasSize(1);
        assertThat(fakeEmail().lastNotificationEmail).isNull();
    }

    @Test
    void mutingACategoryStopsItsEmailButNotTheInAppRecord() {
        long customerId = nextCustomerId();
        notificationService.updatePreferences(customerId, List.of(NotificationCategory.ORGANIZATION));

        notificationService.notify(customerId, "muted@example.com", NotificationCategory.ORGANIZATION,
            NotificationSeverity.INFO, "Access granted", "You have new access.");

        assertThat(notificationService.listNotifications(customerId).notifications()).hasSize(1);
        assertThat(fakeEmail().lastNotificationEmail).isNull();
    }

    @Test
    void mutingOneCategoryDoesNotAffectAnother() {
        long customerId = nextCustomerId();
        notificationService.updatePreferences(customerId, List.of(NotificationCategory.ORGANIZATION));

        notificationService.notify(customerId, "still-emailed@example.com", NotificationCategory.SUBSCRIPTION,
            NotificationSeverity.INFO, "Subscribed", "You're now subscribed.");

        assertThat(fakeEmail().lastNotificationEmail).isEqualTo("still-emailed@example.com");
    }

    @Test
    void markReadFlipsOnlyThatNotification() {
        long customerId = nextCustomerId();
        notificationService.notify(customerId, null, NotificationCategory.SYSTEM, NotificationSeverity.INFO, "One", "First");
        notificationService.notify(customerId, null, NotificationCategory.SYSTEM, NotificationSeverity.INFO, "Two", "Second");

        NotificationSummaryDto before = notificationService.listNotifications(customerId);
        Long firstId = before.notifications().stream().filter(n -> n.title().equals("One")).findFirst().orElseThrow().id();

        notificationService.markRead(customerId, firstId);

        NotificationSummaryDto after = notificationService.listNotifications(customerId);
        assertThat(after.unreadCount()).isEqualTo(1);
        assertThat(after.notifications().stream().filter(n -> n.id().equals(firstId)).findFirst().orElseThrow().read()).isTrue();
    }

    @Test
    void markReadOnAnotherCustomersNotificationIsForbidden() {
        long ownerId = nextCustomerId();
        long strangerId = nextCustomerId();
        notificationService.notify(ownerId, null, NotificationCategory.SYSTEM, NotificationSeverity.INFO, "Mine", "Body");
        Long notificationId = notificationService.listNotifications(ownerId).notifications().get(0).id();

        assertThatThrownBy(() -> notificationService.markRead(strangerId, notificationId))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void markAllReadClearsEveryUnreadNotificationForThatCustomer() {
        long customerId = nextCustomerId();
        notificationService.notify(customerId, null, NotificationCategory.SYSTEM, NotificationSeverity.INFO, "One", "First");
        notificationService.notify(customerId, null, NotificationCategory.SYSTEM, NotificationSeverity.INFO, "Two", "Second");

        notificationService.markAllRead(customerId);

        assertThat(notificationService.listNotifications(customerId).unreadCount()).isZero();
    }

    @Test
    void preferencesDefaultToNoCategoriesMuted() {
        long customerId = nextCustomerId();

        NotificationPreferenceDto preferences = notificationService.getPreferences(customerId);

        assertThat(preferences.emailDisabledCategories()).isEmpty();
    }

    @Test
    void updatePreferencesThenGetRoundTrips() {
        long customerId = nextCustomerId();

        notificationService.updatePreferences(customerId, List.of(NotificationCategory.SECURITY, NotificationCategory.SYSTEM));

        assertThat(notificationService.getPreferences(customerId).emailDisabledCategories())
            .containsExactlyInAnyOrder(NotificationCategory.SECURITY, NotificationCategory.SYSTEM);
    }
}
