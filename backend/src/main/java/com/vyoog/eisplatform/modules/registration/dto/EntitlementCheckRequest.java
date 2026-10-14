package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotNull;

/** C97 EN-2: {@code POST /internal/entitlements/check}. Give {@code customerId} (an individual, or an organization member), {@code organizationId} (the
 * organization as a whole), or both (that customer as a member of that organization). {@code featureKey} is optional. */
public record EntitlementCheckRequest(Long customerId, Long organizationId, @NotNull Long productId, String featureKey) {
}
