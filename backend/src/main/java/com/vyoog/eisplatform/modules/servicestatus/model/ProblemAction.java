package com.vyoog.eisplatform.modules.servicestatus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** C101 / REQ-SUP-003: one corrective action of a problem. */
@Entity
@Table(name = "problem_action")
@Getter
@Setter
public class ProblemAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 100)
    private String owner;

    @Column(name = "due_on")
    private LocalDate dueOn;

    @Column(nullable = false)
    private boolean done;

    @Column(name = "done_at")
    private Instant doneAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
