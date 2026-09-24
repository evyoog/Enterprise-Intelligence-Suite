package com.vyoog.eisplatform.modules.audit.service;

import com.vyoog.eisplatform.modules.audit.dto.AuditLogDto;
import com.vyoog.eisplatform.modules.audit.dto.AuditLogPageDto;
import com.vyoog.eisplatform.modules.audit.model.AuditLog;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Phase 25: the one seam every other module calls through to raise a real
 * audit record — deliberately depends on nothing from any other domain
 * module (same one-directional-sink shape as NotificationService, see that
 * class's own javadoc for why), so auth/registration/authorization can all
 * call this without creating a cycle back into their own internals.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(String action, String actorKeycloakSub, Long actorCustomerId, String actorEmail,
                        String targetType, String targetId, Long organizationId, String outcome, String detail) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setActorKeycloakSub(actorKeycloakSub);
        log.setActorCustomerId(actorCustomerId);
        log.setActorEmail(actorEmail);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOrganizationId(organizationId);
        log.setOutcome(outcome == null ? "SUCCESS" : outcome);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }

    /**
     * Convenience for the common "actor did something to a target" shape.
     * Needs its OWN {@code @Transactional} rather than relying on
     * {@link #record}'s — calling another method on {@code this} bypasses
     * Spring's AOP proxy entirely (the classic self-invocation pitfall), so
     * without this annotation the call would silently run under this
     * class's own {@code @Transactional(readOnly = true)} default instead
     * and fail outright on the INSERT (caught live: a failed login attempt
     * 500'd instead of returning 401, because the audit write it triggered
     * couldn't execute in a read-only transaction).
     */
    @Transactional
    public void recordSuccess(String action, String actorKeycloakSub, Long actorCustomerId, String actorEmail,
                               String targetType, String targetId, Long organizationId, String detail) {
        record(action, actorKeycloakSub, actorCustomerId, actorEmail, targetType, targetId, organizationId, "SUCCESS", detail);
    }

    /** Convenience for events with no resolved identity yet (e.g. a failed
     * login — there's no customer/keycloak-sub to blame, only the email that
     * was typed in). Same self-invocation reasoning as {@link #recordSuccess}
     * for why this needs its own {@code @Transactional}. */
    @Transactional
    public void recordFailure(String action, String actorEmail, String detail) {
        record(action, null, null, actorEmail, null, null, null, "FAILURE", detail);
    }

    public AuditLogPageDto search(Long organizationId, Long actorCustomerId, String action,
                                   Instant from, Instant to, int page, int size) {
        Specification<AuditLog> spec = Specification.where(null);
        if (organizationId != null) {
            spec = spec.and(AuditSpecifications.hasOrganizationId(organizationId));
        }
        if (actorCustomerId != null) {
            spec = spec.and(AuditSpecifications.hasActorCustomerId(actorCustomerId));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and(AuditSpecifications.hasAction(action));
        }
        if (from != null) {
            spec = spec.and(AuditSpecifications.timestampAfter(from));
        }
        if (to != null) {
            spec = spec.and(AuditSpecifications.timestampBefore(to));
        }

        int safeSize = size <= 0 || size > 200 ? 50 : size;
        int safePage = Math.max(page, 0);
        var result = auditLogRepository.findAll(spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "timestamp")));

        return new AuditLogPageDto(result.getContent().stream().map(this::toDto).toList(),
            result.getTotalElements(), safePage, safeSize);
    }

    private AuditLogDto toDto(AuditLog log) {
        return new AuditLogDto(log.getId(), log.getTimestamp(), log.getAction(), log.getActorCustomerId(),
            log.getActorEmail(), log.getTargetType(), log.getTargetId(), log.getOrganizationId(),
            log.getOutcome(), log.getDetail());
    }
}
