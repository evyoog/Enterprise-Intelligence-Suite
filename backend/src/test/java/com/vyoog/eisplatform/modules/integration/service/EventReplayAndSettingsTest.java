package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.integration.dto.EventPlatformSettingsDto;
import com.vyoog.eisplatform.modules.integration.dto.ReplayRequest;
import com.vyoog.eisplatform.modules.integration.dto.ReplayResultDto;
import com.vyoog.eisplatform.modules.integration.dto.UpdateEventPlatformSettingsRequest;
import com.vyoog.eisplatform.modules.integration.job.OutboxRetentionJob;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus;
import com.vyoog.eisplatform.modules.integration.repository.EventHandlerReceiptRepository;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/** REQ-INT-002 (C91): replay of events to one receiver (EV-1 B), configurable retention (EV-3) and failure alerts (EV-4). TC-INT-020..026. */
@SpringBootTest(properties = {
    "app.events.dispatcher.batch-size=100000", "app.events.dispatcher.max-attempts=1", "app.events.replay.max-events=3"})
@ActiveProfiles("test")
class EventReplayAndSettingsTest {

    static final List<String> REPLAYED_A = new CopyOnWriteArrayList<>();
    static final List<String> REPLAYED_B = new CopyOnWriteArrayList<>();
    static final Set<String> FAIL = ConcurrentHashMap.newKeySet();

    @TestConfiguration
    static class Receivers {
        @Bean
        PlatformEventHandler receiverA() {
            return new PlatformEventHandler() {
                public String name() { return "replay-receiver-a"; }
                public boolean handles(String type) { return type.startsWith("Rp"); }
                public void handle(PlatformEvent event) {
                    if (FAIL.contains(event.aggregateId())) {
                        throw new IllegalStateException("product still down");
                    }
                    REPLAYED_A.add(event.aggregateId() + ":" + event.eventType());
                }
            };
        }

        @Bean
        PlatformEventHandler receiverB() {
            return new PlatformEventHandler() {
                public String name() { return "replay-receiver-b"; }
                public boolean handles(String type) { return type.startsWith("Rp"); }
                public void handle(PlatformEvent event) { REPLAYED_B.add(event.aggregateId()); }
            };
        }
    }

    @MockBean private EmailService emailService;

    @Autowired private OutboxService outboxService;
    @Autowired private OutboxDispatcher dispatcher;
    @Autowired private EventReplayService replayService;
    @Autowired private EventPlatformSettingsService settingsService;
    @Autowired private OutboxRetentionJob retentionJob;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private EventHandlerReceiptRepository receiptRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private NotificationService notificationService;

    @BeforeEach
    void reset() {
        REPLAYED_A.clear();
        REPLAYED_B.clear();
        FAIL.clear();
        receiptRepository.deleteAll();
        outboxEventRepository.deleteAll(); // each test replays "today", so it starts without other tests' events
        settingsService.update(new UpdateEventPlatformSettingsRequest(30, List.of(), true, true), "test");
    }

    private static String agg() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    private OutboxEvent delivered(String type, String aggregate, Instant occurredAt) {
        OutboxEvent event = outboxService.publish(type, "RpAggregate", aggregate, Map.of("n", 1));
        event.setOccurredAt(occurredAt);
        outboxEventRepository.save(event);
        dispatcher.dispatchDue();
        REPLAYED_A.clear();
        REPLAYED_B.clear();
        return outboxEventRepository.findById(event.getId()).orElseThrow();
    }

    private static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    private ReplayRequest req(String receiver, LocalDate from, LocalDate to, String type, boolean preview) {
        return new ReplayRequest(receiver, from, to, type, preview);
    }

    // TC-INT-020
    @Test
    void replayResendsTheDeliveredEventsOfARangeToOneReceiverOnly() {
        String a1 = agg(), a2 = agg(), old = agg();
        OutboxEvent e1 = delivered("RpCreated", a1, Instant.now().minus(Duration.ofHours(3)));
        delivered("RpRenewed", a2, Instant.now().minus(Duration.ofHours(2)));
        delivered("RpCreated", old, Instant.now().minus(Duration.ofDays(10)));

        ReplayResultDto result = replayService.replay(req("replay-receiver-a", today(), today(), null, false), "sub-1");

        assertThat(result.preview()).isFalse();
        assertThat(result.matched()).isEqualTo(2);
        assertThat(result.replayed()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        assertThat(REPLAYED_A).containsExactly(a1 + ":RpCreated", a2 + ":RpRenewed"); // oldest first, the 10-day-old one is outside the range
        assertThat(REPLAYED_B).isEmpty(); // other receivers are not called
        assertThat(outboxEventRepository.findById(e1.getId()).orElseThrow().getStatus()).isEqualTo(OutboxEventStatus.DELIVERED);
        assertThat(auditLogRepository.findAll()).anyMatch(l -> "EVENTS_REPLAYED".equals(l.getAction())
            && l.getDetail().contains("replay-receiver-a") && l.getDetail().contains("2 replayed"));
        assertThat(replayService.receivers()).contains("replay-receiver-a", "replay-receiver-b");

        // an event type narrows it further
        REPLAYED_A.clear();
        ReplayResultDto onlyRenewed = replayService.replay(req("replay-receiver-a", today(), today(), "RpRenewed", false), "sub-1");
        assertThat(onlyRenewed.replayed()).isEqualTo(1);
        assertThat(REPLAYED_A).containsExactly(a2 + ":RpRenewed");
    }

    // TC-INT-021
    @Test
    void aPreviewOnlyCountsAndPendingOrFailedEventsAreNotReplayed() {
        String delivered = agg();
        delivered("RpCreated", delivered, Instant.now().minus(Duration.ofHours(1)));
        OutboxEvent pending = outboxService.publish("RpCreated", "RpAggregate", agg(), Map.of());
        pending.setOccurredAt(Instant.now().minus(Duration.ofMinutes(30)));
        outboxEventRepository.save(pending);
        OutboxEvent failed = outboxService.publish("RpCreated", "RpAggregate", agg(), Map.of());
        failed.setStatus(OutboxEventStatus.FAILED);
        outboxEventRepository.save(failed);

        ReplayResultDto preview = replayService.replay(req("replay-receiver-a", today(), today(), "RpCreated", true), "sub-1");

        assertThat(preview.preview()).isTrue();
        assertThat(preview.matched()).isEqualTo(1);
        assertThat(preview.replayed()).isZero();
        assertThat(REPLAYED_A).isEmpty();
        assertThat(auditLogRepository.findAll()).noneMatch(l -> "EVENTS_REPLAYED".equals(l.getAction()) && l.getDetail().contains("RpCreated"));
    }

    // TC-INT-022
    @Test
    void aFailingEventDoesNotStopTheReplayAndIsReported() {
        String ok = agg(), bad = agg();
        delivered("RpCreated", ok, Instant.now().minus(Duration.ofHours(2)));
        delivered("RpCreated", bad, Instant.now().minus(Duration.ofHours(1)));
        FAIL.add(bad);

        ReplayResultDto result = replayService.replay(req("replay-receiver-a", today(), today(), "RpCreated", false), "sub-1");

        assertThat(result.replayed()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.failures()).hasSize(1);
        assertThat(result.failures().get(0).error()).contains("product still down");
        assertThat(REPLAYED_A).containsExactly(ok + ":RpCreated");
    }

    // TC-INT-023
    @Test
    void replayValidatesTheReceiverTheRangeAndTheSize() {
        assertThatThrownBy(() -> replayService.replay(req("nobody", today(), today(), null, false), "s")).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> replayService.replay(req(" ", today(), today(), null, false), "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> replayService.replay(req("replay-receiver-a", null, today(), null, false), "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> replayService.replay(req("replay-receiver-a", today(), today().minusDays(1), null, false), "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("end date");

        for (int i = 0; i < 4; i++) {
            delivered("RpBulk", agg(), Instant.now().minus(Duration.ofMinutes(10 + i)));
        }
        ReplayResultDto preview = replayService.replay(req("replay-receiver-a", today(), today(), "RpBulk", true), "s");
        assertThat(preview.matched()).isEqualTo(4);
        assertThat(preview.limit()).isEqualTo(3);
        assertThatThrownBy(() -> replayService.replay(req("replay-receiver-a", today(), today(), "RpBulk", false), "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("limited to 3");
        assertThat(REPLAYED_A).isEmpty();
    }

    // TC-INT-024
    @Test
    void retentionStartsAtThirtyDaysIsConfigurableAndValidated() {
        assertThat(settingsService.get().retentionDays()).isEqualTo(30);

        EventPlatformSettingsDto saved = settingsService.update(new UpdateEventPlatformSettingsRequest(90, null, null, null), "sub-admin");
        assertThat(saved.retentionDays()).isEqualTo(90);
        assertThat(settingsService.retentionDays()).isEqualTo(90);
        assertThat(auditLogRepository.findAll()).anyMatch(l -> "EVENT_SETTINGS_CHANGED".equals(l.getAction()) && l.getDetail().contains("30 -> 90"));

        assertThatThrownBy(() -> settingsService.update(new UpdateEventPlatformSettingsRequest(0, null, null, null), "s")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> settingsService.update(new UpdateEventPlatformSettingsRequest(3651, null, null, null), "s")).isInstanceOf(IllegalArgumentException.class);
        assertThat(settingsService.retentionDays()).isEqualTo(90);
    }

    // TC-INT-025
    @Test
    void theDailyRetentionJobUsesTheConfiguredPeriod() {
        settingsService.update(new UpdateEventPlatformSettingsRequest(5, null, null, null), "s");
        OutboxEvent sixDays = outboxService.publish("NobodyListens", "RpAggregate", agg(), Map.of());
        sixDays.setStatus(OutboxEventStatus.DELIVERED);
        sixDays.setDeliveredAt(Instant.now().minus(Duration.ofDays(6)));
        outboxEventRepository.save(sixDays);
        OutboxEvent twoDays = outboxService.publish("NobodyListens", "RpAggregate", agg(), Map.of());
        twoDays.setStatus(OutboxEventStatus.DELIVERED);
        twoDays.setDeliveredAt(Instant.now().minus(Duration.ofDays(2)));
        outboxEventRepository.save(twoDays);

        retentionJob.run();

        assertThat(outboxEventRepository.findById(sixDays.getId())).isEmpty();
        assertThat(outboxEventRepository.findById(twoDays.getId())).isPresent();
    }

    // TC-INT-026
    @Test
    void anEventThatFailsForGoodAlertsTheListedPeopleInTheAppAndByEmail() {
        Customer admin = new Customer();
        admin.setEmail("ops-" + System.nanoTime() + "@events.example");
        admin.setFirstName("Ops");
        admin.setLastName("Admin");
        admin = customerRepository.save(admin);
        String outsider = "outsider-" + System.nanoTime() + "@events.example";
        EventPlatformSettingsDto settings = settingsService.update(
            new UpdateEventPlatformSettingsRequest(null, List.of(admin.getEmail().toUpperCase(), outsider + ", " + outsider), true, true), "s");
        assertThat(settings.alertRecipients()).containsExactly(admin.getEmail(), outsider); // lower-cased, de-duplicated

        String bad = agg();
        FAIL.add(bad);
        OutboxEvent event = outboxService.publish("RpCreated", "RpAggregate", bad, Map.of());
        dispatcher.dispatchDue(); // max attempts is 1, so this failure is final

        assertThat(outboxEventRepository.findById(event.getId()).orElseThrow().getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(notificationService.listNotifications(admin.getId()).notifications())
            .anyMatch(n -> n.title().equals("Event failed: RpCreated") && n.message().contains("Open Platform events"));
        verify(emailService).sendNotificationEmail(eq(admin.getEmail()), eq("Event failed: RpCreated"), contains("FAILED"));
        verify(emailService).sendNotificationEmail(eq(outsider), eq("Event failed: RpCreated"), contains("FAILED"));
    }

    @Test
    void alertRecipientsAreValidated() {
        assertThatThrownBy(() -> settingsService.update(new UpdateEventPlatformSettingsRequest(null, List.of("not-an-email"), null, null), "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not a valid email");
        List<String> many = java.util.stream.IntStream.range(0, 21).mapToObj(i -> "p" + i + "@x.example").toList();
        assertThatThrownBy(() -> settingsService.update(new UpdateEventPlatformSettingsRequest(null, many, null, null), "s"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("At most 20");
    }
}
