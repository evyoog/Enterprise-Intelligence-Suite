package com.vyoog.eisplatform.modules.servicestatus.dto;

import com.vyoog.eisplatform.modules.servicestatus.model.ServiceStatusValue;

import java.time.Instant;

/** One product on the status page. {@code purchased}: the viewer's organization
 * or account has an active subscription, so incident details are visible (C26).
 * {@code source}: INSTANCES when the status comes from the instances' recent health reports (C101 SR-3 B), MANUAL when an admin set it (or no instance
 * reported recently); the counts are the instances behind an INSTANCES status. */
public record ProductStatusDto(
    Long productId,
    String productName,
    ServiceStatusValue status,
    String note,
    Instant updatedAt,
    boolean purchased,
    long openIncidents,
    String source,
    int reportingInstances,
    int downInstances,
    int degradedInstances
) {
}
