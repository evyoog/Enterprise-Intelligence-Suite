package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.integration.dto.ReplayRequest;
import com.vyoog.eisplatform.modules.integration.dto.ReplayResultDto;
import com.vyoog.eisplatform.modules.integration.model.EventHandlerReceipt;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus;
import com.vyoog.eisplatform.modules.integration.repository.EventHandlerReceiptRepository;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * REQ-INT-002.9 (C91, EV-1 B): replay. A platform administrator re-sends the delivered events of a time range to ONE
 * receiver (an in-application handler), oldest first — for example after a product was down for a day. Only DELIVERED
 * events are replayed (a FAILED one is retried, a PENDING one is still on its way) and only those the receiver handles;
 * other receivers are not called and the event's own status is not changed. A receiver must therefore be safe to run
 * twice. Events are available as long as the retention period keeps them. One replay is limited to
 * {@code app.events.replay.max-events} events (default 1000); a wider range is refused so it can be split.
 */
@Service
@Slf4j
public class EventReplayService {

    static final int MAX_REPORTED_FAILURES = 20;

    private final OutboxEventRepository outboxEventRepository;
    private final EventHandlerReceiptRepository receiptRepository;
    private final ObjectProvider<PlatformEventHandler> handlers;
    private final AuditService auditService;
    private final TransactionTemplate tx;
    private final int maxEvents;

    public EventReplayService(OutboxEventRepository outboxEventRepository, EventHandlerReceiptRepository receiptRepository,
                              ObjectProvider<PlatformEventHandler> handlers, AuditService auditService,
                              PlatformTransactionManager transactionManager,
                              @Value("${app.events.replay.max-events:1000}") int maxEvents) {
        this.outboxEventRepository = outboxEventRepository;
        this.receiptRepository = receiptRepository;
        this.handlers = handlers;
        this.auditService = auditService;
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.maxEvents = maxEvents;
    }

    /** The names of the receivers that can be chosen, in a stable order. */
    public List<String> receivers() {
        return handlers.orderedStream().map(PlatformEventHandler::name).distinct().sorted().toList();
    }

    public ReplayResultDto replay(ReplayRequest request, String actorKeycloakSub) {
        if (request == null || request.receiver() == null || request.receiver().isBlank()) {
            throw new IllegalArgumentException("Choose a receiver.");
        }
        if (request.from() == null || request.to() == null) {
            throw new IllegalArgumentException("Choose the first and last day to replay.");
        }
        if (request.to().isBefore(request.from())) {
            throw new IllegalArgumentException("The end date must not be before the start date.");
        }
        PlatformEventHandler handler = handlers.orderedStream()
            .filter(h -> h.name().equals(request.receiver().trim())).findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Instant start = request.from().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = request.to().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        String type = request.eventType() == null || request.eventType().isBlank() ? null : request.eventType().trim();
        Specification<OutboxEvent> spec = (root, q, cb) -> cb.and(
            cb.equal(root.get("status"), OutboxEventStatus.DELIVERED),
            cb.greaterThanOrEqualTo(root.<Instant>get("occurredAt"), start),
            cb.lessThan(root.<Instant>get("occurredAt"), end));
        if (type != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("eventType"), type));
        }
        Sort order = Sort.by(Sort.Order.asc("occurredAt"), Sort.Order.asc("id"));
        // The receiver's own type filter is applied in memory (a handler is a predicate, not a list of types).
        List<OutboxEvent> candidates = outboxEventRepository.findAll(spec, order).stream()
            .filter(e -> handler.handles(e.getEventType())).toList();
        long matched = candidates.size();

        if (request.preview()) {
            return new ReplayResultDto(true, handler.name(), matched, 0, 0, maxEvents, List.of());
        }
        if (matched > maxEvents) {
            throw new IllegalArgumentException(matched + " events match; one replay is limited to " + maxEvents
                + ". Choose a shorter range or an event type.");
        }

        int replayed = 0;
        int failed = 0;
        List<ReplayResultDto.Failure> failures = new ArrayList<>();
        for (OutboxEvent event : candidates) {
            PlatformEvent view = new PlatformEvent(event.getEventId(), event.getEventType(), event.getAggregateType(),
                event.getAggregateId(), event.getOccurredAt(), event.getPayload());
            try {
                tx.executeWithoutResult(status -> {
                    handler.handle(view);
                    if (!receiptRepository.existsByHandlerNameAndEventId(handler.name(), event.getEventId())) {
                        EventHandlerReceipt receipt = new EventHandlerReceipt();
                        receipt.setHandlerName(handler.name());
                        receipt.setEventId(event.getEventId());
                        receipt.setProcessedAt(Instant.now());
                        receiptRepository.save(receipt);
                    }
                });
                replayed++;
            } catch (RuntimeException e) {
                failed++;
                log.warn("Replay of event {} to {} failed: {}", event.getEventId(), handler.name(), e.getMessage());
                if (failures.size() < MAX_REPORTED_FAILURES) {
                    String error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                    failures.add(new ReplayResultDto.Failure(event.getEventId(), event.getEventType(),
                        error.length() > 300 ? error.substring(0, 300) : error));
                }
            }
        }
        auditService.recordSuccess("EVENTS_REPLAYED", actorKeycloakSub, null, null, "EventReceiver", handler.name(), null,
            "Replayed to " + handler.name() + " from " + request.from() + " to " + request.to()
                + (type == null ? "" : " (" + type + ")") + ": " + replayed + " replayed, " + failed + " failed");
        return new ReplayResultDto(false, handler.name(), matched, replayed, failed, maxEvents, failures);
    }
}
