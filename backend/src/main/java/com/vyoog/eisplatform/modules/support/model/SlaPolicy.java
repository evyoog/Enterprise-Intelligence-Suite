package com.vyoog.eisplatform.modules.support.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C101 / REQ-SUP-002 (SL-1 B, SL-2 B): the support targets of one priority, the same for every customer and plan. Minutes of business time, or of the
 * clock when {@link #aroundTheClock} (Urgent). */
@Entity
@Table(name = "sla_policy")
@Getter
@Setter
public class SlaPolicy {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketPriority priority;

    @Column(name = "first_response_minutes", nullable = false)
    private int firstResponseMinutes;

    @Column(name = "resolution_minutes", nullable = false)
    private int resolutionMinutes;

    @Column(name = "around_the_clock", nullable = false)
    private boolean aroundTheClock;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
