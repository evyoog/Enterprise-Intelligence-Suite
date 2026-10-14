package com.vyoog.eisplatform.modules.support.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;

/** C101 / REQ-SUP-002: the business hours the business-hours targets run in, when a warning goes out, and who is alerted besides the assignee. One row. */
@Entity
@Table(name = "sla_settings")
@Getter
@Setter
public class SlaSettings {

    @Id
    private Short id = 1;

    /** Comma-separated, e.g. {@code MON,TUE,WED,THU,FRI}. */
    @Column(name = "business_days", nullable = false, length = 30)
    private String businessDays = "MON,TUE,WED,THU,FRI";

    @Column(name = "business_start", nullable = false)
    private LocalTime businessStart = LocalTime.of(9, 0);

    @Column(name = "business_end", nullable = false)
    private LocalTime businessEnd = LocalTime.of(18, 0);

    @Column(name = "time_zone", nullable = false, length = 60)
    private String timeZone = "Asia/Kolkata";

    @Column(name = "warn_at_percent", nullable = false)
    private int warnAtPercent = 80;

    /** Comma-separated addresses. */
    @Column(name = "alert_emails", length = 1000)
    private String alertEmails;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
