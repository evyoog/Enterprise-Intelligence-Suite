package com.vyoog.eisplatform.modules.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One immutable audit record for one lifecycle event on one
 * {@link PrivilegedAccessRequest} — never updated after insert, never
 * deleted. {@code actorKeycloakSub} is whoever caused the event (the
 * requester for REQUESTED/REVOKED-by-requester, the approver for
 * APPROVED/REJECTED/REVOKED-by-approver) — always a real identity, never
 * inferred.
 */
@Entity
@Table(name = "privileged_access_audit_entry")
@Getter
@Setter
public class PrivilegedAccessAuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private PrivilegedAccessEventType eventType;

    @Column(name = "actor_keycloak_sub", nullable = false)
    private String actorKeycloakSub;

    @Column(nullable = false)
    private Instant occurredAt = Instant.now();

    @Column(length = 500)
    private String note;
}
