package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Platform-admin-only — see the seat model's rule that only Vyoog Admin may
 * raise (or lower) an organization's licensed seat count. */
public record UpdateSeatsRequest(@NotNull @Min(0) Integer licensedSeats) {
}
