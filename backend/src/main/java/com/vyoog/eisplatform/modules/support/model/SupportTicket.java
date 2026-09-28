package com.vyoog.eisplatform.modules.support.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 12.01.01 Ticket Management (sprint 2027.1.2). Not AI-driven ([C40]): the
 * roadmap's own "AI Agent" actor for these functions belongs to 12.02 AI
 * Support / 04b (later sprint, needs an agent/LLM framework this platform
 * doesn't have yet) — every action here is a human (the requester, or a
 * platform admin holding MANAGE_SUPPORT_TICKETS) instead.
 */
@Entity
@Table(name = "support_ticket")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requested_by_customer_id", nullable = false)
    private Long requestedByCustomerId;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String description;

    /** 12.01.01.02 Categorize ticket — free text, admin-set. Null until categorized. */
    @Column(length = 100)
    private String category;

    /** 12.01.01.03 Prioritize ticket — admin-set; starts MEDIUM. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    /** 12.01.01.04 Assign ticket — a platform admin/support agent (any
     * Customer id, not role-checked further — see business-rules.md). */
    @Column(name = "assigned_to_customer_id")
    private Long assignedToCustomerId;

    @Column(name = "resolution_note", length = 2000)
    private String resolutionNote;

    private Instant resolvedAt;

    private Instant closedAt;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
