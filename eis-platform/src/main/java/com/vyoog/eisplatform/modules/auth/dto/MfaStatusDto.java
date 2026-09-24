package com.vyoog.eisplatform.modules.auth.dto;

import java.time.Instant;

public record MfaStatusDto(boolean enabled, Instant enrolledAt, long remainingRecoveryCodes) {
}
