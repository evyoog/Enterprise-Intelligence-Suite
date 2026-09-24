package com.vyoog.eisplatform.modules.authorization.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Deliberately has no {@code scope} or {@code organizationId} field — both
 * are resolved server-side ({@code scope} from which Role actually grants
 * {@code permissionName}; {@code organizationId} from the requester's own
 * membership when that turns out to be ORGANIZATION scope), so a caller can
 * never lie about either. {@code durationMinutes} is capped well below "any
 * duration" — see {@code PrivilegedAccessService}'s own constant — since an
 * unbounded "temporary" grant defeats the entire point of this phase.
 */
public record PrivilegedAccessRequestCreateRequest(
    @NotBlank String permissionName,
    @NotBlank String justification,
    @NotNull @Min(1) @Max(480) Integer durationMinutes
) {
}
