package com.vyoog.eisplatform.modules.servicestatus.model;

/** C26 (2026-09-26, REQ-PRT-001): the status values a platform admin can post. */
public enum ServiceStatusValue {
    OPERATIONAL,
    DEGRADED,
    PARTIAL_OUTAGE,
    MAJOR_OUTAGE,
    MAINTENANCE
}
