package com.vyoog.eisplatform.modules.administration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 15.01.02 Configure defaults (REQ-GOV-002, C92, TM-2 A): what a user gets before changing anything — language, region
 * (a formatting locale such as en-IN), currency, time zone and date format. One row (id 1). A null value means "no platform
 * default", so the browser decides, exactly as before. Reminder days live in the billing settings (REQ-SUB-004).
 */
@Entity
@Table(name = "platform_default")
@Getter
@Setter
public class PlatformDefault {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(length = 10)
    private String language;

    @Column(length = 20)
    private String region;

    @Column(length = 10)
    private String currency;

    @Column(name = "time_zone", length = 64)
    private String timeZone;

    @Column(name = "date_format", length = 20)
    private String dateFormat;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
