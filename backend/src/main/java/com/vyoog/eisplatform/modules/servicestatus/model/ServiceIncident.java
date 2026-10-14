package com.vyoog.eisplatform.modules.servicestatus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * REQ-PRT-001.2: an incident a platform admin posts for one product. Open
 * while {@link #endedAt} is null. Details are shown only to customers who
 * purchased the product (C26).
 * C101 / REQ-SUP-003 (IC-1 A): the same record is the support incident. The public
 * fields are title, message, product and the start and end times; the phase,
 * severity, internal notes, assignee, resolution summary, linked tickets and
 * problem are internal and never reach the status page.
 */
@Entity
@Table(name = "service_incident")
@Getter
@Setter
public class ServiceIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String message;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by_keycloak_sub")
    private String createdByKeycloakSub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private IncidentPhase phase = IncidentPhase.LOGGED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IncidentSeverity severity = IncidentSeverity.MINOR;

    @Column(name = "internal_notes", length = 4000)
    private String internalNotes;

    @Column(name = "assigned_to_customer_id")
    private Long assignedToCustomerId;

    @Column(name = "resolution_summary", length = 2000)
    private String resolutionSummary;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "problem_id")
    private Long problemId;

    /** Support tickets raised about this incident. */
    @ElementCollection
    @CollectionTable(name = "service_incident_ticket", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "ticket_id")
    private Set<Long> ticketIds = new LinkedHashSet<>();
}
