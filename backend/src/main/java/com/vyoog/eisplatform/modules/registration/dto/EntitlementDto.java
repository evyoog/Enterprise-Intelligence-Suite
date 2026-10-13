package com.vyoog.eisplatform.modules.registration.dto;

import java.time.Instant;
import java.util.List;

/** REQ-SUB-002 (C97 EN-1): one entitlement, derived at read time from an ACTIVE subscription and its plan. Nothing here is stored. */
public record EntitlementDto(
    Long subscriptionId,
    Long productId,
    String productName,
    /** INDIVIDUAL or ORGANIZATION. */
    String ownerType,
    Long planId,
    String planName,
    String status,
    Instant expiresAt,
    List<String> includedFeatures,
    /** The plan's limit only (C97 EN-5); each product counts its own usage and enforces it. Null means no limit is set. */
    Integer usageLimit,
    int quantity
) {
}
