package com.vyoog.eisplatform.modules.audit.service;

import com.vyoog.eisplatform.modules.audit.model.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

final class AuditSpecifications {

    private AuditSpecifications() {
    }

    static Specification<AuditLog> hasOrganizationId(Long organizationId) {
        return (root, cq, cb) -> cb.equal(root.get("organizationId"), organizationId);
    }

    static Specification<AuditLog> hasActorCustomerId(Long actorCustomerId) {
        return (root, cq, cb) -> cb.equal(root.get("actorCustomerId"), actorCustomerId);
    }

    static Specification<AuditLog> hasAction(String action) {
        return (root, cq, cb) -> cb.equal(root.get("action"), action);
    }

    static Specification<AuditLog> hasActionIn(java.util.Collection<String> actions) {
        return (root, cq, cb) -> root.get("action").in(actions);
    }

    static Specification<AuditLog> timestampAfter(Instant from) {
        return (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), from);
    }

    static Specification<AuditLog> timestampBefore(Instant to) {
        return (root, cq, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), to);
    }
}
