package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeMemberStatusRequest(@NotNull MemberStatusAction action) {
}
