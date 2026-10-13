package com.vyoog.eisplatform.modules.integration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * REQ-INT-002 (C91, EV-3 and EV-4): the event platform's settings, one row (id 1). Retention starts at 30 days and the
 * platform administrator can change it; failure alerts go to the listed addresses in the app and by email.
 */
@Entity
@Table(name = "event_platform_setting")
@Getter
@Setter
public class EventPlatformSetting {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "retention_days", nullable = false)
    private int retentionDays = 30;

    /** Comma-separated, lower-case email addresses. */
    @Column(name = "alert_recipients", length = 2000)
    private String alertRecipients;

    @Column(name = "alert_in_app", nullable = false)
    private boolean alertInApp = true;

    @Column(name = "alert_email", nullable = false)
    private boolean alertEmail = true;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
