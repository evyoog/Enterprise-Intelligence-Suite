package com.vyoog.eisplatform.modules.servicestatus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C101 / REQ-SUP-003 (IC-2 A): the root cause behind one or more incidents (incidents point to it with {@code problem_id}), with corrective actions. */
@Entity
@Table(name = "problem")
@Getter
@Setter
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ProblemStatus status = ProblemStatus.OPEN;

    @Column(name = "root_cause", length = 4000)
    private String rootCause;

    @Column(name = "created_by_keycloak_sub", length = 100)
    private String createdByKeycloakSub;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;
}
