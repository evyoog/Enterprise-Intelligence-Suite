package com.vyoog.eisplatform.modules.registration.dto;

import java.time.Instant;
import java.util.List;

/** C97 EN-2. {@code reason} is null when allowed, else one of NO_SUBSCRIPTION, NO_MEMBER_ACCESS, FEATURE_NOT_INCLUDED, UNKNOWN_PRODUCT, BAD_REQUEST. */
public record EntitlementCheckResponse(
    boolean allowed,
    String reason,
    Long productId,
    String plan,
    String ownerType,
    Instant expiresAt,
    List<String> includedFeatures,
    Integer usageLimit
) {
    static EntitlementCheckResponse denied(String reason, Long productId) {
        return new EntitlementCheckResponse(false, reason, productId, null, null, null, List.of(), null);
    }

    public static EntitlementCheckResponse deny(String reason, Long productId) {
        return denied(reason, productId);
    }
}
