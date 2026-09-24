package com.vyoog.eisplatform.modules.audit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 25: one real, already-occurred security/administrative event. Kept
 * intentionally loose-coupled — plain string/id fields, no foreign keys to
 * Customer/Organization/etc — since an audit record must remain readable
 * even after the thing it refers to is later renamed or deleted (that's the
 * whole point of an audit trail), and because this module is written to by
 * almost every other module (auth, registration, authorization) the same
 * one-directional-sink way NotificationService already is (see AuditService).
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    @Column(nullable = false, length = 60)
    private String action;

    @Column(name = "actor_keycloak_sub")
    private String actorKeycloakSub;

    @Column(name = "actor_customer_id")
    private Long actorCustomerId;

    @Column(name = "actor_email")
    private String actorEmail;

    @Column(name = "target_type", length = 60)
    private String targetType;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(nullable = false, length = 20)
    private String outcome = "SUCCESS";

    @Column(length = 1000)
    private String detail;
}
