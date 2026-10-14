package com.vyoog.eisplatform.modules.servicestatus.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** C101 / REQ-SUP-003 (IC-2 A): the problem API. */
public final class ProblemDtos {

    private ProblemDtos() {
    }

    public record Problem(Long id, String title, String description, String status, String rootCause, int incidentCount, int actionCount, int openActionCount,
                          Instant createdAt, Instant resolvedAt, Instant closedAt) {
    }

    public record IncidentRef(Long id, String title, String productName, String phase, String severity, Instant startedAt) {
    }

    public record Action(Long id, String description, String owner, LocalDate dueOn, boolean done, Instant doneAt) {
    }

    public record Detail(Problem problem, List<IncidentRef> incidents, List<Action> actions) {
    }

    /** {@code incidentIds}: incidents to link right away. */
    public record CreateRequest(String title, String description, List<Long> incidentIds) {
    }

    /** Fields left null keep their value; {@code status} moves the problem along. */
    public record UpdateRequest(String title, String description, String rootCause, String status) {
    }

    public record ActionRequest(String description, String owner, LocalDate dueOn, Boolean done) {
    }
}
