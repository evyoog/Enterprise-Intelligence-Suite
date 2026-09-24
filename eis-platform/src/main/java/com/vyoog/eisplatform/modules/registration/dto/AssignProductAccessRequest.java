package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** ORG_ADMIN-only — grants (or updates) one org member's own access to one
 * product the ORGANIZATION already holds an active subscription for. See
 * OrganizationProductAccess's own javadoc for why this is a distinct fact
 * from the org's subscription itself. */
public record AssignProductAccessRequest(
    @NotNull Long productId,
    @NotBlank String productRole
) {
}
