package com.vyoog.eisplatform.modules.servicestatus.dto;

import com.vyoog.eisplatform.modules.servicestatus.model.IncidentPhase;
import com.vyoog.eisplatform.modules.servicestatus.model.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Post or update an incident. The first five fields are the public ones ({@code endedAt} null = still open). The rest are internal (C101, IC-1 A) and
 * optional: left out, they keep their value. {@code phase} left out is worked out from {@code endedAt}. */
public record IncidentRequest(
    @NotNull Long productId,
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 4000) String message,
    @NotNull Instant startedAt,
    Instant endedAt,
    IncidentSeverity severity,
    IncidentPhase phase,
    @Size(max = 4000) String internalNotes,
    Long assignedToCustomerId,
    @Size(max = 2000) String resolutionSummary
) {
}
