package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus;
import com.vyoog.eisplatform.modules.integration.repository.EventHandlerReceiptRepository;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-INT-002 (C62): outbox publishing, dispatch, retries, ordering,
 * idempotent receipts and the admin retry. TC-INT-011..017. */
@SpringBootTest(properties = "app.events.dispatcher.batch-size=100000")
@ActiveProfiles("test")
class OutboxDispatcherTest {

    static final List<String> RECORDED = new CopyOnWriteArrayList<>();
    static final Set<String> FAIL_AGGREGATES = ConcurrentHashMap.newKeySet();
    static final Map<String, Integer> SECOND_HANDLER_CALLS = new ConcurrentHashMap<>();

    @TestConfiguration
    static class Handlers {
        @Bean
        PlatformEventHandler recordingHandler() {
            return new PlatformEventHandler() {
                public String name() { return "test-recorder"; }
                public boolean handles(String type) { return type.startsWith("Test"); }
                public void handle(PlatformEvent event) { RECORDED.add(event.aggregateId() + ":" + event.eventType()); }
            };
        }

        @Bean
        PlatformEventHandler failingHandler() {
            return new PlatformEventHandler() {
                public String name() { return "test-failing"; }
                public boolean handles(String type) { return type.startsWith("Test"); }
                public void handle(PlatformEvent event) {
                    SECOND_HANDLER_CALLS.merge(event.eventId(), 1, Integer::sum);
                    if (FAIL_AGGREGATES.contains(event.aggregateId())) {
                        throw new IllegalStateException("downstream unavailable");
                    }
                }
            };
        }
    }

    @Autowired private OutboxService outboxService;
    @Autowired private OutboxDispatcher dispatcher;
    @Autowired private AdminEventService adminEventService;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private EventHandlerReceiptRepository receiptRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void reset() {
        FAIL_AGGREGATES.clear();
    }

    private static String aggregate() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    private OutboxEvent reload(OutboxEvent event) {
        return outboxEventRepository.findById(event.getId()).orElseThrow();
    }

    @Test
    void anEventIsWrittenOnlyWhenItsTransactionCommits() {
        String committed = aggregate();
        String rolledBack = aggregate();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(s -> outboxService.publish("TestCommitted", "TestAggregate", committed, Map.of("n", 1)));
        tx.executeWithoutResult(s -> {
            outboxService.publish("TestRolledBack", "TestAggregate", rolledBack, Map.of("n", 2));
            s.setRollbackOnly();
        });

        assertThat(outboxEventRepository.findByAggregateTypeAndAggregateIdOrderByIdAsc("TestAggregate", committed)).hasSize(1);
        assertThat(outboxEventRepository.findByAggregateTypeAndAggregateIdOrderByIdAsc("TestAggregate", rolledBack)).isEmpty();
    }

    @Test
    void aDueEventIsDeliveredToItsHandlersAndReceiptsAreRecorded() {
        String id = aggregate();
        OutboxEvent event = outboxService.publish("TestDelivered", "TestAggregate", id, Map.of("subscriptionId", 1));
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getPayload()).isEqualTo("{\"subscriptionId\":1}");

        dispatcher.dispatchDue();

        OutboxEvent delivered = reload(event);
        assertThat(delivered.getStatus()).isEqualTo(OutboxEventStatus.DELIVERED);
        assertThat(delivered.getAttempts()).isEqualTo(1);
        assertThat(delivered.getDeliveredAt()).isNotNull();
        assertThat(RECORDED).contains(id + ":TestDelivered");
        assertThat(receiptRepository.findByEventIdOrderByProcessedAtAsc(event.getEventId()))
            .extracting(r -> r.getHandlerName()).containsExactlyInAnyOrder("test-recorder", "test-failing");
    }

    @Test
    void anEventWithNoHandlerIsDelivered() {
        OutboxEvent event = outboxService.publish("NobodyListens", "TestAggregate", aggregate(), Map.of());
        dispatcher.dispatchDue();
        assertThat(reload(event).getStatus()).isEqualTo(OutboxEventStatus.DELIVERED);
    }

    @Test
    void aFailureIsRetriedLaterAndEventuallyFails() {
        String id = aggregate();
        FAIL_AGGREGATES.add(id);
        OutboxEvent event = outboxService.publish("TestFailing", "TestAggregate", id, Map.of());

        dispatcher.dispatchDue();
        OutboxEvent afterFirst = reload(event);
        assertThat(afterFirst.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(afterFirst.getAttempts()).isEqualTo(1);
        assertThat(afterFirst.getNextAttemptAt()).isAfter(Instant.now().plusSeconds(20));
        assertThat(afterFirst.getLastError()).contains("test-failing", "downstream unavailable");

        // not due yet: nothing happens
        dispatcher.dispatchDue();
        assertThat(reload(event).getAttempts()).isEqualTo(1);

        // the last allowed attempt
        afterFirst.setAttempts(9);
        afterFirst.setNextAttemptAt(Instant.now().minusSeconds(1));
        outboxEventRepository.save(afterFirst);
        dispatcher.dispatchDue();
        OutboxEvent failed = reload(event);
        assertThat(failed.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(failed.getAttempts()).isEqualTo(10);
        assertThat(failed.getNextAttemptAt()).isNull();
    }

    @Test
    void aHandlerThatAlreadySucceededIsNotCalledAgainOnRetry() {
        String id = aggregate();
        FAIL_AGGREGATES.add(id);
        OutboxEvent event = outboxService.publish("TestIdempotent", "TestAggregate", id, Map.of());
        dispatcher.dispatchDue();
        long recorderCalls = RECORDED.stream().filter(r -> r.equals(id + ":TestIdempotent")).count();
        assertThat(recorderCalls).isEqualTo(1);

        FAIL_AGGREGATES.remove(id);
        OutboxEvent pending = reload(event);
        pending.setNextAttemptAt(Instant.now().minusSeconds(1));
        outboxEventRepository.save(pending);
        dispatcher.dispatchDue();

        assertThat(reload(event).getStatus()).isEqualTo(OutboxEventStatus.DELIVERED);
        assertThat(RECORDED.stream().filter(r -> r.equals(id + ":TestIdempotent")).count()).isEqualTo(1);
        assertThat(SECOND_HANDLER_CALLS.get(event.getEventId())).isEqualTo(2);
    }

    @Test
    void aLaterEventOfTheSameAggregateWaitsForTheEarlierOne() {
        String id = aggregate();
        FAIL_AGGREGATES.add(id);
        OutboxEvent first = outboxService.publish("TestFirst", "TestAggregate", id, Map.of());
        OutboxEvent second = outboxService.publish("TestSecond", "TestAggregate", id, Map.of());

        dispatcher.dispatchDue();
        assertThat(reload(first).getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(reload(second).getAttempts()).isZero();
        assertThat(RECORDED).doesNotContain(id + ":TestSecond");

        FAIL_AGGREGATES.remove(id);
        OutboxEvent retry = reload(first);
        retry.setNextAttemptAt(Instant.now().minusSeconds(1));
        outboxEventRepository.save(retry);
        dispatcher.dispatchDue();

        assertThat(reload(first).getStatus()).isEqualTo(OutboxEventStatus.DELIVERED);
        assertThat(reload(second).getStatus()).isEqualTo(OutboxEventStatus.DELIVERED);
        assertThat(RECORDED.indexOf(id + ":TestFirst")).isLessThan(RECORDED.indexOf(id + ":TestSecond"));
    }

    @Test
    void anAdministratorCanRetryOnlyAFailedEvent() {
        String id = aggregate();
        OutboxEvent event = outboxService.publish("TestRetry", "TestAggregate", id, Map.of());
        assertThatThrownBy(() -> adminEventService.retry(event.getId(), "admin-sub")).isInstanceOf(InvalidStateException.class);

        OutboxEvent failed = reload(event);
        failed.setStatus(OutboxEventStatus.FAILED);
        failed.setAttempts(10);
        failed.setNextAttemptAt(null);
        outboxEventRepository.save(failed);

        var detail = adminEventService.retry(event.getId(), "admin-sub");
        assertThat(detail.status()).isEqualTo("PENDING");
        assertThat(detail.attempts()).isZero();
        dispatcher.dispatchDue();
        assertThat(reload(event).getStatus()).isEqualTo(OutboxEventStatus.DELIVERED);
    }

    @Test
    void theAdminListFiltersByTypeAndStatus() {
        String type = "TestListed" + aggregate().replace("-", "");
        outboxService.publish(type, "TestAggregate", aggregate(), Map.of());
        var page = adminEventService.list(type, "PENDING", null, null, 0);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.items().get(0).eventType()).isEqualTo(type);
        assertThat(adminEventService.list(type, "FAILED", null, null, 0).totalElements()).isZero();
        assertThat(adminEventService.eventTypes()).contains(type);
        assertThatThrownBy(() -> adminEventService.list(null, "LOST", null, null, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theRetryDelayDoublesAndIsCapped() {
        assertThat(dispatcher.delayAfter(1)).isEqualTo(Duration.ofSeconds(30));
        assertThat(dispatcher.delayAfter(2)).isEqualTo(Duration.ofMinutes(1));
        assertThat(dispatcher.delayAfter(3)).isEqualTo(Duration.ofMinutes(2));
        assertThat(dispatcher.delayAfter(30)).isEqualTo(Duration.ofHours(6));
    }

    @Test
    void deliveredEventsOlderThanTheRetentionPeriodAreRemoved() {
        OutboxEvent old = outboxService.publish("NobodyListens", "TestAggregate", aggregate(), Map.of());
        old.setStatus(OutboxEventStatus.DELIVERED);
        old.setDeliveredAt(Instant.now().minus(Duration.ofDays(31)));
        outboxEventRepository.save(old);
        OutboxEvent recent = outboxService.publish("NobodyListens", "TestAggregate", aggregate(), Map.of());
        recent.setStatus(OutboxEventStatus.DELIVERED);
        recent.setDeliveredAt(Instant.now().minus(Duration.ofDays(2)));
        outboxEventRepository.save(recent);

        dispatcher.purgeDelivered(30);

        assertThat(outboxEventRepository.findById(old.getId())).isEmpty();
        assertThat(outboxEventRepository.findById(recent.getId())).isPresent();
    }
}
