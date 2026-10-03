package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.integration.dto.EventReceiptDto;
import com.vyoog.eisplatform.modules.integration.dto.OutboxEventDetailDto;
import com.vyoog.eisplatform.modules.integration.dto.OutboxEventPageDto;
import com.vyoog.eisplatform.modules.integration.dto.OutboxEventSummaryDto;
import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.model.OutboxEventStatus;
import com.vyoog.eisplatform.modules.integration.repository.EventHandlerReceiptRepository;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

/** REQ-INT-002.6: the platform administrator's events view and retry (BR-7). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventService {

    public static final int PAGE_SIZE = 20;

    private final OutboxEventRepository outboxEventRepository;
    private final EventHandlerReceiptRepository receiptRepository;
    private final AuditService auditService;

    public OutboxEventPageDto list(String type, String status, LocalDate from, LocalDate to, int page) {
        Specification<OutboxEvent> spec = Specification.where(null);
        if (type != null && !type.isBlank()) {
            String exact = type.trim();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("eventType"), exact));
        }
        if (status != null && !status.isBlank()) {
            OutboxEventStatus parsed = parseStatus(status);
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), parsed));
        }
        if (from != null) {
            Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("occurredAt"), start));
        }
        if (to != null) {
            Instant end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            spec = spec.and((root, q, cb) -> cb.lessThan(root.<Instant>get("occurredAt"), end));
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("The end date must not be before the start date.");
        }
        int safePage = Math.max(page, 0);
        Page<OutboxEvent> result = outboxEventRepository.findAll(spec,
            PageRequest.of(safePage, PAGE_SIZE, Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"))));
        return new OutboxEventPageDto(result.getContent().stream().map(AdminEventService::summary).toList(),
            result.getTotalElements(), safePage, PAGE_SIZE);
    }

    public List<String> eventTypes() {
        return outboxEventRepository.findDistinctEventTypes();
    }

    public OutboxEventDetailDto get(Long id) {
        return detail(find(id));
    }

    @Transactional
    public OutboxEventDetailDto retry(Long id, String actorKeycloakSub) {
        OutboxEvent event = find(id);
        if (event.getStatus() != OutboxEventStatus.FAILED) {
            throw new InvalidStateException("Only a FAILED event can be retried.");
        }
        event.setStatus(OutboxEventStatus.PENDING);
        event.setAttempts(0);
        event.setNextAttemptAt(Instant.now());
        auditService.recordSuccess("EVENT_RETRIED", actorKeycloakSub, null, null, "OutboxEvent",
            String.valueOf(event.getId()), null, event.getEventType() + " " + event.getEventId());
        return detail(event);
    }

    private OutboxEvent find(Long id) {
        return outboxEventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private static OutboxEventStatus parseStatus(String status) {
        try {
            return OutboxEventStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    private static OutboxEventSummaryDto summary(OutboxEvent e) {
        return new OutboxEventSummaryDto(e.getId(), e.getEventId(), e.getEventType(), e.getAggregateType(), e.getAggregateId(),
            e.getOccurredAt(), e.getStatus().name(), e.getAttempts(), e.getNextAttemptAt(), e.getLastError());
    }

    private OutboxEventDetailDto detail(OutboxEvent e) {
        List<EventReceiptDto> receipts = receiptRepository.findByEventIdOrderByProcessedAtAsc(e.getEventId()).stream()
            .map(r -> new EventReceiptDto(r.getHandlerName(), r.getProcessedAt())).toList();
        return new OutboxEventDetailDto(e.getId(), e.getEventId(), e.getEventType(), e.getAggregateType(), e.getAggregateId(),
            e.getOccurredAt(), e.getStatus().name(), e.getAttempts(), e.getNextAttemptAt(), e.getLastError(),
            e.getDeliveredAt(), e.getPayload(), receipts);
    }
}
