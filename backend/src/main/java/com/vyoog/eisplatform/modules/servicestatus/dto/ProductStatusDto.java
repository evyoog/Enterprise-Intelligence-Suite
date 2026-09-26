package com.vyoog.eisplatform.modules.servicestatus.dto;

import com.vyoog.eisplatform.modules.servicestatus.model.ServiceStatusValue;

import java.time.Instant;

/** One product on the status page. {@code purchased}: the viewer's organization
 * or account has an active subscription, so incident details are visible (C26). */
public record ProductStatusDto(
    Long productId,
    String productName,
    ServiceStatusValue status,
    String note,
    Instant updatedAt,
    boolean purchased,
    long openIncidents
) {
}
