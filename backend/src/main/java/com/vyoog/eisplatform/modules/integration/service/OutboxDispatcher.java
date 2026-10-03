package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.modules.integration.model.EventHandlerReceipt;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus;
import com.vyoog.eisplatform.modules.integration.repository.EventHandlerReceiptRepository;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * REQ-INT-002.3–.5: delivers due PENDING events, oldest first, to the
 * handlers registered for their type. At least once: each handler runs in
 * its own transaction together with its receipt, so a handler that already
 * succeeded is skipped when the event is retried. A failure schedules the
 * next attempt with a doubling delay; after the maximum attempts the event
 * is FAILED. An event with no handler is DELIVERED. A later event of the
 * same aggregate waits while an earlier one is not DELIVERED.
 *
 * <p>Single application instance assumed (as everywhere else in this
 * backend's scheduled jobs); several instances would need row locking.
 */
@Service
@Slf4j
public class OutboxDispatcher {

    private final OutboxEventRepository outboxEventRepository;
    private final EventHandlerReceiptRepository receiptRepository;
    private final ObjectProvider<PlatformEventHandler> handlers;
    private final TransactionTemplate tx;
    private final int batchSize;
    private final int maxAttempts;
    private final long initialDelaySeconds;
    private final long maxDelaySeconds;

    public OutboxDispatcher(OutboxEventRepository outboxEventRepository,
                            EventHandlerReceiptRepository receiptRepository,
                            ObjectProvider<PlatformEventHandler> handlers,
                            PlatformTransactionManager transactionManager,
                            @Value("${app.events.dispatcher.batch-size:100}") int batchSize,
                            @Value("${app.events.dispatcher.max-attempts:10}") int maxAttempts,
                            @Value("${app.events.dispatcher.initial-delay-seconds:30}") long initialDelaySeconds,
                            @Value("${app.events.dispatcher.max-delay-seconds:21600}") long maxDelaySeconds) {
        this.outboxEventRepository = outboxEventRepository;
        this.receiptRepository = receiptRepository;
        this.handlers = handlers;
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.initialDelaySeconds = initialDelaySeconds;
        this.maxDelaySeconds = maxDelaySeconds;
    }

    /** One dispatcher run. Returns how many events became DELIVERED. */
    public int dispatchDue() {
        List<OutboxEvent> due = outboxEventRepository.findDue(OutboxEventStatus.PENDING, Instant.now(), PageRequest.of(0, batchSize));
        int delivered = 0;
        for (OutboxEvent event : due) {
            if (outboxEventRepository.existsEarlierUndelivered(event.getAggregateType(), event.getAggregateId(),
                    event.getOccurredAt(), event.getId())) {
                continue;
            }
            if (deliver(event)) {
                delivered++;
            }
        }
        return delivered;
    }

    private boolean deliver(OutboxEvent event) {
        PlatformEvent view = new PlatformEvent(event.getEventId(), event.getEventType(), event.getAggregateType(),
            event.getAggregateId(), event.getOccurredAt(), event.getPayload());
        for (PlatformEventHandler handler : handlers.orderedStream().toList()) {
            if (!handler.handles(event.getEventType())) {
                continue;
            }
            if (receiptRepository.existsByHandlerNameAndEventId(handler.name(), event.getEventId())) {
                continue;
            }
            try {
                tx.executeWithoutResult(status -> {
                    handler.handle(view);
                    EventHandlerReceipt receipt = new EventHandlerReceipt();
                    receipt.setHandlerName(handler.name());
                    receipt.setEventId(event.getEventId());
                    receipt.setProcessedAt(Instant.now());
                    receiptRepository.save(receipt);
                });
            } catch (RuntimeException e) {
                recordFailure(event.getId(), handler.name(), e);
                return false;
            }
        }
        tx.executeWithoutResult(status -> outboxEventRepository.findById(event.getId()).ifPresent(current -> {
            current.setAttempts(current.getAttempts() + 1);
            current.setStatus(OutboxEventStatus.DELIVERED);
            current.setDeliveredAt(Instant.now());
            current.setNextAttemptAt(null);
            current.setLastError(null);
        }));
        return true;
    }

    private void recordFailure(Long id, String handlerName, RuntimeException error) {
        log.warn("Event {} failed in handler {}: {}", id, handlerName, error.getMessage());
        tx.executeWithoutResult(status -> outboxEventRepository.findById(id).ifPresent(current -> {
            int attempts = current.getAttempts() + 1;
            current.setAttempts(attempts);
            String message = handlerName + ": " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
            current.setLastError(message.length() > 1000 ? message.substring(0, 1000) : message);
            if (attempts >= maxAttempts) {
                current.setStatus(OutboxEventStatus.FAILED);
                current.setNextAttemptAt(null);
            } else {
                current.setNextAttemptAt(Instant.now().plus(delayAfter(attempts)));
            }
        }));
    }

    /** BR-3: 30 s, 1 min, 2 min, … doubling, capped (defaults). */
    Duration delayAfter(int attempts) {
        long seconds = initialDelaySeconds;
        for (int i = 1; i < attempts && seconds < maxDelaySeconds; i++) {
            seconds *= 2;
        }
        return Duration.ofSeconds(Math.min(seconds, maxDelaySeconds));
    }

    /** REQ-INT-002.7: removes DELIVERED events older than the retention period. */
    public int purgeDelivered(int retentionDays) {
        Integer removed = tx.execute(status -> outboxEventRepository.deleteDeliveredBefore(Instant.now().minus(Duration.ofDays(retentionDays))));
        return removed == null ? 0 : removed;
    }
}
