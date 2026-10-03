package com.vyoog.eisplatform.modules.renewal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** REQ-SUB-004.8 (C64): one user's renewal reminder settings, for all their
 * subscriptions (OQ 6 default). No row = defaults (on, platform days and time). */
@Entity
@Table(name = "renewal_reminder_preference")
@Getter
@Setter
public class RenewalReminderPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(nullable = false)
    private boolean enabled = true;

    /** 1–30; null = the platform default. */
    @Column(name = "days_before")
    private Integer daysBefore;

    /** HH:mm; null = the platform default. */
    @Column(name = "send_time", length = 5)
    private String sendTime;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
