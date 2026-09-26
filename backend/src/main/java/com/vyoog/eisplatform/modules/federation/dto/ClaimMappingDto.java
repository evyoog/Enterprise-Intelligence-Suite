package com.vyoog.eisplatform.modules.federation.dto;

import jakarta.validation.constraints.Size;

/** REQ-IAM-007: request and response shape for a provider's claim mapping.
 * A blank or null name means "use the defaults". */
public record ClaimMappingDto(
    @Size(max = 255) String email,
    @Size(max = 255) String firstName,
    @Size(max = 255) String lastName,
    @Size(max = 255) String displayName
) {
}
