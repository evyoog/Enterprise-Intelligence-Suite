package com.vyoog.eisplatform.modules.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * REQ-INT-002.1 (C62): publishing an event = inserting an outbox row in the
 * caller's transaction (REQUIRED joins it), so the event exists exactly when
 * the business change commits. Payloads carry IDs, statuses, dates and
 * amounts only — never secrets or card data (BR-2, BR-BIL-001).
 */
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public OutboxEvent publish(String eventType, String aggregateType, Object aggregateId, Map<String, ?> payload) {
        Instant now = Instant.now();
        OutboxEvent event = new OutboxEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(String.valueOf(aggregateId));
        event.setOccurredAt(now);
        event.setCreatedAt(now);
        event.setNextAttemptAt(now);
        event.setStatus(OutboxEventStatus.PENDING);
        event.setPayload(toJson(payload));
        return outboxEventRepository.save(event);
    }

    private String toJson(Map<String, ?> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Event payload is not serializable", e);
        }
    }
}
