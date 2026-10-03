package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** REQ-SUB-003.2 (BR-1): the new seat quantity, 1–100 000. */
public record ChangeSeatsRequest(@NotNull @Min(1) @Max(100000) Integer quantity) {
}
