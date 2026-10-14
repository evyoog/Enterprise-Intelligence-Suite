package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotNull;

/** C100 RR-1 B: turn auto-renew on or off for one subscription. */
public record AutoRenewRequest(@NotNull Boolean autoRenew) {
}
